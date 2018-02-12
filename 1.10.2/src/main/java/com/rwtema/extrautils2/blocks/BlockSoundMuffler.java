package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockFull;
import com.rwtema.extrautils2.sounds.SoundMuffler;
import com.rwtema.extrautils2.tile.TileSoundMuffler;
import com.rwtema.extrautils2.tile.XUTile;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// 🔇 Sound Muffler 🔇
public class BlockSoundMuffler extends XUBlockFull {
	public BlockSoundMuffler() {
		super(Material.CLOTH);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public String getTexture(IBlockState state, EnumFacing side) {
		return "sound_muffler";
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileSoundMuffler();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void supressSound(PlaySoundEvent event) {
		WorldClient theWorld = Minecraft.getMinecraft().world;
		if (theWorld == null) return;
		ISound sound = event.getSound();
		if (sound instanceof ITickableSound) return;

		AxisAlignedBB expand = new AxisAlignedBB(sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), sound.getXPosF(), sound.getYPosF(), sound.getZPosF()).grow(8, 8, 8);

		List<TileSoundMuffler> tileSoundMufflers = XUTile.searchAABBForTiles(theWorld, expand, TileSoundMuffler.class, true, null);
		if(tileSoundMufflers.isEmpty()) return;

		float volume = 0.05F;

		event.setResultSound(new SoundMuffler(sound, volume));
	}
}
