/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.utils.ChatUtils;

public class BetterChat extends Feature {
	
	private CheckboxSetting clagWarning = new CheckboxSetting("Amplify clag warning", false);
	private CheckboxSetting AFKMessage = new CheckboxSetting("Reply to messages with AFK warning", false);
	private CheckboxSetting lottoBuy = new CheckboxSetting("Auto buy lottery tickets", false);
	private CheckboxSetting messageNotifications = new CheckboxSetting("Plays a message notification sound", false);
	private CheckboxSetting filterPlayerAdverts = new CheckboxSetting("Filter player advertisements", false);
	private EnumSetting<Filter> filter = new EnumSetting<>("Chat filter", Filter.values(), Filter.NONE);
	private long timeOfLastAFKMessage;
	
	public BetterChat() {
		super("BetterChat", "Various chat related features.", false);
		setCategory(Category.SKYBLOCK);
		addSetting(clagWarning);
		addSetting(AFKMessage);
		addSetting(lottoBuy);
		addSetting(messageNotifications);
		addSetting(filterPlayerAdverts);
		addSetting(filter);
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
	public void onChat(ClientChatReceivedEvent event) {
		String message = ChatUtils.getUnformattedText(event.getMessage().getUnformattedText());
		String[] splitMessage = message.split(" ");
		EntityPlayerSP player = mc.player;
		
		//ChatUtils.debugMessage(message);
		if (filter.getSelected().getStrength() > 0)
			filterServerMessages(event, message);

		if (filterPlayerAdverts.isChecked() && message.matches("\\[[a-zA-Z]*\\] [a-zA-Z0-9_]*:.*"))
			filterPlayerAdverts(event, message);
		
		if (clagWarning.isChecked() && message.matches("WARNING .* Ground items .*"))
			amplifyClagWarning(splitMessage);
		
		if (AFKMessage.isChecked() && message.matches("\\[[a-zA-Z0-9_]* -> me\\].*"))
			sendAFKMessage(player, splitMessage);
		
		if (lottoBuy.isChecked() && message.matches("\\[SB(Lotto|Lottery)\\] Congratulations .*"))
			buyLottoTickets(player);
		
		if (messageNotifications.isChecked() && message.matches("\\[[a-zA-Z0-9_]* -> me\\].*"))
			mc.player.playSound(SoundEvents.BLOCK_NOTE_BELL, 10, 5);
	}
	
	private void filterServerMessages(ClientChatReceivedEvent event, String message) {
		
		if (message.matches("(\\[Skyblock\\]|\\[SB\\]).*"))
			event.setCanceled(true);
		else if (message.matches("[a-zA-Z0-9_]* voted at .*"))
			event.setCanceled(true);
		else if (message.matches("Welcome (to|home).*"))
			event.setCanceled(true);
		else if (message.matches("Teleporting to .*"))
			event.setCanceled(true);
		else if (message.matches("You are now leaving .*"))
			event.setCanceled(true);
		else if (message.matches("Visiting [a-zA-Z0-9_]*'s island."))
			event.setCanceled(true);
		else if (message.matches("[a-zA-Z0-9_]* has made the advancement .*"))
			event.setCanceled(true);
		else if (message.matches(" *"))
			event.setCanceled(true);
		
		if (filter.getSelected().getStrength() > 1) {
			if (message.matches("\\[SB(Lotto|Lottery)\\].*" +
								 "(You got|You now have|just bought|players buying) \\d+ ticket.*"))
				event.setCanceled(true);
			else if (message.matches("(\\[SBCrates\\].*|[a-zA-Z0-9_]* has just opened a .* Crate!)"))
				event.setCanceled(true);
			else if (message.matches("Welcome [a-zA-Z0-9_]* to Skyblock!"))
				event.setCanceled(true);
			else if (message.matches("[a-zA-Z0-9_]* was lucky and received .*"))
				event.setCanceled(true);
			else if (message.matches("\\[.\\] [0-9]* players have perished .*"))
				event.setCanceled(true);
			else if (message.matches("You have not voted .*"))
				event.setCanceled(true);
		}
		
		if (filter.getSelected().getStrength() > 2) {
			if (message.matches("[(SBLotto|SBLottery)] Congratulations go to .*"))
				event.setCanceled(true);
			else if (message.matches("[Shop] .*"))
				event.setCanceled(true);
			else if (message.matches("WARNING .* Ground items .*"))
				event.setCanceled(true);
			else if (message.matches("\\[.\\] (.*the word.*|The word was.*)"))
				event.setCanceled(true);
		}
	}
	
	private void filterPlayerAdverts(ClientChatReceivedEvent event, String message) {
		String formattedMessage = event.getMessage().getFormattedText();
		if (message.indexOf(':') + 2 >= message.length())
				return;
		
		String playerMessageFormatted = formattedMessage.substring(formattedMessage.indexOf(':') + 2);
		String playerMessageUnformatted = "\u00a7r\u00a7f" + 
												 message.substring(message.indexOf(':') + 2) +
												 "\u00a7r";
		if (!playerMessageFormatted.equals(playerMessageUnformatted))
			event.setCanceled(true);
	}
	
	private void amplifyClagWarning(String[] splitMessage) {
		String title = TextFormatting.BOLD + "" + TextFormatting.RED + "Items" +
							  TextFormatting.RESET + " removed in " + 
							  TextFormatting.BOLD + "" + TextFormatting.RED + 
							  splitMessage[splitMessage.length - 2] + " " +
							  splitMessage[splitMessage.length - 1];
		mc.ingameGUI.displayTitle(title, "", 1, 10, 1);
	}
	
	private void sendAFKMessage(EntityPlayerSP player, String[] splitMessage) {
		String msgname = splitMessage[0].substring(1);
    	long currentTime = Minecraft.getSystemTime();
    	if (timeOfLastAFKMessage == 0 || currentTime > timeOfLastAFKMessage + 1000) {
			ChatUtils.sendMessage(player,  "/msg " + msgname + " I am AFK right now ^_^");
			timeOfLastAFKMessage = currentTime;
    	}
	}
	
	private void buyLottoTickets(EntityPlayerSP player) {
		String serverName = wurst.getFeatureController().getServerName();
		if (serverName.equals("economy"))
			ChatUtils.sendMessage(player, "/lot buy 5");
		else if (serverName.equals("skyblock"))
			ChatUtils.sendMessage(player, "/lot buy 2");
	}
	
	private enum Filter {
		
		NONE("None", 0),
		LIGHT("Light", 1),
		MODERATE("Moderate", 2),
		HEAVY("Heavy", 3);
		
		private String name;
		private int filterStrength;
		
		private Filter(String name, int strength) {
			this.name = name;
			this.filterStrength = strength;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public int getStrength() {
			return filterStrength;
		}
	}
	
}