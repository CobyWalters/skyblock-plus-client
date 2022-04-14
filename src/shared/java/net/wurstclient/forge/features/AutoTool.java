package net.wurstclient.forge.features;

import net.minecraft.block.state.IBlockState;
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
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.utils.BlockUtils;

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
	}
	
	@SubscribeEvent
	public void onPlayerDamageBlock(WPlayerDamageBlockEvent event) {
		equipBestTool(event.getPos(), useSwords.isChecked(), useHands.isChecked(), repairMode.isChecked());
	}
	
	public void equipBestTool(BlockPos pos, boolean useSwords, boolean useHands, 
							  boolean repairMode) {
		EntityPlayer player = WMinecraft.getPlayer();
		if (player.capabilities.isCreativeMode)
			return;
		
		InventoryPlayer inventory = player.inventory;
		IBlockState state = BlockUtils.getState(pos);
		ItemStack heldItem = player.getHeldItemMainhand();
		float bestScore = getDestroySpeed(heldItem, state);
		int fallbackSlot = -1;
		int bestSlot = -1;
		
		for (int slot = 0; slot < 9; slot++) {
			
			ItemStack stack = inventory.getStackInSlot(slot);
			if (fallbackSlot == -1 && !isDamageable(stack))
				fallbackSlot = slot;
			
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
			
			if (score <= bestScore)
				continue;
			else if (!useSwords && stack.getItem() instanceof ItemSword)
				continue;
			else if (isTooDamaged(stack, repairMode))
				continue;
			
			bestScore = score;
			bestSlot = slot;
		}
		
		boolean useFallback = isDamageable(heldItem) && 
							  		(isTooDamaged(heldItem, repairMode) ||
							  		 useHands && getDestroySpeed(heldItem, state) <= 1);
		
		if (bestSlot != -1)
			inventory.currentItem = bestSlot;
		else if (useFallback && fallbackSlot != -1)
			inventory.currentItem = fallbackSlot;
		/*else if (isTooDamaged(heldItem, repairMode))
			inventory.currentItem = (inventory.currentItem + 1) % 9;*/
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
		return repairMode && stack.getMaxDamage() - stack.getItemDamage() <= 4;
	}
	
}