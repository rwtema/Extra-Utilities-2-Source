package com.rwtema.extrautils2.asm;

import com.google.common.collect.Sets;
import java.util.ListIterator;
import java.util.Set;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class TessellatorTransformer implements IClassTransformer {
	Set<String> draw = Sets.newHashSet("draw", "func_78381_a");
	String name = "net.minecraft.client.renderer.Tessellator";

	@Override
	public byte[] transform(String s, String s1, byte[] bytes) {
		if (!name.equals(s1))
			return bytes;

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for (MethodNode method : classNode.methods) {
			if (draw.contains(method.name)) {
				ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
				while (iter.hasNext()) {
					AbstractInsnNode next = iter.next();
					if (next.getOpcode() == Opcodes.RETURN) {
						method.instructions.insertBefore(next, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/rwtema/extrautils2/utils/client/GLState", "resetStateQuads", "()V", false));
						break;
					}
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);

		return writer.toByteArray();
	}
}
