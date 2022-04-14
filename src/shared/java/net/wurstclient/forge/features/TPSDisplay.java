package net.wurstclient.forge.features;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;

public class TPSDisplay extends Feature {

	private final SliderSetting scale = new SliderSetting("Overlay scale", 1, 0.50, 1, 0.01, ValueDisplay.PERCENTAGE);
	private final SliderSetting xOffset = new SliderSetting("Overlay X offset", 0, 0, 1, 0.01, ValueDisplay.PERCENTAGE);
	private final SliderSetting yOffset = new SliderSetting("Overlay Y offset", 0, 0, 1, 0.01, ValueDisplay.PERCENTAGE);
	
	private long prevTime;
	private double tps = 20;
	
	public TPSDisplay() {
		super("TPS", "Displays the TPS (ticks per second) in the top left corner.", false);
		setCategory(Category.UTILITY);
		addSetting(scale);
		addSetting(xOffset);
		addSetting(yOffset);
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
	
	@SubscribeEvent
	public void onRenderGUI(RenderGameOverlayEvent.Post event) {
		
		if (event.getType() != ElementType.ALL || mc.gameSettings.showDebugInfo)
			return;
		
		int textColor = 0xffffff;
		double tpsScale = scale.getValue();
		double tpsXOffset = xOffset.getValue();
		double tpsYOffset = yOffset.getValue();
		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		
		GL11.glPushMatrix();
		GL11.glScaled(tpsScale, tpsScale, 1);
		int tpsWidth = WMinecraft.getFontRenderer().getStringWidth("TPS: " + tps);
		int tpsHeight = (int) (9 * tpsScale * tpsScale - 5 * tpsScale + 5);
		double x = 3 + tpsXOffset * (sr.getScaledWidth() - tpsWidth - 6);
		double y = 3 + tpsYOffset * (sr.getScaledHeight() - tpsHeight - 6);
		WMinecraft.getFontRenderer().drawStringWithShadow("TPS: " + tps, (float) (x / tpsScale), (float) (y / tpsScale), textColor);
		GL11.glPopMatrix();
	}
	
	public double getTPS() {
		return tps;
	}
	
}