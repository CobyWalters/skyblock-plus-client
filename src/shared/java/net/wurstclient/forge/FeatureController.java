/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPlayerMoveEvent;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.SkyblockUtils;

public final class FeatureController {
	
	//SHARED WARPS
	private final AxisAlignedBB skyblockXmas2020 =
			new AxisAlignedBB(5301, 0, 5601, 5501, 256, 5831);
	private final AxisAlignedBB skyblockXmas2021 =
			new AxisAlignedBB(114897, 0, 114901, 115101, 256, 115152);
	private final AxisAlignedBB skyblockEaster2021 =
			new AxisAlignedBB(7901, 0, 7901, 8101, 256, 8101);
	private final AxisAlignedBB skyblockHalloween2021 =
			new AxisAlignedBB(5301, 0, 4501, 5501, 256, 4701);
	private final AxisAlignedBB skyblockSpleef =
			new AxisAlignedBB(109901, 0, 109901, 110101, 256, 110101);
	
	//SKYBLOCK WARPS
	private final AxisAlignedBB skyblockSpawn =
			new AxisAlignedBB(3901, 0, 1901, 4106, 256, 2101);
	private final AxisAlignedBB skyblockOldSpawn =
			new AxisAlignedBB(-4099, 0, -2120, -3899, 256, -1899);
	private final AxisAlignedBB skyblockInfo =
			new AxisAlignedBB(2701, 0, -28899, 2901, 256, -28699);
	private final AxisAlignedBB skyblockAirplane =
			new AxisAlignedBB(2901, 0, 2901, 3101, 256, 3101);
	private final AxisAlignedBB skyblockCastle =
			new AxisAlignedBB(37901, 0, 37901, 38101, 256, 38101);
	private final AxisAlignedBB skyblockPassive =
			new AxisAlignedBB(-499, 0, -299, -299, 256, -99);
	private final AxisAlignedBB skyblockHostile =
			new AxisAlignedBB(-2499, 0, -299, -2299, 256, -99);
	
	//ECONOMY WARPS
	private final AxisAlignedBB skyblockEconomySpawn =
		new AxisAlignedBB(-200, 0, -200, 201, 256, 201);
	private final AxisAlignedBB skyblockEconomyShop =
			new AxisAlignedBB(-100, 0, -500, 101, 256, -299);
	private final AxisAlignedBB skyblockEconomyPassive =
			new AxisAlignedBB(3900, 0, 3900, 4101, 256, 4101);
	
	//CLASSIC WARPS
	/*private final AxisAlignedBB skyblockClassicPassive = 
			new AxisAlignedBB(44301, 0, 7400, 44501, 256, 7600);
	private final AxisAlignedBB skyblockClassicChristmas =
			new AxisAlignedBB(-14899, 0, 40101, -15099, 256, 40301);
	private final AxisAlignedBB skyblockClassicSpawn =
			new AxisAlignedBB(-99, 0, 101, 101, 256, 301);
	private final AxisAlignedBB skyblockClassicDie =
			new AxisAlignedBB(101, 0, -8499, 301, 256, -8299);
	private final AxisAlignedBB skyblockClassicRules =
			new AxisAlignedBB(101, 0, -8199, 301, 256, -7999);
	private final AxisAlignedBB skyblockClassicInfo =
			new AxisAlignedBB(37901, 0, 37901, 38101, 256, 38101);
	private final AxisAlignedBB skyblockClassicMob =
			new AxisAlignedBB(301, 0, -8399, 501, 256, -8199);
	private final AxisAlignedBB skyblockClassicNoobSpawn =
			new AxisAlignedBB(-99, 0, -8399, 301, 256, -8099);
	private final AxisAlignedBB skyblockClassicMob =
			new AxisAlignedBB(101, 0, -8399, 301, 256, -8199);
	private final AxisAlignedBB skyblockClassicMob =
			new AxisAlignedBB(301, 0, -8399, 501, 256, -8199);*/

	private final FeatureList features;
	private boolean inPublicSpace;
	private String serverName;
	private long lastWorldLoad;
	private int blankMessageCounter;
	private boolean interceptingPlayerInfo;
	private boolean interceptingServerInfo;
	private String rank;
	
	public FeatureController() {
		features = ForgeWurst.getForgeWurst().getFeatures();
	}	
	
	public String getServerName() {
		return serverName;
	}
	
	public String getPlayerRank() {
		return rank;
	}
	
	public boolean isInPublicSpace() {
		return inPublicSpace;
	}

	public void suppressFeatures(boolean suppress) {
		for(Feature feature : features.getRegistry())
			if (feature.isLimited())
				feature.setSuppressed(suppress);
	}
	
	public void suppressSkyblockFeatures(boolean suppress) {
		for (Feature feature : features.getRegistry()) {
			if (feature.getCategory() == null)
				continue;
			else if (feature.getCategory().getName().equals("Skyblock"))
				feature.setSuppressed(suppress);
		}
	}
	
	public void suppressSkyblockFeaturesByRank(boolean suppress) {
		features.flight.setSuppressed(suppress);
		features.autoRepair.setSuppressed(suppress);
	}
	
