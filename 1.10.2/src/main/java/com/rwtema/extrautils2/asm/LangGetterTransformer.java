package com.rwtema.extrautils2.asm;

import com.rwtema.extrautils2.utils.Lang;
import java.util.ArrayList;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class LangGetterTransformer implements IClassTransformer {
	boolean initLoading = false;

	static String LANG_CLASS;

	static {
		LANG_CLASS = "com.rwtema.extrautils2.utils.Lang";
	}

	String LANG_TYPE = LANG_CLASS.replace('.', '/');

	ArrayList<String> translating = null;


	public LangGetterTransformer() {
		super();
	}

	@Override
	public byte[] transform(String s, String s2, byte[] bytes) {
		if (CoreXU2.runtimeDeobfuscationEnabled)
			return bytes;

		if (!s2.startsWith("com.rwtema.extrautils2"))
			return bytes;

		if (LANG_CLASS.equals(s2))
			return bytes;

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for (MethodNode method : classNode.methods) {


			AbstractInsnNode[] nodes = method.instructions.toArray();
			for (int i = 0; i < nodes.length; i++) {

				AbstractInsnNode node = nodes[i];
				if (i > 0 && node.getOpcode() == Opcodes.INVOKESTATIC) {
					MethodInsnNode node1 = (MethodInsnNode) node;
					if (LANG_TYPE.equals(node1.owner)) {
						if ("translate".equals(node1.name) && "(Ljava/lang/String;)Ljava/lang/String;".equals(node1.desc)) {
							AbstractInsnNode node2 = nodes[i - 1];
							if (node2.getOpcode() == Opcodes.LDC) {
								LdcInsnNode ldc = (LdcInsnNode) node2;
								if (ldc.cst instanceof String) {
									addTranslate((String) ldc.cst);
								}
							}
						}

						if (
								("translateArgs".equals(node1.name) && "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;".equals(node1.desc)) ||
										("chat".equals(node1.name) && "(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/util/text/TextComponentTranslation;".equals(node1.desc))
								) {

							for (int j = (i - 1); j >= 2; j--) {
								if (nodes[j].getType() == AbstractInsnNode.LINE) {
									break;
								}

								if (nodes[j].getOpcode() == Opcodes.ANEWARRAY) {
									if (nodes[j - 2].getOpcode() == Opcodes.LDC) {
										LdcInsnNode node2 = (LdcInsnNode) nodes[j - 2];
										if (node2.cst instanceof String) {
											addTranslate((String) node2.cst);
										}
									}
									break;
								}
							}
						}
					}
				}
			}
		}

		return bytes;
	}

	protected void addTranslate(String cst) {
		ArrayList<String> list = translating;
		if (list != null) {
			list.add(cst);
		} else {
			ArrayList<String> strings = new ArrayList<>();
			strings.add(cst);
			translating = strings;
			Lang.init();
			translating = null;
			for (String string : strings) {
				Lang.translate(string);
			}
		}
	}
}
