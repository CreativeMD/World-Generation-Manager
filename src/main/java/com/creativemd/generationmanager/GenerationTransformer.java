package com.creativemd.generationmanager;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.tree.AbstractInsnNode.METHOD_INSN;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.launchwrapper.IClassTransformer;

public class GenerationTransformer implements IClassTransformer {

	//NOTE: This doesn't work for some cases like EntityItem and EntityItemFrame
	public static final String[] names = new String[]{".", "net/minecraft/world/chunk/Chunk", "net/minecraft/world/World",
		"net/minecraft/world/chunk/IChunkProvider"};
	public static final String[] namesOb = new String[]{"/", "apx", "ahb", "apu"};
	
	public static String patch(String input)
	{
		for(int zahl = 0; zahl < names.length; zahl++)
			input = input.replace(names[zahl], namesOb[zahl]);
		return input;
	}
		
	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2) {
		if (arg0.equals("apx") | arg0.equals("net.minecraft.world.chunk.Chunk")) {
			System.out.println("[GenerationManager] Patching " + arg0);
			arg2 = addMethod(arg0, arg2, GenerationPatchingLoader.location, !arg0.equals(arg1));
		}
		if (arg0.equals("cpw.mods.fml.common.registry.GameRegistry")) {
			System.out.println("[GenerationManager] Patching " + arg0);
			arg2 = changeRegistry(arg0, arg2, GenerationPatchingLoader.location, !arg0.equals(arg1));
		}
		if (arg0.equals("cpw.mods.fml.common.IWorldGenerator")) {
			System.out.println("[GenerationManager] Patching " + arg0);
			arg2 = addIDMethod(arg0, arg2, GenerationPatchingLoader.location, !arg0.equals(arg1));
		}
		return arg2;
	}
	
	public byte[] changeRegistry(String name, byte[] bytes, File location, boolean obfuscated)
	{
		String targetMethodName = "registerWorldGenerator";
		String targetMethodName2 = "generateWorld";
		String targetDESC = "(Lcpw/mods/fml/common/IWorldGenerator;I)V";
		String targetDESC2 = "(IILnet/minecraft/world/World;Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/chunk/IChunkProvider;)V";
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
						m.instructions.insertBefore(currentNode, new MethodInsnNode(INVOKESTATIC, "com/creativemd/generationmanager/GenerationDummyContainer", "onAdd", "(Lcpw/mods/fml/common/IWorldGenerator;)V"));//, "(Ljava/util/Set<Lcpw/mods/fml/common/IWorldGenerator;>;)V"));
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
				m.instructions.add(new MethodInsnNode(INVOKESTATIC, "com/creativemd/generationmanager/GenerationDummyContainer", "generateWorld", targetDESC2));
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
