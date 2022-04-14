package net.wurstclient.forge.features;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.compatibility.WPlayerController;
import net.wurstclient.forge.utils.KeyBindingUtils;
import net.minecraft.util.math.BlockPos;


public class AutoEat extends Feature {

	private int oldSlot = -1;

	public AutoEat() {
		super("AutoEat", 
			  "Automatically eats the best food\n" +
			   "in your hotbar when possible.",
			  false);
		setCategory(Category.UTILITY);
	}
	
	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		EntityPlayerSP player = event.getPlayer();
		if (!shouldEat(player)) {
			if (isEating())
				stopEating(player);
			return;
		}
		
		if (isEating())
			return;
		
		int bestSlot = getBestFoodSlot();
		if (bestSlot == -1)
			return;
		
		oldSlot = WMinecraft.getPlayer().inventory.currentItem;
		WMinecraft.getPlayer().inventory.currentItem = bestSlot;
		KeyBindingUtils.setPressed(mc.gameSettings.keyBindUseItem, true);
		WPlayerController.processRightClick();
	}
	
	private boolean shouldEat(EntityPlayerSP player) {
		
		if (!player.canEat(false))
			return false;
		
		if (mc.currentScreen == null && mc.objectMouseOver != null) {
			Entity entity = mc.objectMouseOver.entityHit;
			if (entity instanceof EntityVillager || entity instanceof EntityTameable)
				return false;
			
			BlockPos pos = mc.objectMouseOver.getBlockPos();
			if (pos != null) {
				Block block = WMinecraft.getWorld().getBlockState(pos).getBlock();
				if (block instanceof BlockContainer || block instanceof BlockWorkbench)
					return false;
			}
		}
		
		return true;
	}
	
	public boolean isEating() {
		return oldSlot != -1;
	}
	
	private void stopEating(EntityPlayerSP player) {
		KeyBindingUtils.setPressed(mc.gameSettings.keyBindUseItem, false);
		WMinecraft.getPlayer().inventory.currentItem = oldSlot;
		oldSlot = -1;
	}
	
	private int getBestFoodSlot() {
		int bestSlot = -1;
		float bestSaturation = -1;
		for (int i = 0; i < 9; i++) {
			ItemStack stack = WMinecraft.getPlayer().inventory.getStackInSlot(i);
			if (stack == null || !(stack.getItem() instanceof ItemFood))
				continue;
			
			float saturation = ((ItemFood)stack.getItem()).getSaturationModifier(stack);
			if (saturation > bestSaturation) {
				bestSaturation = saturation;
				bestSlot = i;
			}
		}
		return bestSlot;
	}
	
}