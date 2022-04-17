package net.wurstclient.forge.features;

import java.lang.reflect.Field;

import com.google.common.collect.Iterables;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockStem;
import net.minecraft.block.IGrowable;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.compatibility.WBlock;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.BlockUtils;

public class BonemealAura extends Feature {
	
	private CheckboxSetting autoSelectBonemeal = new CheckboxSetting("Auto select bonemeal", false);
	private SliderSetting range = new SliderSetting("Range", 4.25, 1, 6, 0.05, ValueDisplay.DECIMAL);
	private CheckboxSetting saplings = new CheckboxSetting("Saplings", true);
	private CheckboxSetting crops =
		new CheckboxSetting("Crops", "Wheat, carrots, potatoes and beetroots.", true);
	private CheckboxSetting stems = new CheckboxSetting("Stems", "Pumpkins and melons.", true);
	private CheckboxSetting cocoa = new CheckboxSetting("Cocoa", true);
	private CheckboxSetting other = new CheckboxSetting("Other", false);
	
	private boolean isGrowing;
	
	public BonemealAura() {
		super("BonemealAura",
			"Automatically uses bone meal on specific types of plants.\n"
			+ "Use the checkboxes to specify the types of plants.",
			true);
		setCategory(Category.UTILITY);
		addSetting(autoSelectBonemeal);
		addSetting(range);
		addSetting(saplings);
		addSetting(crops);
		addSetting(stems);
		addSetting(cocoa);
		addSetting(other);
	}
	
	@Override
	public void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {

		isGrowing = false;
		
		try {
			Field rightClickDelayTimer = mc.getClass().getDeclaredField(
				wurst.isObfuscated()? "field_71467_ac" : "rightClickDelayTimer");
			rightClickDelayTimer.setAccessible(true);
			if (rightClickDelayTimer.getInt(mc) > 0)
				return;
		} catch (ReflectiveOperationException e) {
			setEnabled(false);
			throw new RuntimeException(e);
		}
		
		if (wurst.getFeatures().autoFarm.isEnabled() && wurst.getFeatures().autoFarm.isFarming())
			return;
		
		Iterable<BlockPos> validBlocks = BlockUtils.getValidBlocks(range.getValue(), (p) -> isCorrectBlock(p));
		if (Iterables.size(validBlocks) == 0)
			return;
		
		EntityPlayerSP player = WMinecraft.getPlayer();
		if (!autoSelectBonemeal.isChecked() && !isBonemeal(player.inventory.getCurrentItem()))
			return;
		
		if (autoSelectBonemeal.isChecked()) {
			int bonemealSlot = getBonemealSlot(player);
			if (bonemealSlot == -1)
				return;
			if (bonemealSlot != player.inventory.currentItem)
				player.inventory.currentItem = bonemealSlot;
		}
		
		isGrowing = true;
		for (BlockPos pos : validBlocks)
			if (BlockUtils.rightClickBlockLegit(pos))
				break;
	}
	
	public boolean isGrowing() {
		return isGrowing;
	}
	
	private int getBonemealSlot(EntityPlayerSP player) {
		for (int i = 0; i < 9; ++i)
			if (isBonemeal(player.inventory.getStackInSlot(i)))
				return i;
		return -1;
	}

	private boolean isBonemeal(ItemStack item) {
		if (WItem.isNullOrEmpty(item))
			return false;
		else
			return item.getItem() instanceof ItemDye && item.getMetadata() == 15;
	}
	
	private boolean isCorrectBlock(BlockPos pos) {
		Block block = WBlock.getBlock(pos);
		if (!(block instanceof IGrowable))
			return false;
		
		IGrowable farmBlock = (IGrowable) block;
		if (farmBlock instanceof BlockGrass)
			return false;
		else if (!farmBlock.canGrow(WMinecraft.getWorld(), pos, WBlock.getState(pos), false))
			return false;
		
		if (block instanceof BlockSapling)
			return saplings.isChecked();
		else if (block instanceof BlockCrops)
			return crops.isChecked();
		else if (block instanceof BlockStem)
			return stems.isChecked();
		else if (block instanceof BlockCocoa)
			return cocoa.isChecked();
		else
			return other.isChecked();
	}
	
}