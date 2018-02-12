package com.rwtema.extrautils2.quarry;

import com.rwtema.extrautils2.ExtraUtils2;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelDiggerMite extends ModelBase {
	private final ResourceLocation location = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER + ":textures/entity/miner.png");
	private final ModelRenderer helmet;
	private final ModelRenderer helmetBrim;
	private final ModelRenderer mite;

	public ModelDiggerMite() {
		this.textureWidth = 16;
		this.textureHeight = 8;
		this.helmet = new ModelRenderer(this, 0, 0);
		this.helmet.addBox(-1, 0, -1, 2, 2, 2);
		this.helmetBrim = new ModelRenderer(this, 8, 0);
		this.helmetBrim.addBox(-1, 0, -1, 2, 1, 2, 1.25F);
		this.mite = new ModelRenderer(this, 0, 4);
		this.mite.addBox(-1, -2, -1, 2, 2, 2);
	}


	public void render(float scale) {

		helmet.render(scale);
		helmetBrim.render(scale);
		mite.render(scale);
	}
}
