package net.wurstclient.forge.features;

import java.lang.reflect.Field;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.compatibility.WPlayer;
import net.wurstclient.forge.compatibility.WPlayerController;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.utils.InventoryUtils;
import net.wurstclient.forge.utils.KeyBindingUtils;
import net.wurstclient.forge.utils.RotationUtils;

public class AutoShear extends Feature {

	private CheckboxSetting autoSelectShears = new CheckboxSetting("Auto select shears", false);
	
	public AutoShear() {
		super("AutoShear", "Locates and shears sheep automatically", true);
		setCategory(Category.UTILITY);
		addSetting(autoSelectShears);
	}
	
	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
		KeyBindingUtils.resetPressed(mc.gameSettings.keyBindForward);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		EntityPlayerSP player = event.getPlayer();
		if (!autoSelectShears.isChecked() && !(player.inventory.getCurrentItem().getItem() instanceof ItemShears)) {
			KeyBindingUtils.resetPressed(mc.gameSettings.keyBindForward);
			return;
		}
		
		if (autoSelectShears.isChecked()) {
			int shearSlot = getShearSlot(player);
			if (shearSlot == -1)
				return;
			if (shearSlot != player.inventory.currentItem)
				player.inventory.currentItem = shearSlot;
		}
		
		World world = WPlayer.getWorld(player);
		EntityItem nearestWoolDrop = getNearestWoolDrop(player, world);
		if (nearestWoolDrop != null) {
			double distance = Math.sqrt(Math.pow(player.posX - nearestWoolDrop.posX, 2) + Math.pow(player.posZ - nearestWoolDrop.posZ, 2));
			if (distance < .2)
				KeyBindingUtils.resetPressed(mc.gameSettings.keyBindForward);
			else 
				moveTo(player, nearestWoolDrop);
			return;
		}
		
		EntitySheep nearestSheep = getNearestSheep(player, world);
		if (nearestSheep != null) {
			double distance = Math.sqrt(Math.pow(player.posX - nearestSheep.posX, 2) + Math.pow(player.posZ - nearestSheep.posZ, 2));
			if (distance < 1) {
				KeyBindingUtils.resetPressed(mc.gameSettings.keyBindForward);
				shear(player, nearestSheep);
			} else {
				moveTo(player, nearestSheep);
			}
			return;
		}
		
		KeyBindingUtils.resetPressed(mc.gameSettings.keyBindForward);
	}
	
	private int getShearSlot(EntityPlayerSP player) {
		int bestSlot = -1;
		float bestScore = -1;
		for (int i = 0; i < 9; ++i) {
			ItemStack itemStack = player.inventory.getStackInSlot(i);
			if (itemStack.getItem() instanceof ItemShears) {
				float score = 0; 
				score += EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemStack);
				score += EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, itemStack) * 0.1;
				score += EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, itemStack) * 0.01;
				if (score > bestScore) {
					bestScore = score;
					bestSlot = i;
				}
			}
		}
		return bestSlot;
	}
	
	private EntityItem getNearestWoolDrop(EntityPlayerSP player, World world) {
		double minDistance = Double.MAX_VALUE;
		EntityItem closestWoolDrop = null;
		
		for (Entity entity : world.loadedEntityList) {
			if (!(entity instanceof EntityItem))
				continue;
			
			EntityItem entityItem = (EntityItem) entity;
			ItemStack stack = entityItem.getItem();
			if (InventoryUtils.getId(stack) == 35 && entityItem.posY == player.posY) {
				double distance = Math.sqrt(Math.pow(player.posX - entityItem.posX, 2) + Math.pow(player.posZ - entityItem.posZ, 2));
				if (distance < minDistance) {	
					minDistance = distance;
					closestWoolDrop = entityItem;
				}	
			}
		}
		return closestWoolDrop;
	}
	
	private EntitySheep getNearestSheep(EntityPlayerSP player, World world) {
		double minDistance = Double.MAX_VALUE;
		EntitySheep closestSheep = null;
		
		for (Entity entity : world.loadedEntityList) {
			if (!(entity instanceof EntitySheep) || ((EntityAgeable) entity).isChild())
				continue;
			
			EntitySheep sheep = (EntitySheep) entity;
			if (!sheep.getSheared() && sheep.posY == player.posY) {
				double distance = Math.sqrt(Math.pow(player.posX - sheep.posX, 2) + Math.pow(player.posZ - sheep.posZ, 2));
				if (distance < minDistance) {	
					minDistance = distance;
					closestSheep = sheep;
				}
			}
		}
		return closestSheep;
	}
	
	public void moveTo(EntityPlayerSP player, Entity entity) {
		Vec3d vec = entity.getEntityBoundingBox().getCenter();
		boolean yLock = entity instanceof EntityItem;
		RotationUtils.faceVectorForWalking(vec, yLock);
		KeyBindingUtils.setPressed(mc.gameSettings.keyBindForward, true);
	}
	
	private void shear(EntityPlayerSP player, EntitySheep sheep) {
		
		try {
			Field rightClickDelayTimer = mc.getClass().getDeclaredField(
					wurst.isObfuscated() ? "field_71467_ac" : "rightClickDelayTimer");
			rightClickDelayTimer.setAccessible(true);
			if (rightClickDelayTimer.getInt(mc) > 0)
				return;
		} catch (ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
		
		WPlayerController.interactWithEntity(player, sheep);

		try {
			Field rightClickDelayTimer = mc.getClass().getDeclaredField(
					wurst.isObfuscated() ? "field_71467_ac" : "rightClickDelayTimer");
			rightClickDelayTimer.setAccessible(true);
			rightClickDelayTimer.setInt(mc, 4);	
		} catch (ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}
}