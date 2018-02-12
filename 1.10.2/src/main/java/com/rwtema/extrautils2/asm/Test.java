package com.rwtema.extrautils2.asm;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.client.GLState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class Test extends Chunk {
	public Test(World worldIn, int x, int z) {
		super(worldIn, x, z);
	}

	private World worldObj;

	public int getLightSubtracted(BlockPos pos, int amount) {
		return Lighting.getCombinedLight(worldObj, pos, getLightSubtractedPassThru(pos, amount));
	}

	public int getLightSubtractedPassThru(BlockPos pos, int amount) {
		return 0;
	}

	public void run(){
		GLState.resetStateQuads();
	}

	public void test(){
		ItemStack stack = new ItemStack(Items.COMMAND_BLOCK_MINECART);
		StackHelper.increase(stack);
	}
}
