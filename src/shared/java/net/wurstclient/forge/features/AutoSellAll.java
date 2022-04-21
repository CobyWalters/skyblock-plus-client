package net.wurstclient.forge.features;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.FeatureController;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.ItemListSetting;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.InventoryUtils;
import net.wurstclient.forge.utils.PlayerControllerUtils;
import net.wurstclient.forge.utils.SkyblockUtils;

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
	private CheckboxSetting sendToEnderchest = new CheckboxSetting("Send blacklisted items to ender chest", "Requires /enderchest", false);
	
	private ArrayList<String> itemNames;
	private long timeOfLastSellAll;
	private boolean openingEnderchest;
	private boolean dumping;
	private int tickTimer;
	
	public AutoSellAll() {
		super("AutoSellAll", 
				"Does /sell all when your inventory gets full, \n"
				+ "except when it contains valuable items", false);
		setCategory(Category.SKYBLOCK);
		addSetting(items);
		addSetting(mode);
		addSetting(sendToEnderchest);
	}
	
	@Override
	protected void onEnable() {
		itemNames = new ArrayList<>(items.getItemNames());
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
		openingEnderchest = false;
		dumping = false;
		tickTimer = 0;
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		EntityPlayerSP player = event.getPlayer();
		long currentTime = Minecraft.getSystemTime();
		if (!(timeOfLastSellAll == 0 || currentTime > timeOfLastSellAll + 45500))
			return;
		else if (mode.getSelected().name.equals("Full Inventory") && !dumping && player.inventory.getFirstEmptyStack() != -1)
			return;

		String rank = wurst.getFeatureController().getPlayerRank();
		if (sendToEnderchest.isChecked() && SkyblockUtils.isRankHighEnough(rank)) {
			if (openingEnderchest) {
				waitForEnderchest();
				return;
			} else if (dumping) {
				if (dumpedAnItem(player))
					return;
				player.closeScreen();
			} else if (InventoryUtils.containsAnyItems(itemNames)) {
				if (Minecraft.getMinecraft().currentScreen == null) {
					ChatUtils.sendMessage(player,  "/enderchest");
					openingEnderchest = true;
				}
				return;
			}
		}
		
		if (!InventoryUtils.containsAnyItems(itemNames)) {
			sellAll(player);
		}
	}
	
	private void waitForEnderchest() {
		++tickTimer;
		if (isEnderchestOpen() && tickTimer == 10) {
			openingEnderchest = false;
			dumping = true;
			tickTimer = 0;
		} else if (tickTimer >= 10) {
			openingEnderchest = false;
			tickTimer = 0;
		}
	}
	
	private boolean dumpedAnItem(EntityPlayerSP player) {
		tickTimer = (tickTimer + 1) % 4;
		if (!isEnderchestOpen() || (tickTimer == 3 && !sendBlacklistedItemToEnderchest(player))) {
			dumping = false;
			tickTimer = 0;
			return false;
		}
		return true;
	}
	
	private boolean isEnderchestOpen() {
		
		GuiScreen screen = mc.currentScreen;
		if (!(screen instanceof GuiChest)) 
			return false;
		
		IInventory chestInventory;
		try {
			chestInventory = (IInventory) ReflectionHelper.findField(GuiChest.class, "lowerChestInventory", "field_147015_w").get((GuiChest) screen);
		} catch (Exception e) {
			setEnabled(false);
			return false;
		}

		return chestInventory.getDisplayName().getUnformattedText().matches("Ender Chest");
	}
	
	private boolean sendBlacklistedItemToEnderchest(EntityPlayerSP player) {
		
		int windowId = WMinecraft.getPlayer().openContainer.windowId;
		InventoryPlayer inventory = player.inventory;
		for (int i = 27; i < 63; ++i) {
			//ChatUtils.message("$" + i);
			for (String itemName : itemNames) {
				if (Item.getIdFromItem(inventory.getStackInSlot((i + 18) % 36).getItem()) == Integer.parseInt(itemName)) {
					ChatUtils.message("clickin " + i);
					PlayerControllerUtils.windowClick_QUICK_MOVE_WINDOW(windowId, i);
					return true;
				}
			}
		}
		
		return false;
	}

	private void sellAll(EntityPlayerSP player) {
		long currentTime = Minecraft.getSystemTime();
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