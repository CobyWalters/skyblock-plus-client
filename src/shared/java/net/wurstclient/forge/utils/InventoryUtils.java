package net.wurstclient.forge.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.AbstractIterator;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.wurstclient.forge.compatibility.WMinecraft;

public final class InventoryUtils {
	
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static Iterable<ItemStack> getHotbar() {
		InventoryPlayer inventory = mc.player.inventory;
		return new Iterable<ItemStack>() {
            public Iterator<ItemStack> iterator() {
                return new AbstractIterator<ItemStack>() {
                    private int index = 0;
                    protected ItemStack computeNext() {
                    	if (index <= 9)
                    		return inventory.getStackInSlot(index++);
                    	return (ItemStack) this.endOfData();
                    }
                };
            }
        };
	}
	
	public static int getId(ItemStack stack) {
		return Item.getIdFromItem(stack.getItem());
	}
	
	public static int getId(Item item) {
		return Item.getIdFromItem(item);
	}
	
	public static Set<String> getToolClasses(ItemStack stack) {
		return stack.getItem().getToolClasses(stack);
	}
	
	public static int getNumberOfEmptySlots() {
		InventoryPlayer inventory = WMinecraft.getPlayer().inventory;
		int count = 0;
		for (int i = 0; i < 36; ++i)
			if (inventory.getStackInSlot(i).getDisplayName().equals("Air"))
				++count;
		return count;
	}
	
	public static boolean containsAnyItems(ArrayList<String> itemNames) {
		InventoryPlayer inventory = WMinecraft.getPlayer().inventory;
		for (int i = 0; i < 36; ++i)
			for (String itemName : itemNames)
				if (Item.getIdFromItem(inventory.getStackInSlot(i).getItem()) == Integer.parseInt(itemName))
					return true;
		return false;
	}
	
	public static boolean isTool(ItemStack stack) {
		return (stack.getItem() instanceof ItemTool ||
				stack.getItem() instanceof ItemHoe ||
				stack.getItem() instanceof ItemSword ||
				stack.getItem() instanceof ItemFishingRod || 
				stack.getItem() instanceof ItemBow || 
				stack.getItem() instanceof ItemShears);
	}
}