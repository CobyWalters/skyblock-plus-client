/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.wurstclient.forge.compatibility.WFeatureList;
import net.wurstclient.forge.features.*;
import net.wurstclient.forge.settings.Setting;
import net.wurstclient.forge.utils.JsonUtils;

public final class FeatureList extends WFeatureList {
	
	public final AutoAdvert autoAdvert = register(new AutoAdvert());
	public final AutoClaim autoClaim = register(new AutoClaim());
	public final AutoEat autoEat = register(new AutoEat());
	public final AutoFarm autoFarm = register(new AutoFarm());
	public final AutoFish autoFish = register(new AutoFish());
	public final AutoMine autoMine = register(new AutoMine());
	public final AutoReconnect autoReconnect = register(new AutoReconnect());
	public final AutoRepair autoRepair = register(new AutoRepair());
	public final AutoSellAll autoSellAll = register(new AutoSellAll());
	public final AutoShear autoShear = register(new AutoShear());
	public final AutoSprint autoSprint = register(new AutoSprint());
	public final AutoTool autoTool = register(new AutoTool());
	public final AutoWalk autoWalk = register(new AutoWalk());
	public final BonemealAura bonemealAura = register(new BonemealAura());
	public final BetterChat betterChat = register(new BetterChat());
	public final FastPlace fastPlace = register(new FastPlace());
	public final Flight flight = register(new Flight());
	public final Freecam freecam = register(new Freecam());
	public final Fullbright fullbright = register(new Fullbright());
	public final GuardianAngel guardianAngel = register(new GuardianAngel());
	public final ItemEsp itemEsp = register(new ItemEsp());
	public final Killaura killaura = register(new Killaura());
	public final MobEsp mobEsp = register(new MobEsp());
	public final MobSpawnEsp mobSpawnEsp = register(new MobSpawnEsp());
	public final PlayerEsp playerEsp = register(new PlayerEsp());
	public final TPSDisplay tpsDisplay = register(new TPSDisplay());
	public final XRay xRay = register(new XRay());
	
	public final UISettings uiSettings = register(new UISettings());
	public final OverlaySettings overlaySettings = register(new OverlaySettings());
	public final OpenGUI openGUI = register(new OpenGUI());
	
	private final Path enabledFeaturesFile;
	private final Path settingsFile;
	private boolean disableSaving;
	
	public FeatureList(Path enabledFeaturesFile, Path settingsFile) {
		this.enabledFeaturesFile = enabledFeaturesFile;
		this.settingsFile = settingsFile;
	}
	
	public void loadEnabledFeatures() {
		JsonArray json;
		try (BufferedReader reader = Files.newBufferedReader(enabledFeaturesFile)) {
			json = JsonUtils.jsonParser.parse(reader).getAsJsonArray();
		} catch (NoSuchFileException e) {
			saveEnabledFeatures();
			return;	
		} catch (Exception e) {
			System.out
				.println("Failed to load " + enabledFeaturesFile.getFileName());
			e.printStackTrace();
			
			saveEnabledFeatures();
			return;
		}
		
		disableSaving = true;
		for (JsonElement e : json) {
			if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString())
				continue;
			
			Feature feature = get(e.getAsString());
			if (feature == null || !feature.isStateSaved())
				continue;
			
			feature.setEnabled(true);
		}
		disableSaving = false;
		
		saveEnabledFeatures();
	}
	
	public void saveEnabledFeatures() {
		if (disableSaving)
			return;
		
		JsonArray enabledFeatures = new JsonArray();
		for (Feature features : getRegistry())
			if (features.isEnabled() && features.isStateSaved())
				enabledFeatures.add(new JsonPrimitive(features.getName()));
			
		try (BufferedWriter writer = Files.newBufferedWriter(enabledFeaturesFile)) {
			JsonUtils.prettyGson.toJson(enabledFeatures, writer);
			
		} catch (IOException e) {
			System.out
				.println("Failed to save " + enabledFeaturesFile.getFileName());
			e.printStackTrace();
		}
	}
	
	public void loadSettings() {
		JsonObject json;
		try(BufferedReader reader = Files.newBufferedReader(settingsFile)) {
			json = JsonUtils.jsonParser.parse(reader).getAsJsonObject();
			
		} catch(NoSuchFileException e) {
			saveSettings();
			return;
		} catch(Exception e) {
			System.out.println("Failed to load " + settingsFile.getFileName());
			e.printStackTrace();
			saveSettings();
			return;
		}
		
		disableSaving = true;
		for (Entry<String, JsonElement> e : json.entrySet()) {
			if (!e.getValue().isJsonObject())
				continue;
			
			Feature feature = get(e.getKey());
			if (feature == null)
				continue;
			
			Map<String, Setting> settings = feature.getSettings();
			for(Entry<String, JsonElement> e2 : e.getValue().getAsJsonObject()
				.entrySet()) {
				String key = e2.getKey().toLowerCase();
				if(!settings.containsKey(key))
					continue;
				
				settings.get(key).fromJson(e2.getValue());
			}
		}
		disableSaving = false;
		
		saveSettings();
	}
	
	public void saveSettings() {
		if (disableSaving)
			return;
		
		JsonObject json = new JsonObject();
		for (Feature feature : getRegistry()) {
			if (feature.getSettings().isEmpty())
				continue;
			
			JsonObject settings = new JsonObject();
			for (Setting setting : feature.getSettings().values())
				settings.add(setting.getName(), setting.toJson());
			
			json.add(feature.getName(), settings);
		}
		
		try (BufferedWriter writer = Files.newBufferedWriter(settingsFile)) {
			JsonUtils.prettyGson.toJson(json, writer);
			
		} catch (IOException e){
			System.out.println("Failed to save " + settingsFile.getFileName());
			e.printStackTrace();
		}
	}
}
