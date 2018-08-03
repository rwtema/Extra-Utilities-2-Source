package com.rwtema.extrautils2.modcompat;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.entries.VoidEntry;
import com.rwtema.extrautils2.utils.LogHelper;
import slimeknights.tconstruct.TConstruct;

@ModCompatibility(mod = TConstruct.modID)
public class TinkersIntegrationEntry extends VoidEntry {

	public TinkersIntegrationEntry() {
		super("tinkers_construct_integration");
	}

	@Override
	public void preInitLoad() {
		try {
			TinkersIntegration.createObjects();
		} catch (Throwable error) {
			LogHelper.logger.error("Unable to init Tinkers Integration. The API may have changed.", error);
			if (ExtraUtils2.deobf) {
				throw error;
			} else {
				enabled = false;
			}
			return;
		}
		TinkersIntegration.doRegister();
	}

	@Override
	public void init() {
		TinkersIntegration.init();
	}
}
