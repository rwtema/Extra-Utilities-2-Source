package com.rwtema.extrautils2.quarry;

import com.rwtema.extrautils2.utils.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.BlockStructureVoid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockBreakingRegistry {
	public static final BlockBreakingRegistry instance = new BlockBreakingRegistry();
	public static final HashMap<Block, entry> entries = new HashMap<>();
	public static final Set<String> methodNames;
	public static final Map<String, Boolean> names = new HashMap<>();
	public static final LaunchClassLoader cl = (LaunchClassLoader) BlockBreakingRegistry.class.getClassLoader();

	static {
		methodNames = new HashSet<>();

		for (Method m : BlockDummy.class.getDeclaredMethods()) {
			methodNames.add(m.getName());
		}
	}

	public static boolean blackList(Block id) {
		return entries.get(id).blackList;
	}

	public static boolean isSpecial(Block id) {
		return entries.get(id).isSpecial;
	}

	public static boolean isFence(Block id) {
		return entries.get(id).isFence;
	}

	public static boolean isFluid(Block id) {
		return entries.get(id).isFluid;
	}

	public void setupBreaking() {

	}

	public boolean hasSpecialBreaking(Class clazz) {
		if (clazz == null || clazz.equals(Block.class)) {
			return false;
		}

		if (clazz == BlockCommandBlock.class) return true;
		if (clazz == BlockStructure.class) return true;
		if (clazz == BlockStructureVoid.class) return true;

		if (names.containsKey(clazz.getName())) {
			return names.get(clazz.getName());
		}

		LogHelper.fine("Checking class for special block breaking code: " + clazz.getName());
		try {
			byte[] bytes;

			if (clazz.getClassLoader() instanceof LaunchClassLoader) {
				bytes = ((LaunchClassLoader) clazz.getClassLoader()).getClassBytes(clazz.getName());
			} else {
				bytes = cl.getClassBytes(clazz.getName());
			}

			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, 0);

			for (MethodNode method : classNode.methods) {
				if (methodNames.contains(method.name)) {
					LogHelper.fine("Detected special block breaking code in class: " + clazz.getName());
					names.put(clazz.getName(), true);
					return true;
				}
			}
		} catch (Throwable e) {
			try {
				for (Method m : clazz.getDeclaredMethods()) {
					if (methodNames.contains(m.getName())) {
						LogHelper.fine("Detected special block breaking code in class: " + clazz.getName());
						names.put(clazz.getName(), true);
						return true;
					}
				}
			} catch (Throwable e2) {
				LogHelper.error("Error checking block class code: " + clazz.getName());
				e.printStackTrace();
				e2.printStackTrace();
				names.put(clazz.getName(), true);
				return true;
			}
		}

		boolean result = hasSpecialBreaking(clazz.getSuperclass());
		names.put(clazz.getName(), result);
		return result;
	}

	public static class entry {
		public boolean isSpecial = false;
		public boolean blackList = false;
		public boolean isFence = false;
		public boolean isFluid = false;
	}
}
