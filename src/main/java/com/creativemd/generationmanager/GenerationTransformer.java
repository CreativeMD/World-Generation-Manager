package com.creativemd.generationmanager;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.File;
import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class GenerationTransformer implements IClassTransformer {
	
	public static final String[] names = new String[]{".", "net/minecraft/world/chunk/Chunk", "net/minecraft/world/World",
		"net/minecraft/world/chunk/IChunkProvider", "net/minecraft/world/chunk/IChunkGenerator"};
	public static final String[] namesOb = new String[]{"/", "ase", "aht", "arz", "ary"};
	
	public static String patch(String input)
	{
		for(int zahl = 0; zahl < names.length; zahl++)
			input = input.replace(names[zahl], namesOb[zahl]);
		return input;
	}
		
	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2) {
		if (arg0.equals("ase") | arg0.equals("net.minecraft.world.chunk.Chunk")) {
			System.out.println("[GenerationManager] Patching " + arg0);
			arg2 = addMethod(arg0, arg2, GenerationPatchingLoader.location, !arg0.equals(arg1));
		}
		if (arg0.equals("net.minecraftforge.fml.common.registry.GameRegistry")) {
			System.out.println("[GenerationManager] Patching " + arg0);
			arg2 = changeRegistry(arg0, arg2, GenerationPatchingLoader.location, !arg0.equals(arg1));
		}
		if (arg0.equals("net.minecraftforge.fml.common.IWorldGenerator")) {
			System.out.println("[GenerationManager] Patching " + arg0);
			arg2 = addIDMethod(arg0, arg2, GenerationPatchingLoader.location, !arg0.equals(arg1));
		}
		return arg2;
	}
	
	public byte[] changeRegistry(String name, byte[] bytes, File location, boolean obfuscated)
	{
		String targetMethodName = "registerWorldGenerator";
		String targetMethodName2 = "generateWorld";
		String targetDESC = "(Lnet/minecraftforge/fml/common/IWorldGenerator;I)V";
		String targetDESC2 = "(IILnet/minecraft/world/World;Lnet/minecraft/world/chunk/IChunkGenerator;Lnet/minecraft/world/chunk/IChunkProvider;)V";
		String fieldName = "sortedGeneratorList";
		String targetMethodName3 = "computeSortedGeneratorList";
		
		if(obfuscated)
		{
			targetDESC2 = patch(targetDESC2);
		}
		
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		
		Iterator<FieldNode> fields = classNode.fields.iterator();
		while(fields.hasNext())
		{
			FieldNode f = fields.next();
			if(f.name.equals(fieldName))
			{
				f.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC;
			}
		}	
		
		Iterator<MethodNode> methods = classNode.methods.iterator();
		while(methods.hasNext())
		{
			MethodNode m = methods.next();
			if ((m.name.equals(targetMethodName) && m.desc.equals(targetDESC)))
			{
				AbstractInsnNode currentNode = null;
				@SuppressWarnings("unchecked")
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				
				/*while (iter.hasNext())
				{
					currentNode = iter.next();
					if (currentNode instanceof LineNumberNode)
						iter.remove();
				}

				iter = m.instructions.iterator();*/
				
				while (iter.hasNext())
				{
					currentNode = iter.next();
					if (currentNode instanceof InsnNode && currentNode.getOpcode() == RETURN) // && currentNode.getPrevious() instanceof MethodInsnNode && ((MethodInsnNode) currentNode.getPrevious()).name.equals("add"))
					{
						m.instructions.insertBefore(currentNode, new LabelNode());
						m.instructions.insertBefore(currentNode, new VarInsnNode(ALOAD, 0));
						m.instructions.insertBefore(currentNode, new MethodInsnNode(INVOKESTATIC, "com/creativemd/generationmanager/GenerationDummyContainer", "onAdd", "(Lnet/minecraftforge/fml/common/IWorldGenerator;)V", false));//, "(Ljava/util/Set<Lcpw/mods/fml/common/IWorldGenerator;>;)V"));
						//m.instructions.insert(currentNode, new VarInsnNode(ALOAD, 0));
					}
				}
			}
			if ((m.name.equals(targetMethodName2) && m.desc.equals(targetDESC2)))
			{
		
				AbstractInsnNode currentNode = null;
		
				@SuppressWarnings("unchecked")
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				
				m.instructions.clear();
				m.instructions.add(new VarInsnNode(ILOAD, 0));
				m.instructions.add(new VarInsnNode(ILOAD, 1));
				m.instructions.add(new VarInsnNode(ALOAD, 2));
				m.instructions.add(new VarInsnNode(ALOAD, 3));
				m.instructions.add(new VarInsnNode(ALOAD, 4));
				m.instructions.add(new MethodInsnNode(INVOKESTATIC, "com/creativemd/generationmanager/GenerationDummyContainer", "generateWorld", targetDESC2, false));
				m.instructions.add(new InsnNode(Opcodes.RETURN));
			}
			if (m.name.equals(targetMethodName3))
			{
				m.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC;
			}
		}
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}
	
	public byte[] addIDMethod(String name, byte[] bytes, File location, boolean obfuscated)
	{
		ClassReader reader = new ClassReader(bytes);
		ClassNode clazz = new ClassNode();
		reader.accept(clazz, 0);
		
		MethodNode method = new MethodNode(ACC_PUBLIC + ACC_ABSTRACT, "getModID", "()Ljava/lang/String;", null, null);
		//method.instructions.add(new LdcInsnNode(""));
		//method.instructions.add(new InsnNode(ARETURN));
		clazz.methods.add(method);
		
		MethodNode method2 = new MethodNode(ACC_PUBLIC + ACC_ABSTRACT, "getName", "()Ljava/lang/String;", null, null);
		//method2.instructions.add(new LdcInsnNode(""));
		//method2.instructions.add(new InsnNode(ARETURN));
		clazz.methods.add(method2);
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		clazz.accept(writer);
		bytes = writer.toByteArray();
		return bytes;
	}
	
	public byte[] addMethod(String name, byte[] bytes, File location, boolean obfuscated)
	{
		ClassReader reader = new ClassReader(bytes);
		ClassNode clazz = new ClassNode();
		reader.accept(clazz, 0);
		
		clazz.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "modsGenerated", Type.getDescriptor(String[].class), null, null));
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		clazz.accept(writer);
		bytes = writer.toByteArray();
		return bytes;
	}
}
