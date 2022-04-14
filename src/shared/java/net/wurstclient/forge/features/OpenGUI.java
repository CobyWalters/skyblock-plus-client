package net.wurstclient.forge.features;

import net.wurstclient.forge.Feature;
import net.wurstclient.forge.clickgui.ClickGuiScreen;

public class OpenGUI extends Feature {

	public OpenGUI() {
		super("OpenGUI", "", false);
	}
	
	@Override
	protected void onEnable() {
		mc.displayGuiScreen(new ClickGuiScreen(wurst.getGui()));
		setEnabled(false);
	}
}
