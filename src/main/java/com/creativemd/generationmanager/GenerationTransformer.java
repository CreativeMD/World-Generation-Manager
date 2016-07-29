package com.creativemd.generationmanager;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
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

import com.creativemd.creativecore.transformer.CreativeTransformer;
import com.creativemd.creativecore.transformer.Transformer;

public class GenerationTransformer extends CreativeTransformer {

	public GenerationTransformer() {
		super("generationmanager");
	}

	@Override
	protected void initTransformers() {
		addTransformer(new Transformer("net.minecraft.world.chunk.Chunk") {
			
			@Override
			public void transform(ClassNode node) {
				node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "modsGenerated", Type.getDescriptor(String[].class), null, null));
			}
		});
		addTransformer(new Transformer("net.minecraftforge.fml.common.registry.GameRegistry") {
			
			@Override
			public void transform(ClassNode node) {
				String targetMethodName = "registerWorldGenerator";
				String targetMethodName2 = "generateWorld";
				String targetDESC = "(Lnet/minecraftforge/fml/common/IWorldGenerator;I)V";
				String targetDESC2 = patchDESC("(IILnet/minecraft/world/World;Lnet/minecraft/world/chunk/IChunkGenerator;Lnet/minecraft/world/chunk/IChunkProvider;)V");
				String fieldName = "sortedGeneratorList";
				String targetMethodName3 = "computeSortedGeneratorList";
				
				Iterator<FieldNode> fields = node.fields.iterator();
				while(fields.hasNext())
				{
					FieldNode f = fields.next();
					if(f.name.equals(fieldName))
					{
						f.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC;
					}
				}	
				
				Iterator<MethodNode> methods = node.methods.iterator();
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
			}
		});
		addTransformer(new Transformer("net.minecraftforge.fml.common.IWorldGenerator") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode method = new MethodNode(ACC_PUBLIC + ACC_ABSTRACT, "getModID", "()Ljava/lang/String;", null, null);
				node.methods.add(method);
				
				MethodNode method2 = new MethodNode(ACC_PUBLIC + ACC_ABSTRACT, "getName", "()Ljava/lang/String;", null, null);
				node.methods.add(method2);
			}
		});
	}

}
