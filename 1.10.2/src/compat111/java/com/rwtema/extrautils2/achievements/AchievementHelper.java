package com.rwtema.extrautils2.achievements;

import com.google.common.collect.HashMultimap;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import gnu.trove.list.array.TIntArrayList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.toposort.TopologicalSort;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AchievementHelper {
	private static HashMultimap<ItemRef, XUAchievement> dropMap = HashMultimap.create();
	private static HashMultimap<XUAchievement, XUAchievement> achievementChildren = HashMultimap.create();
	private static AchievementPage page;
	private static List<Achievement> achievements;
	private static HashMap<IItemStackMaker, XUAchievement> map = new HashMap<>();
	private static boolean built = false;

	private static HashSet<String> names = new HashSet<>();

	public static void addAchievement(String name, String description, @Nonnull IItemStackMaker entry, @Nullable IItemStackMaker parent) {
		if (!names.add(name))
			throw new RuntimeException("Duplicate Name " + name);
		XUAchievement achievement = new XUAchievement(name, description, entry, parent);
		map.put(entry, achievement);
	}

	public static void bake() {
		if (page == null) {
			page = new AchievementPage("ExtraUtils 2");
			AchievementPage.registerAchievementPage(page);
			achievements = page.getAchievements();
		} else {
			achievements.clear();
			achievementChildren.clear();
			dropMap.clear();
		}

		TopologicalSort.DirectedGraph<XUAchievement> graph = new TopologicalSort.DirectedGraph<>();

		for (XUAchievement xuAchievement : map.values()) {
			graph.addNode(xuAchievement);
		}

		for (XUAchievement xuAchievement : map.values()) {
			if (xuAchievement.parent == null) {
				achievementChildren.put(null, xuAchievement);
				continue;
			}

			xuAchievement.achParent = map.get(xuAchievement.parent);

			achievementChildren.put(xuAchievement.achParent, xuAchievement);
			if (xuAchievement.achParent != null) {
				graph.addEdge(xuAchievement.achParent, xuAchievement);
			} else {
				xuAchievement.achParent = null;
			}
		}

		List<XUAchievement> sort = TopologicalSort.topologicalSort(graph);


		new Layout().buildTree();

		for (int i = 1; i < sort.size(); i++) {
			XUAchievement a = sort.get(i);
			for (int j = 0; j < i; j++) {
				XUAchievement b = sort.get(j);
				if (a != b && a.x == b.x && a.y == b.y) {
					LogHelper.info(a + " " + b);
					throw new RuntimeException("Overlay Error\n " + a + "\n " + b);
				}
			}
		}

		int xc = 0;
		int yc = 0;

		for (XUAchievement xuAchievement : sort) {
			xc += xuAchievement.x;
			yc += xuAchievement.y;
		}

		xc = xc / sort.size();
		yc = yc / sort.size();

		for (XUAchievement xuAchievement : sort) {
			String key = Lang.stripText(xuAchievement.name);
			String achKey = "achievements.xu2." + key;
			Lang.translate(achKey, xuAchievement.name);
			Lang.translate(achKey + ".desc", xuAchievement.description);
			Achievement parentAch = xuAchievement.achParent != null ? xuAchievement.achParent.achievement : null;
			ItemStack stack = xuAchievement.entry.newStack();
			if (StackHelper.isNull(stack)) {
				stack = ItemIngredients.Type.SYMBOL_NOCRAFT.newStack();
			} else {
				dropMap.put(ItemRef.wrap(stack), xuAchievement);
				if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
					stack.setItemDamage(0);
				}
			}

			int y = xuAchievement.x - xc;
			int x = xuAchievement.y - yc;

			Achievement achievement = new Achievement("achievements.xu2." + key, "xu2." + key, x, y, stack, parentAch);


			if (!built)
				achievement.registerStat();

			xuAchievement.achievement = achievement;

			achievements.add(achievement);
		}


		if (!built) {
			MinecraftForge.EVENT_BUS.register(new Object() {
				@SubscribeEvent
				public void onTick(TickEvent.PlayerTickEvent event) {
					if (event.side == Side.SERVER) {
						if (event.player.getEntityWorld().getTotalWorldTime() % 40 == 0) {
							InventoryPlayer inventory = event.player.inventory;
							for (int i = 0; i < inventory.getSizeInventory(); i++) {
								ItemStack stackInSlot = inventory.getStackInSlot(i);
								if (StackHelper.isNonNull(stackInSlot)) {
									checkForPotentialAwards(event.player, stackInSlot);
								}
							}
						}
					}
				}

				@SubscribeEvent
				public void onPickup(PlayerEvent.ItemPickupEvent event) {
					EntityItem pickedUp = event.pickedUp;
					if (pickedUp == null) return;
					checkForPotentialAwards(event.player, pickedUp.getItem());
				}

				@SubscribeEvent
				public void onSmelt(PlayerEvent.ItemSmeltedEvent event) {
					checkForPotentialAwards(event.player, event.smelting);
				}

				@SubscribeEvent
				public void onCraft(PlayerEvent.ItemCraftedEvent event) {
					checkForPotentialAwards(event.player, event.crafting);
				}
			});
		}

		built = true;
	}

	public static void checkForPotentialAwards(EntityPlayer player, ItemStack stack) {
		if (StackHelper.isNull(stack) || player == null) return;
		Set<XUAchievement> set = dropMap.get(ItemRef.wrap(stack));
		if (!set.isEmpty())
			for (XUAchievement achievement : set) {
				player.addStat(achievement.achievement);
			}
	}

	public static class XUAchievement {
		public Achievement achievement;
		public String name;
		public String description;
		@Nonnull
		public IItemStackMaker entry;
		@Nullable
		public IItemStackMaker parent;
		public XUAchievement achParent;
		int x;
		int y;

		public XUAchievement(String name, String description, @Nonnull IItemStackMaker entry, @Nullable IItemStackMaker parent) {
			this.name = name;
			this.description = description;
			this.entry = entry;
			this.parent = parent;
		}

		@Override
		public String toString() {
			return "XUAchievement{" + name + '}';
		}

	}

	private static class Layout {

		public void buildTree() {
			TreeNode treeNode = new TreeNode(achievementChildren.get(null));
			treeNode.buildDepth();
			treeNode.assignValues(0, 0);
		}

		private static class TreeNode {
			public XUAchievement xuAchievement;

			public TIntArrayList minPoints = new TIntArrayList();
			public TIntArrayList maxPoints = new TIntArrayList();

			int shift;

			ArrayList<TreeNode> children = new ArrayList<>();

			public TreeNode(Collection<XUAchievement> xuChildren) {
				ArrayList<XUAchievement> list = new ArrayList<>(xuChildren);
				Collections.shuffle(list);
				for (XUAchievement achievement : list) {
					children.add(new TreeNode(achievement));
				}
			}

			public TreeNode(XUAchievement xuAchievement) {
				this.xuAchievement = xuAchievement;
				ArrayList<XUAchievement> list = new ArrayList<>(achievementChildren.get(xuAchievement));

				int n = list.size();
				if (n != 0) {
					if (n == 1) {
						children.add(new TreeNode(new TreeNode(list.get(0))));
					} else {
						Collections.shuffle(list);
						for (int i = 0; i < list.size(); i++) {
							int k = Math.min(i, list.size() - 1 - i);
							XUAchievement achievement = list.get(i);
							children.add(linkedChain(new TreeNode(achievement), k));
						}
					}
				}
			}

			public TreeNode(TreeNode child) {
				children.add(child);
			}

			public static TreeNode linkedChain(TreeNode child, int chain_length) {
				if (chain_length == 0) return child;
				return new TreeNode(linkedChain(child, chain_length - 1));
			}


			void buildDepth() {
				int n = children.size();
				if (n == 0) {
					minPoints.add(0);
					maxPoints.add(0);
				} else if (n == 1) {
					TreeNode child = children.get(0);
					child.buildDepth();
					minPoints.add(0);
					maxPoints.add(0);
					child.shift = 0;
					minPoints.addAll(child.minPoints);
					maxPoints.addAll(child.maxPoints);
				} else {
					int maxDepth = 0;
					for (TreeNode child : children) {
						child.buildDepth();
						maxDepth = Math.max(Math.max(maxDepth, child.minPoints.size()), child.maxPoints.size());
					}

					int maxShift = 0;

					for (int i = 1; i < n; i++) {
						TreeNode curChild = children.get(i);
						curChild.shift = maxShift;
						shifting:
						while (true) {
							curChild.shift++;
							for (int j = i - 1; j >= 0; j--) {
								if (overlaps(children.get(j), curChild))
									continue shifting;
							}
							maxShift = curChild.shift;
							break;
						}
					}

					if ((maxShift & 1) != 0) {
						int midPoint = maxShift >> 1;
						for (TreeNode child : children) {
							if (child.shift > midPoint) {
								child.shift++;
							}
						}
						maxShift++;
					}

					int midPoint = maxShift >> 1;

					for (TreeNode child : children) {
						child.shift -= midPoint;
					}

					minPoints.add(children.get(0).shift);
					maxPoints.add(children.get(n - 1).shift);

					for (int i = 0; i < maxDepth; i++) {
						minPoints.add(0);
						maxPoints.add(0);
					}

					for (TreeNode child : children) {
						for (int i = 0; i < child.minPoints.size(); i++) {
							int x = child.shift + child.minPoints.get(i);
							if (minPoints.get(i + 1) > x) {
								minPoints.set(i + 1, x);
							}
						}

						for (int i = 0; i < child.maxPoints.size(); i++) {
							int x = child.shift + child.maxPoints.get(i);
							if (maxPoints.get(i + 1) < x) {
								maxPoints.set(i + 1, x);
							}
						}
					}
				}
			}

			public boolean overlaps(TreeNode left, TreeNode right) {
				int size = Math.min(left.maxPoints.size(), right.minPoints.size());
				for (int i = 0; i < size; i++) {
					int l_max = left.shift + left.maxPoints.get(i);
					int r_min = right.shift + right.minPoints.get(i);
					if (r_min <= l_max) return true;
				}

				return false;
			}

			public void assignValues(int x, int y) {
				if (xuAchievement != null) {
					xuAchievement.x = x;
					xuAchievement.y = y;
				}

				for (TreeNode child : children) {
					child.assignValues(x + child.shift, y + 1);
				}
			}
		}
	}

}
