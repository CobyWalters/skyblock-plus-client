package net.wurstclient.forge.features;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;

public class DisableAll extends Feature {
		
	public DisableAll() {
		super("DisableAll", "Toggles off all hacks", true);
		setCategory(Category.UTILITY);
	}
	
	@Override
	public void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		for (Feature f : wurst.getFeatures().getValues())
			if(f.isEnabled() && f != this)
				f.setEnabled(false);
		setEnabled(false);
	}
	
}