package net.wurstclient.forge.features;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.FeatureList;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.TextInputSetting;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.InventoryUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WChatInputEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;

import static java.nio.file.StandardOpenOption.*;

public class Debug extends Feature {
	
	public Debug() {
		super("Debug", "Helps with debugging.", false);
		setCategory(Category.SKYBLOCK);
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
	public void onPacketInput(WPacketInputEvent event) {
		ChatUtils.message("WOAH");
	}
	
}