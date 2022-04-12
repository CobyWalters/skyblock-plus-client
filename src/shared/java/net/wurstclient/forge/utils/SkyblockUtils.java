package net.wurstclient.forge.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkyblockUtils {
	
	static final List<String> suppressedServers = Arrays.asList(new String[] {
					"hub1", "hub2", "classic", "skywars", "events"
				});
	
	static final List<String> mainServers = Arrays.asList(new String[] {
					"economy", "skyblock"
				});
	
	static final List<String> privilegedRanks = Arrays.asList(new String[] {
					"Highroller", "Elite", "Skytitan", "Skygod", "Skylord"
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
	
	public static boolean isRankHighEnough(String rank) {
		return privilegedRanks.contains(rank) || staffRanks.contains(rank);
	}
	
	public static boolean isStaff(String rank) {
		return staffRanks.contains(rank);
	}

}
