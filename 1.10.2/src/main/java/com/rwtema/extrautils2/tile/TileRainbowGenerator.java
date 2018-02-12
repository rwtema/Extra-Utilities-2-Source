package com.rwtema.extrautils2.tile;


import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.api.machine.Machine;
import com.rwtema.extrautils2.api.machine.XUMachineGenerators;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.machine.BlockMachine;
import com.rwtema.extrautils2.machine.TileMachine;
import com.rwtema.extrautils2.network.SpecialChat;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.IPower;
import com.rwtema.extrautils2.power.IPowerSubType;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.tile.tesr.ITESRHook;
import com.rwtema.extrautils2.transfernodes.FacingHelper;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.MCTimer;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.QuadHelper;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class TileRainbowGenerator extends TilePower implements ITickable, ITESRHook, IPowerSubType {
	public static final int perTick = 25000000;
	public final static Machine[] GENERATORS = new Machine[]{
			XUMachineGenerators.CULINARY_GENERATOR,
			XUMachineGenerators.DEATH_GENERATOR,
			XUMachineGenerators.DRAGON_GENERATOR,
			XUMachineGenerators.ENCHANT_GENERATOR,
			XUMachineGenerators.ENDER_GENERATOR,
			XUMachineGenerators.FURNACE_GENERATOR,
			XUMachineGenerators.ICE_GENERATOR,
			XUMachineGenerators.LAVA_GENERATOR,
			XUMachineGenerators.NETHERSTAR_GENERATOR,
			XUMachineGenerators.OVERCLOCK_GENERATOR,
			XUMachineGenerators.PINK_GENERATOR,
			XUMachineGenerators.POTION_GENERATOR,
			XUMachineGenerators.REDSTONE_GENERATOR,
			XUMachineGenerators.SLIME_GENERATOR,
			XUMachineGenerators.SURVIVALIST_GENERATOR,
			XUMachineGenerators.TNT_GENERATOR};
	private static final ResourceLocation rainbowGenerators = new ResourceLocation(ExtraUtils2.MODID, "rainbowGenerators");
	static final Collection<ResourceLocation> rainbowGeneratorsCollection = ImmutableList.of(rainbowGenerators);

	static {
		MinecraftForge.EVENT_BUS.register(TileRainbowGenerator.class);
	}

	public boolean providing;
	private int extractBuffer = 0;
	public IEnergyStorage INFINISTORAGE = new IEnergyStorage() {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if (!providing || maxExtract <= 0) return 0;
			int toExtract = Math.min(maxExtract, extractBuffer);
			if (!simulate) {
				extractBuffer -= toExtract;
			}
			return toExtract;
		}

		@Override
		public int getEnergyStored() {
			return providing ? perTick : 0;
		}

		@Override
		public int getMaxEnergyStored() {
			return perTick;
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return false;
		}
	};

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) return true;

		PowerManager.PowerFreq freq = PowerManager.instance.getPowerFreqRaw(frequency);
		Collection<IPower> s;
		if (freq == null || ((s = freq.getSubTypes(rainbowGenerators)) == null)) {
			return true;
		}

		int n = 1;
		for (IPower iPower : s) {
			if (iPower == this) {
				break;
			} else {
				n++;
			}
		}


		ArrayList<Machine> missingGenerators = new ArrayList<>();
		ArrayList<Machine> unPoweredGenerators = new ArrayList<>();
		TObjectIntHashMap<Machine> numMissingGenerators = new TObjectIntHashMap<>();

		mainLoop:
		for (Machine generator : GENERATORS) {
			Collection<TileMachine> subTypes = freq.getSubTypes(generator.location);
			if (subTypes == null) {
				missingGenerators.add(generator);
			} else {
				int k = 0;
				for (TileMachine subType : subTypes) {
					if (subType.isProcessing()) {
						k++;
						if (k >= n)
							continue mainLoop;
					}
				}
				numMissingGenerators.put(generator, n - k);
				unPoweredGenerators.add(generator);
			}
		}

		if (missingGenerators.isEmpty() && unPoweredGenerators.isEmpty()) {
			SpecialChat.sendChat(playerIn, Lang.chat("The Generator Spectrum is complete!").appendText("\n").appendSibling(Lang.chat("Sending %s RF energy per tick", StringHelper.format(perTick))));
		} else {
			BlockMachine blockMachine = XU2Entries.machineEntry.value;
			ITextComponent textComponent = null;
			if (!missingGenerators.isEmpty()) {
				textComponent = Lang.chat("%s generators are missing:", missingGenerators.size()).appendText("\n");
				for (int i = 0; i < missingGenerators.size(); i++) {
					Machine missingGenerator = missingGenerators.get(i);
					if (i > 0) textComponent = textComponent.appendText("\n-");
					textComponent = textComponent.appendText("-").appendSibling(blockMachine.createStack(missingGenerator).getTextComponent());
				}
			}

			if (!unPoweredGenerators.isEmpty()) {
				ITextComponent t = Lang.chat("%s generators are not running:", unPoweredGenerators.size()).appendText("\n");
				if (textComponent == null)
					textComponent = t;
				else
					textComponent = textComponent.appendText("\n").appendSibling(t);

				for (int i = 0; i < unPoweredGenerators.size(); i++) {
					Machine unPoweredGenerator = unPoweredGenerators.get(i);
					if (i > 0) textComponent = textComponent.appendText("\n");
					textComponent = textComponent.appendText("-").appendSibling(blockMachine.createStack(unPoweredGenerator).getTextComponent());
					if (n > 1) {
						textComponent = textComponent.appendText(" (x").appendText(Integer.toString(numMissingGenerators.get(unPoweredGenerator))).appendText(")");
					}
				}
			}

			SpecialChat.sendChat(playerIn, textComponent);
		}


		return true;
	}

	@Override
	public float getPower() {
		return Float.NaN;
	}

	@Override
	public void onPowerChanged() {

	}

	@Override
	public void update() {
		if (world.isRemote) return;
		extractBuffer = 0;
		if (!active) {
			if (providing) markForUpdate();
			providing = false;
		} else {
			PowerManager.PowerFreq freq = PowerManager.instance.getPowerFreqRaw(frequency);
			Collection<IPower> s;
			if (freq == null || ((s = freq.getSubTypes(rainbowGenerators)) == null)) {
				if (providing) markForUpdate();
				providing = false;
				return;
			}


			int n = 1;
			for (IPower iPower : s) {
				if (iPower == this) {
					break;
				} else {
					n++;
				}
			}


			for (Machine generator : GENERATORS) {
				Collection<IPower> subTypes = freq.getSubTypes(generator.location);
				if (subTypes == null || (subTypes.size() < n)) {
					if (providing) markForUpdate();
					providing = false;
					return;
				}
			}

			mainLoop:
			for (Machine generator : GENERATORS) {
				Collection<TileMachine> subTypes = freq.getSubTypes(generator.location);
				int k = 0;
				if (subTypes != null) {
					for (TileMachine subType : subTypes) {
						if (subType.isProcessing()) {
							k++;
							if (k >= n)
								continue mainLoop;
						}
					}
				}
				if (providing) markForUpdate();
				providing = false;
				return;
			}

			if (!providing) {
				markForUpdate();
			}

			providing = true;

			extractBuffer = perTick;

			mainLoop:
			for (int pass = 0; pass < 2; pass++) {
				for (EnumFacing facing : FacingHelper.randOrders[world.rand.nextInt(12)]) {
					TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
					if (tileEntity != null) {
						IEnergyStorage storage = CapGetter.energyReceiver.getInterface(tileEntity, facing.getOpposite());
						if (storage != null && storage.canReceive()) {
							if (pass == 0) {
								int i = storage.receiveEnergy(extractBuffer, true);
								extractBuffer -= storage.receiveEnergy((i >> 2) | 1, false);
							} else {
								extractBuffer -= storage.receiveEnergy(extractBuffer, false);
							}
							if (extractBuffer <= 0) {
								extractBuffer = 0;
								break mainLoop;
							}
						}
					}
				}
			}
		}
	}

	@Nullable
	@Override
	public IEnergyStorage getEnergyHandler(EnumFacing facing) {
		return INFINISTORAGE;
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		super.addToDescriptionPacket(packet);
		packet.writeBoolean(providing);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		super.handleDescriptionPacket(packet);
		providing = packet.readBoolean();
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1 && providing;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isGlobalRenderer() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void render(IBlockAccess world, BlockPos pos, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer renderer, BlockRendererDispatcher blockRenderer) {
		if (providing) {
			Vector3f[] vecs = new Vector3f[]{
					new Vector3f(1, 0, 0),
					new Vector3f(0, 1, 0),
					new Vector3f(0, 0, 1),
					new Vector3f(1, 0, 0),
					new Vector3f(0, 1, 0),
					new Vector3f(0, 0, 1),
			};

			Random rand = new Random(425L + pos.hashCode());

			Matrix4f matrix = new Matrix4f();
			matrix.setIdentity();

			Vector4f b = new Vector4f();
			Vector4f c = new Vector4f();
			Vector4f d = new Vector4f();


			for (int i = 0; i < 32; i++) {
				for (Vector3f vec : vecs) {
					QuadHelper.rotate((float) Math.PI * 2 * rand.nextFloat() + MCTimer.renderTimer / 360, vec, matrix, matrix);
				}

				float r = (1F + rand.nextFloat() * 2.5F) * 5;

				QuadHelper.rotate(MCTimer.renderTimer / 180, new Vector3f(0, 1, 0), matrix, matrix);

				b.set(0F, 0.126F * r, 0.5F * r, 1);
				c.set(0F, -0.126F * r, 0.5F * r, 1);
				d.set(0F, 0, 0.6F * r, 1);

				matrix.transform(b);
				matrix.transform(c);
				matrix.transform(d);

				int rgb = Color.HSBtoRGB(i / 16F, 1, 1);
				float col_r = ColorHelper.getRF(rgb);
				float col_g = ColorHelper.getGF(rgb);
				float col_b = ColorHelper.getBF(rgb);

				renderer.pos(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F).color(col_r, col_g, col_b, 0.9F).endVertex();
				renderer.pos(pos.getX() + 0.5F + b.x, pos.getY() + 0.5F + b.y, pos.getZ() + 0.5F + b.z).color(col_r, col_g, col_b, 0.01F).endVertex();
				renderer.pos(pos.getX() + 0.5F + d.x, pos.getY() + 0.5F + d.y, pos.getZ() + 0.5F + d.z).color(col_r, col_g, col_b, 0.01F).endVertex();
				renderer.pos(pos.getX() + 0.5F + c.x, pos.getY() + 0.5F + c.y, pos.getZ() + 0.5F + c.z).color(col_r, col_g, col_b, 0.01F).endVertex();
//				Tessellator.getInstance().draw();
//				renderer.begin(getDrawMode(), getVertexFormat());
			}
		}
	}

	@Override
	public void preRender(int destroyStage) {
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.depthMask(false);
	}

	@Override
	public void postRender(int destroyStage) {
		GlStateManager.enableTexture2D();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableAlpha();
		RenderHelper.enableStandardItemLighting();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getDrawMode() {
		return GL11.GL_QUADS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public VertexFormat getVertexFormat() {
		return DefaultVertexFormats.POSITION_COLOR;
	}

	@Override
	public Collection<ResourceLocation> getTypes() {
		return rainbowGeneratorsCollection;
	}
}
