package net.wurstclient.forge.features;

import java.lang.reflect.Field;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.InventoryUtils;
import net.wurstclient.forge.utils.PlayerControllerUtils;
import net.wurstclient.forge.utils.RotationUtils;

public class AutoMine extends Feature {
	
	private BlockPos currentBlock;
	
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
		if (currentBlock != null) {
			try {
				PlayerControllerUtils.setIsHittingBlock(true);
				mc.playerController.resetBlockRemoving();
				currentBlock = null;
			} catch(ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		
		EntityPlayerSP player = event.getPlayer();
		Vec3d eyesPos = RotationUtils.getEyesPos().subtract(0.5, 0.5, 0.5);
		double blockRange = 6;
		double rangeSq = Math.pow(blockRange, 2);
		RayTraceResult r = mc.objectMouseOver;
		if (r == null)
			return;
		BlockPos lookingAt = r.getBlockPos();
		
		if (isAttackKeybindPressed())
			return;
		else if (isToolAboutToBreak(event))
			return;
		else if (lookingAt == null)
			return;
		else if (!BlockUtils.canBeClicked(lookingAt))
			return;
		else if (eyesPos.squareDistanceTo(new Vec3d(lookingAt)) > rangeSq)
			return;
	
		if (player.capabilities.isCreativeMode)
			mc.playerController.resetBlockRemoving();
		
		boolean breaking = BlockUtils.breakBlockSimple(lookingAt);
		if (!breaking)
			mc.playerController.resetBlockRemoving();
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
	
	private boolean isAttackKeybindPressed() {
		try {
			Field pressed = mc.gameSettings.keyBindAttack.getClass().getDeclaredField(
				wurst.isObfuscated() ? "field_74513_e" : "pressed");
			pressed.setAccessible(true);
			return pressed.getBoolean(mc.gameSettings.keyBindAttack);
		} catch(ReflectiveOperationException e) {
			setEnabled(false);
			throw new RuntimeException(e);
		}
	}
	
}