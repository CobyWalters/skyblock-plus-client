package net.wurstclient.forge.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraft.item.Item;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.clickgui.Component;
import net.wurstclient.forge.clickgui.ItemListEditButton;
import net.wurstclient.forge.utils.InventoryUtils;

public final class ItemListSetting extends Setting {
	
	private final ArrayList<String> itemNames = new ArrayList<>();
	private final String[] defaultNames;
	
	public ItemListSetting(String name, String description, Item... items) {
		super(name, description);
		Arrays.stream(items).parallel()
			.map(i -> Integer.toString(Item.getIdFromItem(i)))
			.distinct().sorted().forEachOrdered(s -> itemNames.add(s));
		defaultNames = itemNames.toArray(new String[0]);
	}
	
	public ItemListSetting(String name, Item... items) {
		this(name, null, items);
	}
	
	public List<String> getItemNames() {
		return Collections.unmodifiableList(itemNames);
	}
	
	public void add(Item item) {
		String name = Integer.toString(InventoryUtils.getId(item));
		if (Collections.binarySearch(itemNames, name) >= 0)
			return;
		itemNames.add(name);
		Collections.sort(itemNames);
		ForgeWurst.getForgeWurst().getFeatures().saveSettings();
	}
	
	public void remove(int index) {
		if (index < 0 || index >= itemNames.size())
			return;
		
		itemNames.remove(index);
		ForgeWurst.getForgeWurst().getFeatures().saveSettings();
	}
	
	public void resetToDefaults() {
		itemNames.clear();
		itemNames.addAll(Arrays.asList(defaultNames));
		ForgeWurst.getForgeWurst().getFeatures().saveSettings();
	}
	
	@Override
	public Component getComponent() {
		return new ItemListEditButton(this);
	}
	
	@Override
	public void fromJson(JsonElement json) {
		
		if (!json.isJsonArray())
			return;
		
		itemNames.clear();
		StreamSupport.stream(json.getAsJsonArray().spliterator(), true)
			.filter(e -> e.isJsonPrimitive())
			.filter(e -> e.getAsJsonPrimitive().isString())
			.map(e -> Item.getByNameOrId(e.getAsString())).filter(Objects::nonNull)
			.map(i -> Integer.toString(Item.getIdFromItem(i)))
			.distinct().sorted().forEachOrdered(s -> itemNames.add(s));
	}
	
	@Override
	public JsonElement toJson() {
		JsonArray json = new JsonArray();
		itemNames.forEach(s -> json.add(new JsonPrimitive(s)));
		return json;
	}
}
