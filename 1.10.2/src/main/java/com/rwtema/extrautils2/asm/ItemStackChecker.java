package com.rwtema.extrautils2.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

public class ItemStackChecker implements IClassTransformer {

	TreeSet<String> found_errors = new TreeSet<>();

	@Override
	public byte[] transform(String s, String s1, byte[] bytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
		String itemStackTypeDesc = "Lnet/minecraft/item/ItemStack;";
		Type type = Type.getType(itemStackTypeDesc);

		boolean shouldReport = false;

		HashSet<LocalVariableNode> itemStackVariables = new HashSet<>();
		HashSet<LabelNode> visitedLabels = new HashSet<>();

		for (MethodNode method : classNode.methods) {
			Type returnType = Type.getReturnType(method.desc);

			visitedLabels.clear();
			itemStackVariables.clear();
			List<LocalVariableNode> localVariables = method.localVariables;
			if (localVariables != null)
				for (LocalVariableNode localVariable : localVariables) {
					if (type.equals(Type.getType(localVariable.desc))) {
						itemStackVariables.add(localVariable);
					}
				}

			boolean checkReturn = returnType.equals(type);

			boolean foundAconstNull = false;


			int lineNumber = -1;

			ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
			while (iterator.hasNext()) {
				AbstractInsnNode insnNode = iterator.next();

				int opcode = insnNode.getOpcode();

				if (opcode == Opcodes.ACONST_NULL) {
					foundAconstNull = true;
				} else if (opcode == -1) {
					if (insnNode instanceof LabelNode) {
						visitedLabels.add((LabelNode) insnNode);
					}
					if (insnNode instanceof LineNumberNode) {
						lineNumber = ((LineNumberNode) insnNode).line;
					}
				} else if (opcode == Opcodes.ASTORE && foundAconstNull) {
					VarInsnNode varInsnNode = (VarInsnNode) insnNode;
					for (LocalVariableNode itemStackVariable : itemStackVariables) {
						if (itemStackVariable.index != varInsnNode.var
								|| !visitedLabels.contains(itemStackVariable.start)
								|| visitedLabels.contains(itemStackVariable.end)) {
							continue;
						}
						report(classNode, method, lineNumber);
						shouldReport = true;
						break;
					}
					foundAconstNull = false;
				} else if (checkReturn && opcode == Opcodes.ARETURN && foundAconstNull) {
					report(classNode, method, lineNumber);
					shouldReport = true;
					foundAconstNull = false;
				} else if ((opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) && foundAconstNull) {
					FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
					if (type.equals(Type.getType(fieldInsnNode.desc))) {
						report(classNode, method, lineNumber);
						shouldReport = true;
					}
					foundAconstNull = false;
				} else {
					foundAconstNull = false;
				}
			}
		}

		if (shouldReport) {
			StringBuilder builder = new StringBuilder("Found null ItemStack assignments:");
			for (String s2 : found_errors) {
				builder.append('\n').append('\t').append(s2);
			}
			ClassTransformerHandler.logger.info(builder.toString());
		}

		return bytes;
	}

	private void report(ClassNode classNode, MethodNode method, int lineNumber) {
		String s = new StackTraceElement(classNode.name.replace('/', '.'), method.name, classNode.sourceFile, lineNumber).toString();
		found_errors.add(s);
	}
}
