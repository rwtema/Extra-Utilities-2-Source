package com.rwtema.extrautils2.resources;

import com.rwtema.extrautils2.ExtraUtils2;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import com.rwtema.extrautils2.resources.ResourcePackHelper;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;

import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RedirectResourcePack implements IResourcePack {

	protected final String name;
	IResourcePack xuPack;
	public final HashSet<String> domains;
	public String prefix;

	public RedirectResourcePack(String name, HashSet<String> domains, String folderPrefix) {
		this.name = name.toLowerCase();
		this.xuPack = FMLClientHandler.instance().getResourcePackFor(ExtraUtils2.MODID);
		this.domains = domains;
		this.prefix = folderPrefix;
	}


	@Nonnull
	@Override
	public InputStream getInputStream(@Nonnull ResourceLocation location) throws IOException {
		return xuPack.getInputStream(new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, location.getResourcePath()));
	}

	@Override
	public boolean resourceExists(@Nonnull ResourceLocation location) {
		String resourcePath = location.getResourcePath();
		return resourcePath.startsWith(prefix) && xuPack.resourceExists(new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, resourcePath));
	}

	@Nonnull
	@Override
	public Set<String> getResourceDomains() {

		return domains;
	}

	@Nonnull
	@Override
	public <T extends IMetadataSection> T getPackMetadata(@Nonnull MetadataSerializer metadataSerializer, @Nonnull String metadataSectionName) throws IOException {
		return null;
	}

	@Nonnull
	@Override
	public BufferedImage getPackImage() throws IOException {
		throw new IOException();
	}

	@Nonnull
	@Override
	public String getPackName() {
		return "ExtraUtils2_Additional";
	}


	public void register() {
		List<IResourcePack> packs = ResourcePackHelper.getiResourcePacks();
		packs.add(this);
	}
}
