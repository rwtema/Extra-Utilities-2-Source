package com.rwtema.extrautils2.sounds;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundMuffler implements ISound {
	final ISound original;
	final float volModifier;

	public SoundMuffler(ISound original, float volModifier) {
		this.original = original;
		this.volModifier = volModifier;
	}


	@Nonnull
	@Override
	public Sound getSound() {
		return original.getSound();
	}

	@Nonnull
	@Override
	public SoundCategory getCategory() {
		return original.getCategory();
	}

	@Nonnull
	@Override
	public ResourceLocation getSoundLocation() {
		return original.getSoundLocation();
	}

	@Nullable
	@Override
	public SoundEventAccessor createAccessor(@Nonnull SoundHandler handler) {
		return original.createAccessor(handler);
	}

	@Override
	public boolean canRepeat() {
		return original.canRepeat();
	}

	@Override
	public int getRepeatDelay() {
		return original.getRepeatDelay();
	}

	@Override
	public float getVolume() {
		return original.getVolume() * volModifier;
	}

	@Override
	public float getPitch() {
		return original.getPitch();
	}

	@Override
	public float getXPosF() {
		return original.getXPosF();
	}

	@Override
	public float getYPosF() {
		return original.getYPosF();
	}

	@Override
	public float getZPosF() {
		return original.getZPosF();
	}

	@Nonnull
	@Override
	public AttenuationType getAttenuationType() {
		return original.getAttenuationType();
	}
}
