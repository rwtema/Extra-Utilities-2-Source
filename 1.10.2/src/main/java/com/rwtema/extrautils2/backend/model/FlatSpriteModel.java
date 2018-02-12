package com.rwtema.extrautils2.backend.model;

public class FlatSpriteModel extends BoxModel {
	Box box;

	{
		box = addBox(0, 0, 0, 1, 1, 1);
		for (int i = 1; i < box.invisible.length; i++) {
			box.invisible[i] = true;
		}
		box.setTexture(Box.MISSING_TEXTURE);
	}


}
