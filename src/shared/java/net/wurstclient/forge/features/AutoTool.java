package net.wurstclient.forge.features;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
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
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.utils.BlockUtils;

public class AutoTool extends Feature {
	
	private final CheckboxSetting useSwords = 
		new CheckboxSetting("Use swords",
							 "Uses swords to break leaves,\n" +
							 "cobwebs, etc.", 
							false);
	private final CheckboxSetting useHands = 
		new CheckboxSetting("Use hands",
							"Uses an empty hand or a non-\n" +
							 "damageable item when no\n" +
							 "applicable tool is found.",
							true);
	private final CheckboxSetting repairMode = 
		new CheckboxSetting("Repair mode", 
							"Won't use tools that are about\n" +
							 "to break.",
							false);
	private final EnumSetting<Enchantment> enchantmentPreference = new EnumSetting<>("Enchantment preference", Enchantment.values(), Enchantment.SILK_TOUCH);
	
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
		addSetting(enchantmentPreference);
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
				double factor = enchantmentPreference.getSelected() == Enchantment.SILK_TOUCH ? .1: .01;
				score += enchantLevel * factor;
			}
			if (BlockUtils.canFortuneHarvest(pos)) {
				int enchantLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
				double factor = enchantmentPreference.getSelected() == Enchantment.FORTUNE ? .1: .01;
				score += enchantLevel * factor;
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
	
	private enum Enchantment {
		
		SILK_TOUCH("Silk Touch"),
		FORTUNE("Fortune");
		
		private final String name;
		
		private Enchantment(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
}