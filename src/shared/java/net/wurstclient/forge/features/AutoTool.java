package net.wurstclient.forge.features;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPlayerDamageBlockEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.KeyBindingUtils;

public class AutoTool extends Feature {
	
	private CheckboxSetting useSwords = 
		new CheckboxSetting("Use swords",
							 "Uses swords to break leaves,\n" +
							 "cobwebs, etc.", 
							false);
	private CheckboxSetting useHands = 
		new CheckboxSetting("Use hands",
							"Uses an empty hand or a non-\n" +
							 "damageable item when no\n" +
							 "applicable tool is found.",
							true);
	private CheckboxSetting repairMode = 
		new CheckboxSetting("Repair mode", 
							"Won't use tools that are about\n" +
							 "to break.",
							false);
	
	private int oldSlot;
	private boolean mining;
	private long timeOfLastMine;
	
	public AutoTool() {
		super("AutoTool",
			  "Automatically equips the fastest\n" +
			   "applicable tool in your hotbar\n" +
			   "when you try to break a block.",
			  false);
		setCategory(Category.UTILITY);
		addSetting(useSwords);
		addSetting(useHands);
		addSetting(repairMode);
	}
	
	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
		mining = false;
	}
	
	@SubscribeEvent
	public void onPlayerDamageBlock(WPlayerDamageBlockEvent event) {
		equipBestTool(event.getPos(), useSwords.isChecked(), useHands.isChecked(), repairMode.isChecked());
	} 
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		EntityPlayerSP player = WMinecraft.getPlayer();
		if (mining && Minecraft.getSystemTime() - timeOfLastMine > 100) {
			mining = false;
			player.inventory.currentItem = oldSlot;
		}
	}
	
	
	public void equipBestTool(BlockPos pos, boolean useSwords, boolean useHands, boolean repairMode) {
		
		EntityPlayerSP player = WMinecraft.getPlayer();
		if (player.capabilities.isCreativeMode)
			return;
		AutoEat autoEat = wurst.getFeatures().autoEat;
		if (autoEat.isEnabled() && autoEat.isEating())
			return;
		Killaura killaura = wurst.getFeatures().killaura;
		if (killaura.isEnabled() && killaura.getTarget(player) != null)
			return;
		
		timeOfLastMine = Minecraft.getSystemTime();
		InventoryPlayer inventory = player.inventory;
		IBlockState state = BlockUtils.getState(pos);
		float bestScore = 1;
		int bestSlot = inventory.currentItem;

		if (!mining) {
			oldSlot = inventory.currentItem;
			mining = true;
		}
		
		for (int i = 0; i < 9; ++i) {
			
			int slot = (inventory.currentItem + i) % 9;
			ItemStack stack = inventory.getStackInSlot(slot);
			float score = getDestroySpeed(stack, state);
			
			// apply silk touch and fortune scores
			if (BlockUtils.canSilkHarvest(pos)) {
				int enchantLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack);
				score += enchantLevel * 0.01;
			}
			if (BlockUtils.canFortuneHarvest(pos)) {
				int enchantLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
				score += enchantLevel * .004;
			}
			
			// if the item is a tool, too damaged, and not ready to be repaired, do not switch to it
			if (isDamageable(stack) && isTooDamaged(stack, repairMode)) {
				AutoRepair autoRepair = wurst.getFeatures().autoRepair;
				if (!(autoRepair.isEnabled() && !autoRepair.isSupressed() && autoRepair.canRepair()))
					score = 0;
			}
			
			// prioritize non-damageabale stacks over damageable stacks
			if (!isDamageable(stack)) {
				score += .0001;
			}
			
			if (score <= bestScore)
				continue;
			else if (!useSwords && stack.getItem() instanceof ItemSword)
				continue;
			
			bestScore = score;
			bestSlot = slot;
		}
		
		if (bestSlot != -1)
			inventory.currentItem = bestSlot;
	}
	
	private float getDestroySpeed(ItemStack stack, IBlockState state) {
		float speed = WItem.getDestroySpeed(stack, state);
		if (speed > 1) {
			int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
			return speed + efficiency * efficiency;
		} else {
			return speed;
		}
	}
	
	private boolean isDamageable(ItemStack stack) {
		return !WItem.isNullOrEmpty(stack) && stack.getItem().isDamageable();
	}
	
	private boolean isTooDamaged(ItemStack stack, boolean repairMode) {
		double durability = 1 - (double) stack.getItemDamage() / stack.getMaxDamage();
		return repairMode && durability <= .05;
	}
	
}