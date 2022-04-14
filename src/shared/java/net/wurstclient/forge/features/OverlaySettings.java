package net.wurstclient.forge.features;

import net.wurstclient.forge.Feature;
import net.wurstclient.forge.clickgui.ClickGuiScreen;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;

@Feature.DontSaveState
public class OverlaySettings extends Feature {
	
	private final CheckboxSetting disableOverlay =
			new CheckboxSetting("Disable overlay",
								"toggles the overlay on the upper-\n" +
								 "left corner of the screen.",
								false);
	private final SliderSetting overlayScale = new SliderSetting("Overlay scale", 1, 0.50, 1, 0.01, ValueDisplay.PERCENTAGE);
	private final SliderSetting overlayXOffset = new SliderSetting("Overlay X offset", 0, 0, 1, 0.01, ValueDisplay.PERCENTAGE);
	private final SliderSetting overlayYOffset = new SliderSetting("Overlay Y offset", 0, 0, 1, 0.01, ValueDisplay.PERCENTAGE);
	private final EnumSetting<Alignment> alignment = new EnumSetting<>("Overlay alignment", Alignment.values(), Alignment.LEFT);
	
	
	public OverlaySettings() {
		super("OverlaySettings", "", false);
		addSetting(disableOverlay);
		addSetting(overlayScale);
		addSetting(overlayXOffset);
		addSetting(overlayYOffset);
		addSetting(alignment);
	}
	
	public boolean isOverlayDisabled() {
		return disableOverlay.isChecked();
	}
	
	public double getScale() {
		return overlayScale.getValue();
	}
	
	public double getXOffset() {
		return overlayXOffset.getValue();
	}
	
	public double getYOffset() {
		return overlayYOffset.getValue();
	}
	
	public String getAlignment() {
		return alignment.getSelected().name();
	}
	
	private enum Alignment {
		
		LEFT("Left"),
		RIGHT("Right"),
		CENTER("Center");
		
		private final String name;
		
		private Alignment(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
}