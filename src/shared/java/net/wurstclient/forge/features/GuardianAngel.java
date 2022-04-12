package net.wurstclient.forge.features;

import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.ChatUtils;

public class GuardianAngel extends Feature {

	private SliderSetting threshold =
		new SliderSetting("Health threshold",
						  "Will trigger only if\n" +
						   "you are at or below the\n" +
						   "given amount of health.",
						  2.5, 0.5, 9.5, 0.5, v -> ValueDisplay.DECIMAL.getValueString(v));

	private EnumSetting<SendTo> sendTo = new EnumSetting<>("Send to", SendTo.values(), SendTo.SPAWN);
	private boolean protecting;
	
	public GuardianAngel() {
		super("GuardianAngel",
			  "Automatically sends you somewhere safe\n" +
			   "when you reach a certain amount of health.\n" +
			   "Resets when you heal above that amount.",
			  false);
		setCategory(Category.SKYBLOCK);
		addSetting(sendTo);
		addSetting(threshold);
	}
	
	@Override
	public String getRenderName() {
		return getName();
	}
	
	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable() {
		protecting = false;
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		EntityPlayerSP player = event.getPlayer();
		boolean belowThreshold = player.getHealth() / 2 <= threshold.getValueF();
		if (protecting && belowThreshold) {
			protecting = !protecting;
			player.sendChatMessage(sendTo.getSelected().command);
		} else if (!protecting && !belowThreshold) {
			protecting = !protecting;
		}
	}
	
	private enum SendTo {
		
		SPAWN("Spawn", "/spawn"),
		ISLAND("Island", "/island");
		
		private String name;
		private String command;
		
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