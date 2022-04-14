/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.wurstclient.forge.clickgui.ClickGui;

@Mod(modid = ForgeWurst.MODID,
	version = ForgeWurst.VERSION)
public final class ForgeWurst {
	
	public static final String MODID = "skyblockplus";
	public static final String VERSION = "0.2.1";
	
	@Instance(MODID)
	private static ForgeWurst forgeWurst;
	
	private boolean obfuscated;
	
	private Path configFolder;
	
	private FeatureList features;
	private CommandList cmds;
	private KeybindList keybinds;
	private ClickGui gui;	
	private IngameHUD hud;
	private CommandProcessor cmdProcessor;
	private KeybindProcessor keybindProcessor;
	private FeatureController featureController;
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		
		if (event.getSide() == Side.SERVER)
			return;
		
		String mcClassName = Minecraft.class.getName().replace(".", "/");
		FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
		obfuscated = !mcClassName.equals(remapper.unmap(mcClassName));
		
		configFolder =
			Minecraft.getMinecraft().mcDataDir.toPath().resolve("wurst");
		try {
			Files.createDirectories(configFolder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		features = new FeatureList(configFolder.resolve("enabled-features.json"), configFolder.resolve("settings.json"));
		features.loadEnabledFeatures();
		features.loadSettings();
		
		cmds = new CommandList();
		
		keybinds = new KeybindList(configFolder.resolve("keybinds.json"));
		keybinds.init();
		
		gui = new ClickGui(configFolder.resolve("windows.json"));
		gui.init(features);
		
		featureController = new FeatureController();
		MinecraftForge.EVENT_BUS.register(featureController);
		
		hud = new IngameHUD(features, gui);
		MinecraftForge.EVENT_BUS.register(hud);
		
		cmdProcessor = new CommandProcessor(cmds);
		MinecraftForge.EVENT_BUS.register(cmdProcessor);
		
		keybindProcessor = new KeybindProcessor(features, keybinds, cmdProcessor);
		MinecraftForge.EVENT_BUS.register(keybindProcessor);
	}
	
	public static ForgeWurst getForgeWurst() {
		return forgeWurst;
	}
	
	public FeatureController getFeatureController() {
		return featureController;
	}
	
	public boolean isObfuscated() {
		return obfuscated;
	}
	
	public FeatureList getFeatures() {
		return features;
	}
	
	public CommandList getCmds() {
		return cmds;
	}
	
	public KeybindList getKeybinds() {
		return keybinds;
	}
	
	public ClickGui getGui() {
		return gui;
	}
}
