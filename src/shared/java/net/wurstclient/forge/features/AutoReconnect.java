package net.wurstclient.forge.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.utils.ChatUtils;

public class AutoReconnect extends Feature {

	private final EnumSetting<SendTo> sendTo = new EnumSetting<>("Send to", SendTo.values(), SendTo.SKYBLOCK);
	private long timeOfLastReconnectAttempt;
	
	public AutoReconnect() {
		super("AutoReconnect",
			  "Automatically sends you to the\n" + 
			   "specified skyblock server\n" + 
			   "when you enter the hub.",
			  false);
		setCategory(Category.SKYBLOCK);
		addSetting(sendTo);
	}
	
	@Override
	public String getRenderName() {
		return getName() + " [" + sendTo.getSelected().name + "]";
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
	public void onUpdate(WUpdateEvent event) {
		String serverName = wurst.getFeatureController().getServerName();
		if (serverName == null)
			return;
		
		long currentTime = Minecraft.getSystemTime();
		if ((serverName.equals("hub1") || serverName.equals("hub2")) &&
			(timeOfLastReconnectAttempt == 0 || currentTime > timeOfLastReconnectAttempt + 2000)) {
			EntityPlayerSP player = WMinecraft.getPlayer();
			ChatUtils.sendMessage(player, sendTo.getSelected().command);
			timeOfLastReconnectAttempt = currentTime;
		}
	}
	
	private enum SendTo {
		
		ECONOMY("Economy", "/economy"),
		SKYBLOCK("Skyblock", "/skyblock"),
		CLASSIC("Classic", "/classic");
		
		private final String name;
		private final String command;
		
		private SendTo(String name, String command) {
			this.name = name;
			this.command = command;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
}