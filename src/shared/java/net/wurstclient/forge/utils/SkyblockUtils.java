package net.wurstclient.forge.utils;

import java.util.Arrays;
import java.util.List;

public class SkyblockUtils {
	
	static final List<String> suppressedServers = Arrays.asList(new String[] {
					"hub1", "hub2", "classic", "skywars", "events"
				});
	
	static final List<String> mainServers = Arrays.asList(new String[] {
					"economy", "skyblock"
				});
	
	static final List<String> specialCommandsRanks = Arrays.asList(new String[] {
					"Highroller", "Elite", "Skytitan", "Skygod", "Skylord"
				});
	
	static final List<String> coloredChatRanks = Arrays.asList(new String[] {
					"Highroller", "Elite", "VIP", "Skytitan", "Skygod", "Skylord", "Skyking"
				});
	
	static final List<String> staffRanks = Arrays.asList(new String[] {
					"Owner", "Admin", "Manager", "Developer", "Super Moderator", "Moderator", "Helper"
				});
	
	public static boolean isSuppressedServer(String serverName) {
		return suppressedServers.contains(serverName);
	}
	
	public static boolean isAMainServer(String serverName) {
		return mainServers.contains(serverName);
	}
	
	public static boolean canUseSpecialCommands(String rank) {
		return specialCommandsRanks.contains(rank) || staffRanks.contains(rank);
	}
	
	public static boolean canUseColoredChat(String rank) {
		return coloredChatRanks.contains(rank) || staffRanks.contains(rank);
	}
	
	public static boolean isStaff(String rank) {
		return staffRanks.contains(rank);
	}

}
