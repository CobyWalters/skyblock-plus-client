package net.wurstclient.forge.features;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.utils.InventoryUtils;
import net.wurstclient.forge.utils.KeyBindingUtils;

public class AutoMine extends Feature {
	
	public AutoMine() {
		super("AutoMine", 
			  "Mines automatically.\n" +
			   "Start mining to activate.",
			  true);
		setCategory(Category.UTILITY);
	}
	
	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
		KeyBindingUtils.setPressed(mc.gameSettings.keyBindAttack, false);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {

		if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null) {
			return;
		}
		
		if (mc.gameSettings.keyBindAttack.isPressed() && !mc.playerController.getIsHittingBlock()) {
			KeyBindingUtils.setPressed(mc.gameSettings.keyBindAttack, false);
		} else if (isToolAboutToBreak(event)) {
			KeyBindingUtils.setPressed(mc.gameSettings.keyBindAttack, false);
		} else {
			RayTraceResult lookingAt = mc.objectMouseOver;
			KeyBindingUtils.setPressed(mc.gameSettings.keyBindAttack, lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK);
		}
	}
	
	private boolean isToolAboutToBreak(WUpdateEvent event) {
		ItemStack currentStack = event.getPlayer().inventory.getCurrentItem();
		if (InventoryUtils.isTool(currentStack)) {
			double durability = 1 - currentStack.getItem().getDurabilityForDisplay(currentStack);
			if (durability <= 0.05)
				return true;
		}
		return false;
	}
	
}