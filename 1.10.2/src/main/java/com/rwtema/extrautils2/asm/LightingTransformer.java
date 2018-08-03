package com.rwtema.extrautils2.asm;

import com.google.common.collect.Sets;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

public class LightingTransformer implements IClassTransformer {
	Set<String> getLightFor = Sets.newHashSet("getLightFor", "func_175642_b");
	Set<String> getLightFromNeighborsFor = Sets.newHashSet("getLightFromNeighborsFor", "func_175705_a");
	Set<String> getLightForExt = Sets.newHashSet("getLightForExt", "func_175629_a");
	Set<String> exceptions = Sets.newHashSet("checkLightFor", "getRawLight", "func_180500_c", "func_175638_a");
	Set<String> getLightSubtracted = Sets.newHashSet("getLightSubtracted", "func_177443_a");
	Set<String> world = Sets.newHashSet("world", "field_72815_e", "field_76637_e");

	@Override
	public byte[] transform(String s2, String s, byte[] bytes) {
		if (bytes == null)
			return null;

		switch (s) {
			case "net.minecraft.world.chunk.Chunk": {
				ClassNode classNode = new ClassNode();
				ClassReader classReader = new ClassReader(bytes);
				classReader.accept(classNode, 0);
				String getLightSubtractedName = null;
				MethodNode m = null;
				String passThru = "getLightSubtractedPassThru";
				for (MethodNode method : classNode.methods) {
					if (getLightSubtracted.contains(method.name)) {
						m = method;
						getLightSubtractedName = method.name;
						method.name = passThru;
					}
				}
				String worldObjFieldName = getField(classNode, world);
				if (getLightSubtractedName == null || worldObjFieldName == null) {
					ClassTransformerHandler.logger.info("Chunk Failed - " + getLightSubtractedName + " " + worldObjFieldName);
					return bytes;
				}
				MethodNode methodNode = new MethodNode(m.access, getLightSubtractedName, m.desc, m.signature, m.exceptions.toArray(new String[m.exceptions.size()]));
				methodNode.visitCode();
				methodNode.visitVarInsn(ALOAD, 0);
				methodNode.visitFieldInsn(GETFIELD, "net/minecraft/world/chunk/Chunk", worldObjFieldName, "Lnet/minecraft/world/World;");
				methodNode.visitVarInsn(ALOAD, 1);
				methodNode.visitVarInsn(ALOAD, 0);
				methodNode.visitVarInsn(ALOAD, 1);
				methodNode.visitVarInsn(ILOAD, 2);
				methodNode.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/chunk/Chunk", passThru, m.desc, false);
				methodNode.visitMethodInsn(INVOKESTATIC, "com/rwtema/extrautils2/asm/Lighting", "getCombinedLight", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;I)I", false);
				methodNode.visitInsn(IRETURN);
				classNode.methods.add(methodNode);
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				classNode.accept(writer);
				return writer.toByteArray();
			}
			case "net.minecraft.world.World": {
				ClassNode classNode = new ClassNode();
				ClassReader classReader = new ClassReader(bytes);
				classReader.accept(classNode, 0);
				if (overrideType(classNode, getLightFor, exceptions)) {
					ClassTransformerHandler.logger.info("World failed -" + getLightFor.toString() + " " + exceptions.toString());
					return bytes;
				}
				if (FMLLaunchHandler.side() == Side.CLIENT) {
					if (overrideType(classNode, getLightFromNeighborsFor, null)) {
						ClassTransformerHandler.logger.info("World failed -" + getLightFromNeighborsFor.toString());
					}
				}
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				classNode.accept(writer);
				return writer.toByteArray();
			}
			case "net.minecraft.world.ChunkCache": {
				if (FMLLaunchHandler.side() != Side.CLIENT)
					return bytes;
				ClassNode classNode = new ClassNode();
				ClassReader classReader = new ClassReader(bytes);
				classReader.accept(classNode, 0);
				String getLightForExtName = null;
				MethodNode m = null;
				String passThru = "getLightForExtPassThru";
				for (MethodNode method : classNode.methods) {
					if (getLightForExt.contains(method.name)) {
						m = method;
						getLightForExtName = method.name;
						method.name = passThru;
					}
				}
				String worldObjFieldName = getField(classNode, world);
				if (getLightForExtName == null || worldObjFieldName == null) {
					ClassTransformerHandler.logger.info("Chunk Failed - " + getLightForExtName + " " + worldObjFieldName);
					return bytes;
				}
				MethodNode methodNode = new MethodNode(m.access, getLightForExtName, m.desc, m.signature, m.exceptions.toArray(new String[m.exceptions.size()]));
				methodNode.visitCode();
				methodNode.visitVarInsn(ALOAD, 0);
				methodNode.visitFieldInsn(GETFIELD, "net/minecraft/world/ChunkCache", worldObjFieldName, "Lnet/minecraft/world/World;");
				methodNode.visitVarInsn(ALOAD, 1);
				methodNode.visitVarInsn(ALOAD, 2);
				methodNode.visitVarInsn(ALOAD, 0);
				methodNode.visitVarInsn(ALOAD, 1);
				methodNode.visitVarInsn(ALOAD, 2);
				methodNode.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/ChunkCache", passThru, m.desc, false);
				methodNode.visitMethodInsn(INVOKESTATIC, "com/rwtema/extrautils2/asm/Lighting", "getLightFor", "(Lnet/minecraft/world/World;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;I)I", false);
				methodNode.visitInsn(IRETURN);
				classNode.methods.add(methodNode);
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				classNode.accept(writer);
				return writer.toByteArray();
			}
			default:
				return bytes;
		}
	}