	public boolean detectPublicSpace(EntityPlayerSP player) {
		AxisAlignedBB playerHitbox = player.getEntityBoundingBox();
		if (serverName.equals("economy")) {
			return player.dimension == -1
				|| playerHitbox.intersects(skyblockEconomySpawn)
				|| playerHitbox.intersects(skyblockEconomyShop)
				|| playerHitbox.intersects(skyblockEconomyPassive)
				|| playerHitbox.intersects(skyblockXmas2020)
				|| playerHitbox.intersects(skyblockXmas2021)
				|| playerHitbox.intersects(skyblockEaster2021)
				|| playerHitbox.intersects(skyblockHalloween2021)
				|| playerHitbox.intersects(skyblockSpleef);
		} else if (serverName.equals("skyblock")) {
			return player.dimension == -1
				|| playerHitbox.intersects(skyblockSpawn)
				|| playerHitbox.intersects(skyblockOldSpawn)
				|| playerHitbox.intersects(skyblockInfo)
				|| playerHitbox.intersects(skyblockAirplane)
				|| playerHitbox.intersects(skyblockCastle)
				|| playerHitbox.intersects(skyblockPassive)
				|| playerHitbox.intersects(skyblockHostile)
				|| playerHitbox.intersects(skyblockXmas2020)
				|| playerHitbox.intersects(skyblockXmas2021)
				|| playerHitbox.intersects(skyblockEaster2021)
				|| playerHitbox.intersects(skyblockHalloween2021)
				|| playerHitbox.intersects(skyblockSpleef);
		} else {
			return true;
		}
	}
	
	@SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		
		// Only continue if playing on a skyblock.net server
		ServerData server = Minecraft.getMinecraft().getCurrentServerData();
		if (server == null || !server.serverIP.matches(".*skyblock\\.(com|net|org)")) {
			serverName = null;
			rank = null;
			interceptingPlayerInfo = false;
			interceptingServerInfo = false;
			controlSkyblockFeatures();
			return;
		}
		
		// Only continue if entity is player
		if (!(event.getEntity() instanceof EntityPlayerSP))
			return;
		
		// Try to avoid any unnecessary worldload events
		EntityPlayerSP player = WMinecraft.getPlayer();
		long currentTime = System.currentTimeMillis();
		long sinceLastWorldLoad = currentTime - lastWorldLoad;
		lastWorldLoad = currentTime;
		if (sinceLastWorldLoad < 1000L || player.isDead || player.dimension == -1)
			return;
		
		ChatUtils.message("Getting server info");
		// Send the message, start waiting for the response to see which server the player is on
		player.sendChatMessage("/server");
		interceptingServerInfo = true;
		
    }
	
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		
		// handle server detection
		if (interceptingServerInfo) {
			String message = event.getMessage().getUnformattedText();
			if (message.startsWith("You are currently connected to")) {
				
				int serverNameStart = message.indexOf("to ") + 3;
				int serverNameEnd = message.indexOf(".");
				try {
					String serverNameCurrent = message.substring(serverNameStart, serverNameEnd);
					if (serverNameCurrent.equals(serverName))
						return;
					serverName = serverNameCurrent;
					//ChatUtils.message(serverName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (SkyblockUtils.isAMainServer(serverName)) {
					EntityPlayerSP player = WMinecraft.getPlayer();
					player.sendChatMessage("/pi " + player.getName());
					interceptingPlayerInfo = true;
				} else {
					controlSkyblockFeatures();
				}
				
				event.setCanceled(true);
			} else if (message.startsWith("You may connect to the following servers")) {
				interceptingServerInfo = false;			
				event.setCanceled(true);
			}
		}
				
		// handle player rank detection
		if (interceptingPlayerInfo) {
			String message = event.getMessage().getUnformattedText();
			if (message.equals(" ")) {
				if (++blankMessageCounter == 2) {
					interceptingPlayerInfo = false;
					blankMessageCounter = 0;
				}
				event.setCanceled(true);
			} else if (message.startsWith(WMinecraft.getPlayer().getName()) && message.contains("Rank: ")) {
				int lineStart = message.indexOf("Rank: ");
				int lineEnd = message.indexOf('\n', lineStart);
				try {
					String line = message.substring(lineStart, lineEnd);
					rank = line.replaceAll("(Rank:)|([\\[\\] \\n])", "");
					//ChatUtils.message(rank);
					controlSkyblockFeatures();
				} catch (Exception e) {
					e.printStackTrace();
				}
				event.setCanceled(true);
			}
		}
		
	}
	
	public void controlSkyblockFeatures() {
		
		suppressFeatures(false);
		
		// Suppress skyblock features on non skyblock servers
		if (serverName == null) {
			suppressSkyblockFeatures(true);
			return;
		} else {
			suppressSkyblockFeatures(false);
		}
		
		// Suppress illegal features always on these skyblock servers
		if (SkyblockUtils.isSuppressedServer(serverName)) {
			suppressFeatures(true);
			return;
		}
		
		// Suppress skyblock features selectively on the main skyblock servers
		if (SkyblockUtils.isAMainServer(serverName))
			suppressSkyblockFeaturesByRank(!SkyblockUtils.isRankHighEnough(rank));
	}
	
	@SubscribeEvent
	public void onPlayerMove(WPlayerMoveEvent event) {
		if (serverName == null)
			return;
		else if (SkyblockUtils.isAMainServer(serverName)) {
			// Suppress illegal features if player is in a public area
			EntityPlayerSP player = WMinecraft.getPlayer();
			if (inPublicSpace != detectPublicSpace(player)) {
				inPublicSpace = !inPublicSpace;
				suppressFeatures(inPublicSpace);
				if (!inPublicSpace) {
					suppressSkyblockFeaturesByRank(!SkyblockUtils.isRankHighEnough(rank));
				}
			}
		}
	}
	
}