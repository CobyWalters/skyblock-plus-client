package net.wurstclient.forge.features;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;

public class TPSDisplay extends Feature {

	private long prevTime;
	private double tps = 20;
	
	public TPSDisplay() {
		super("TPS", "Displays the TPS (ticks per second) in the top left corner.", false);
		setCategory(Category.UTILITY);
	}
	
	@Override
	public String getRenderName() {
		return getName() + ": " + tps;
	}
	
	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
		tps = 20;
	}
	
	@SubscribeEvent
	public void readPacket(WPacketInputEvent event) {
		if (event.getPacket() instanceof SPacketTimeUpdate) {
			long time = System.currentTimeMillis();
			long timeOffset = Math.abs(1000 - (time - prevTime)) + 1000;
			tps = Math.round(MathHelper.clamp(20 / (timeOffset / 1000d), 0, 20) * 100d) / 100d;
			prevTime = time;
		}
	}
	
}