	public String getField(ClassNode classNode, Set<String> fieldNames) {
		String fieldName = null;
		for (FieldNode field : classNode.fields) {
			if (fieldNames.contains(field.name)) {
				fieldName = field.name;
				break;
			}
		}
		return fieldName;
	}

	private boolean overrideType(ClassNode classNode, Set<String> methodName, Set<String> exceptions) {
		String getLightForName = null;
		MethodNode m = null;

		String passThru = null;
		for (MethodNode method : classNode.methods) {
			if (methodName.contains(method.name)) {
				m = method;
				getLightForName = method.name;
				passThru = getLightForName + "Implementation";
				method.name = passThru;
				break;
			}
		}

		if (getLightForName == null) return true;

		if (exceptions != null)
			for (MethodNode method : classNode.methods) {
				if (exceptions.contains(method.name)) {
					ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
					while (iterator.hasNext()) {
						AbstractInsnNode next = iterator.next();
						if (next.getType() == AbstractInsnNode.METHOD_INSN) {
							MethodInsnNode methodNode = (MethodInsnNode) next;
							if (getLightForName.equals(methodNode.name)) {
								methodNode.name = passThru;
							}
						}
					}
				}
			}

		MethodNode methodNode = new MethodNode(m.access, getLightForName, m.desc, m.signature, m.exceptions.toArray(new String[0]));
		methodNode.visitCode();
		methodNode.visitVarInsn(ALOAD, 0);
		methodNode.visitVarInsn(ALOAD, 1);
		methodNode.visitVarInsn(ALOAD, 2);
		methodNode.visitVarInsn(ALOAD, 0);
		methodNode.visitVarInsn(ALOAD, 1);
		methodNode.visitVarInsn(ALOAD, 2);
		methodNode.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/World", passThru, m.desc, false);
		methodNode.visitMethodInsn(INVOKESTATIC, "com/rwtema/extrautils2/asm/Lighting", "getLightFor", "(Lnet/minecraft/world/World;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;I)I", false);
		methodNode.visitInsn(IRETURN);

		classNode.methods.add(methodNode);
		return false;
	}
}

