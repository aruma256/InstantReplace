package com.github.aruma256.instantreplace;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ReplaceKey extends KeyMapping implements ClientTickEvents.EndTick {

	public ReplaceKey(String description, int keyCode, String category) {
		super(description, keyCode, category);
	}

	@Override
	public void onEndTick(Minecraft client) {
		if (this.isDown()) {
			this.execReplace();
		}
	}

	private void execReplace() {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.player.isCreative()) {
			return;
		}
		HitResult target = mc.getCameraEntity().pick(255d, mc.getDeltaFrameTime(), false);
		if (target == null || target.getType() != HitResult.Type.BLOCK){
        	return;
        }
		BlockPos pos = ((BlockHitResult)target).getBlockPos();
		BlockState state = mc.level.getBlockState(pos);
		if (state.isAir())
		{
			return;
		}
		ItemStack itemStack = mc.player.getInventory().getSelected();
		Block block = Block.byItem(itemStack.getItem());
		if (itemStack.isEmpty() || block == Blocks.AIR) {
			return;
		}
		BlockState newBlockState = block.getStateForPlacement(new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND, itemStack, (BlockHitResult)target));
		if (state.getBlock() == newBlockState.getBlock()) return;
		mc.player.connection.sendCommand(toSetBlockCommand(pos.getX(), pos.getY(), pos.getZ(), newBlockState));
	}

	private static String toSetBlockCommand(int x, int y, int z, BlockState blockState) {
		String blockName = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
		return String.format("setblock %d %d %d %s", x, y, z, blockName);
	}

}
