package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.backend.model.Textures;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;

public interface IXUItemTexture extends IXUItem {
	@Override
	default void registerTextures(){
		for (int i = 0; i < getMaxMetadata(); i++) {
			Textures.register(getTexture(i));
		}
	}

	String getTexture(int i);

	@Override
	default IBakedModel createModel(int metadata){
		return new PassthruModelItem(this);
	}

	@Override
	default TextureAtlasSprite getBaseTexture(){
		return Textures.getSprite(getTexture(0));
	}

	@Override
	default void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack){
		model.addSprite(Textures.getSprite(getTexture(stack.getMetadata())));
	}

	@Override
	default void postTextureRegister(){

	}

	@Override
	default void clearCaches(){

	}

	@Override
	default boolean allowOverride(){
		return true;
	}

	@Override
	int getMaxMetadata();
}
