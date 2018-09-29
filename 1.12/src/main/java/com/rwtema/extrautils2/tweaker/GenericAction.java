package com.rwtema.extrautils2.tweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;

public class GenericAction implements IAction {
	final Runnable runnable;
	final String description;

	public GenericAction(Runnable runnable, String description) {
		this.runnable = runnable;
		this.description = description;
	}

	public static void run(Runnable runnable, String description) {
		CraftTweakerAPI.apply(new GenericAction(runnable, description));
	}

	@Override
	public void apply() {
		runnable.run();
	}

	@Override
	public String describe() {
		return description;
	}
}
