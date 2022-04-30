package net.wurstclient.forge.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.InventoryUtils;
import net.wurstclient.forge.utils.SkyblockUtils;

public class AutoClaim extends Feature{
	
	private boolean claimingDaily;
	private boolean interceptingKitInfo;
	private boolean interceptingDailyInfo;
	final ArrayList<String> kitsToClaim = new ArrayList<String>();
	final HashMap<String, Long> timeOfLastKitClaim = new HashMap<String, Long>();
	final HashMap<String, Long> timeOfNextDailyClaim = new HashMap<String, Long>();
	
	public AutoClaim() {
		super("AutoClaim", "Automatically claims kits and /daily", false);
		setCategory(Category.SKYBLOCK);
	}
	
	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable() {
		kitsToClaim.clear();
		interceptingKitInfo = false;
		interceptingDailyInfo = false;
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		EntityPlayerSP player = WMinecraft.getPlayer();
		String serverName = wurst.getFeatureController().getServerName();
		if (serverName == null || !SkyblockUtils.isAMainServer(serverName) || player.dimension == -1)
			return;
		else if (interceptingKitInfo || interceptingDailyInfo) 
			return;
		
		if (kitsToClaim.size() > 0) {
			claimKit(player, kitsToClaim.get(0));
		} else if (claimingDaily) {
			if (isDailyGUIOpen())
				claimDaily(player);
		} else {
			tryClaimingKitsAndDaily(player, serverName);
		}
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		String serverName = wurst.getFeatureController().getServerName();
		EntityPlayerSP player = WMinecraft.getPlayer();
		if (serverName == null || !SkyblockUtils.isAMainServer(serverName) || player.dimension == -1)
			return;
		
		else if (interceptingKitInfo)
			checkForKitInfo(event, player, serverName);
		else if (interceptingDailyInfo)
			checkForDailyInfo(event, serverName);
	}
	
	private void claimKit(EntityPlayerSP player, String kitName) {
		ChatUtils.sendMessage(player, "/kit " + kitName);
		kitsToClaim.remove(0);
		return;
	}
	
	private boolean isDailyGUIOpen() {
		GuiScreen screen = mc.currentScreen;
		if (!(screen instanceof GuiChest)) 
			return false;
		
		IInventory chestInventory;
		try {
			chestInventory = (IInventory) ReflectionHelper.findField(GuiChest.class, "lowerChestInventory", "field_147015_w").get((GuiChest) screen);
		} catch (Exception e) {
			return false;
		}
		
		return chestInventory.getDisplayName().getUnformattedText().matches(".*Daily Rewards");
	}
	
	private void claimDaily(EntityPlayerSP player) {
		GuiChest menu = (GuiChest) mc.currentScreen;
		int id = InventoryUtils.getId(menu.inventorySlots.getSlot(22).getStack());
		if (id == 342 || id == 328) {
			mc.playerController.windowClick(mc.player.openContainer.windowId, 22, 0, ClickType.PICKUP, mc.player);
			interceptingDailyInfo = true;
		}
		player.closeScreen();
		claimingDaily = false;
	}
	
	private void checkForKitInfo(ClientChatReceivedEvent event, EntityPlayerSP player, String serverName) {
		
		String message = event.getMessage().getUnformattedText();
		if (message.matches("/kit is not allowed in this area.")) {
			interceptingKitInfo = false;
			event.setCanceled(true);
		} 
		
		else if (message.matches("(Available Skyblock )?Kits:.*")) {
			String kits = message.substring(message.indexOf("Kits:") + 5).trim();
			ArrayList<String> kitList = new ArrayList<String>(Arrays.asList(kits.split(" ")));
			kitList.remove("Sapling");
			
			String formattedMessage = event.getMessage().getFormattedText();
			for (String kit : kitList)
				if (formattedMessage.charAt(formattedMessage.indexOf(kit) - 1) != 'm')
					kitsToClaim.add(kit);
			
			interceptingKitInfo = false;
			event.setCanceled(true);
		}
		
		else if (Minecraft.getSystemTime() > timeOfLastKitClaim.get(serverName) + 2000) {
			ChatUtils.sendMessage(player, "/kit");
		}
	}
	
	private void checkForDailyInfo(ClientChatReceivedEvent event, String serverName) {
		String message = event.getMessage().getUnformattedText();
		if (message.matches("/daily is not allowed in this area.")) {
			timeOfNextDailyClaim.put(serverName, Minecraft.getSystemTime() + 10000);
			interceptingDailyInfo = false;
			event.setCanceled(true);
			return;
		}
		
		else if (message.matches("You have received your Daily Reward!.*")) {
			timeOfNextDailyClaim.put(serverName, Minecraft.getSystemTime() + 86401000);
			interceptingDailyInfo = false;
			return;
		}
		
		String pattern = "You have to wait another ((\\d+) hrs?(, | and )?)?((\\d+) mins?(, | and )?)?((\\d+) secs?(, | and )?)?";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(message);
		if (m.find()) {
			int hours   = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
			int minutes = m.group(5) == null ? 0 : Integer.parseInt(m.group(5));
			int seconds = m.group(8) == null ? 0 : Integer.parseInt(m.group(8));
			long millis = 1000 * (hours * 3600 + minutes * 60 + seconds + 1);
			timeOfNextDailyClaim.put(serverName, Minecraft.getSystemTime() + millis);
			interceptingDailyInfo = false;
			event.setCanceled(true);
		}
	}
	
	private void tryClaimingKitsAndDaily(EntityPlayerSP player, String serverName) {
		final long currentTime = Minecraft.getSystemTime();
		
		Long lastKitClaim = timeOfLastKitClaim.get(serverName);
		if (lastKitClaim == null)
			timeOfLastKitClaim.put(serverName, currentTime - 59000);
		else if (currentTime > lastKitClaim + 60000) {
			timeOfLastKitClaim.put(serverName, currentTime);
			interceptingKitInfo = true;
			ChatUtils.sendMessage(player, "/kit");
			return;
		}
		
		Long nextDailyClaim = timeOfNextDailyClaim.get(serverName);
		if (nextDailyClaim == null)
			timeOfNextDailyClaim.put(serverName, currentTime + 1000);
		else if (currentTime > nextDailyClaim && Minecraft.getMinecraft().currentScreen == null) {
			claimingDaily = true;
			ChatUtils.sendMessage(player, "/daily");
		}
	}
	
}