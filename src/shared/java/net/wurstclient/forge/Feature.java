package net.wurstclient.forge;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.wurstclient.forge.compatibility.WForgeRegistryEntry;
import net.wurstclient.forge.settings.Setting;

public abstract class Feature extends WForgeRegistryEntry<Feature> {
	
	protected static final ForgeWurst wurst = ForgeWurst.getForgeWurst();
	protected static final Minecraft mc = Minecraft.getMinecraft();
	
	private final String name;
	private final String description;
	private Category category;
	private final LinkedHashMap<String, Setting> settings = new LinkedHashMap<>();
	private boolean limited;
	private boolean suppressed;
	private boolean enabled;
	private final boolean stateSaved = !getClass().isAnnotationPresent(DontSaveState.class);
	
	public Feature(String name, String description, boolean limited) {
		this.name = name;
		this.description = description;
		this.limited = limited;
	}
	
	public final String getName() {
		return name;
	}
	
	public String getRenderName() {
		return name;
	}
	
	public final String getDescription() {
		return description;
	}
	
	public final Category getCategory() {
		return category;
	}
	
	protected final void setCategory(Category category) {
		this.category = category;
	}
	
	public final Map<String, Setting> getSettings() {
		return Collections.unmodifiableMap(settings);
	}
	
	protected final void addSetting(Setting setting) {
		String key = setting.getName().toLowerCase();
		
		if (settings.containsKey(key))
			throw new IllegalArgumentException("Duplicate setting: " + name + " " + key);
		
		settings.put(key, setting);
	}
	
	public final boolean isEnabled() {
		return enabled;
	}
	
	public final boolean isSupressed() {
		return suppressed;
	}
	
	public final boolean isLimited() {
		return limited;
	}
	
	public final void setEnabled(boolean enabled) {
		if (this.enabled == enabled)
			return;
		
		if (this.suppressed && enabled)
			return;
		
		this.enabled = enabled;
		
		if (enabled)
			onEnable();
		else
			onDisable();
		
		if (stateSaved)
			wurst.getFeatures().saveEnabledFeatures();
	}
	
	public final void setSuppressed(boolean supressed) {
		if (this.suppressed == supressed)
			return;
		
		this.suppressed = supressed;
		
		if (!this.enabled)
			return;
		else if (suppressed)
			onDisable();
		else
			onEnable();
	}
	
	public final boolean isStateSaved() {
		return stateSaved;
	}
	
	protected void onEnable() {}
	
	protected void onDisable() {}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface DontSaveState {}
}
