package net.wurstclient.forge.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.clickgui.Component;
import net.wurstclient.forge.clickgui.TextInputButton;

public final class TextInputSetting extends Setting {
	
	private String text;
	private final String defaultText;
	
	public TextInputSetting(String name, String description, String text) {
		super(name, description);
		this.text = text;
		this.defaultText = text;
	}
	
	public TextInputSetting(String name, String text) {
		this(name, null, text);
	}
	
	public String getText() {
		return text;
	}
	
	public String getFormattedText() {
		return text.replace('\u00a7', '&');
	}
	
	public String getDefaultText() {
		return defaultText;
	}
	
	public void setText(String text) {
		this.text = text;
		ForgeWurst.getForgeWurst().getFeatures().saveSettings();
	}
	
	@Override
	public Component getComponent() {
		return new TextInputButton(this);
	}
	
	@Override
	public void fromJson(JsonElement json) {
		if (!json.isJsonPrimitive())
			return;
		
		JsonPrimitive primitive = json.getAsJsonPrimitive();
		if (!primitive.isString())
			return;
		
		setText(primitive.getAsString());
	}
	
	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(text);
	}
}
