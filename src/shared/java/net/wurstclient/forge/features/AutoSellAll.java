package net.wurstclient.forge.features;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.ItemListSetting;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.InventoryUtils;

public class AutoSellAll extends Feature {

	private ItemListSetting items = 
		new ItemListSetting("Item Blacklist",
							Items.COAL, Items.REDSTONE, Items.GOLD_INGOT, Items.IRON_INGOT, Items.DIAMOND,
							Items.DYE, Item.getItemById(29), Item.getItemById(33), Item.getItemById(154));
	private EnumSetting<Mode> mode =
		new EnumSetting<>("Mode",
						  "Determines how to /sell all.\n" +
						   "\u00a7lTimer\u00a7r - Sells every 45 seconds.\n" +
						   "\u00a7lFull Inventory\u00a7r - Sells on a full\n" +
						   "    inventory.",
						  Mode.values(), Mode.FULLINV);
	
	private ArrayList<String> itemNames;
	
	private long timeOfLastSellAll;
	
	public AutoSellAll() {
		super("AutoSellAll", 
				"Does /sell all when your inventory gets full, \n"
				+ "except when it contains valuable items", false);
		setCategory(Category.SKYBLOCK);
		addSetting(items);
		addSetting(mode);
	}
	
	@Override
	protected void onEnable() {
		itemNames = new ArrayList<>(items.getItemNames());
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		EntityPlayerSP player = event.getPlayer();
		long currentTime = Minecraft.getSystemTime();
		if (mode.getSelected().name.equals("Timer") && 
			!(timeOfLastSellAll == 0 || currentTime > timeOfLastSellAll + 45500))
			return;
		else if (mode.getSelected().name.equals("Full Inventory") && player.inventory.getFirstEmptyStack() != -1)
			return;
		else if (InventoryUtils.containsAnyItems(itemNames))
			return;
		ChatUtils.sendMessage(player,  "/sell all");
		timeOfLastSellAll = currentTime;
	}
	
	private enum Mode {
		
		TIMER("Timer"),
		FULLINV("Full Inventory");
		
		private String name;
		
		private Mode(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}	
}