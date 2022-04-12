package net.wurstclient.forge.features;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.TextInputSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.ChatUtils;

public class AutoAdvert extends Feature {

	private final TextInputSetting textInput = 
			new TextInputSetting("Ad: ", 
								 "Automatically sends the given\n" +
								  "message with the given delay.",
								 "");
	private final SliderSetting cooldown = new SliderSetting("Cooldown (minutes)", 20, 10, 60, 5, ValueDisplay.INTEGER);
	
	private long timeOfLastAdvert;
	
	public AutoAdvert() {
		super("AutoAdvert", 
			  "Automatically sends avertisements " +
			   "of your choosing.",
			  false);
		setCategory(Category.SKYBLOCK);
		addSetting(textInput);
		addSetting(cooldown);
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
		long currentTime = Minecraft.getSystemTime();
		if (timeOfLastAdvert + cooldown.getValueI() * 60 * 1000 > currentTime)
			return;
		
		String message = textInput.getFormattedText();
		if (message.length() == 0)
			return;
		
		ChatUtils.sendMessage(WMinecraft.getPlayer(), message);
		timeOfLastAdvert = currentTime;
	}
	
}
