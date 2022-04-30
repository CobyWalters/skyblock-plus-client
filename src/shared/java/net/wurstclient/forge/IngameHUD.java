/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.util.ArrayList;
import java.util.Comparator;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.clickgui.ClickGui;
import net.wurstclient.forge.clickgui.ClickGuiScreen;
import net.wurstclient.forge.compatibility.WMinecraft;

public final class IngameHUD {
	
	private final Minecraft mc = Minecraft.getMinecraft();
	private final FeatureList featureList;
	private final ClickGui clickGui;
	
	public IngameHUD(FeatureList featureList, ClickGui clickGui) {
		this.featureList = featureList;
		this.clickGui = clickGui;
	}
	
	@SubscribeEvent
	public void onRenderGUI(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.ALL || mc.gameSettings.showDebugInfo)
			return;
		
		boolean blend = GL11.glGetBoolean(GL11.GL_BLEND);
		clickGui.updateColors();
		
		if (!featureList.overlaySettings.isOverlayDisabled()) {
			// color
			int textColor1 = 0xffffff;
			int textColor2 = 0xaaaaaa;

			// overlay settings
			double scale = featureList.overlaySettings.getScale() - 0.01;
			double xOffset = featureList.overlaySettings.getXOffset();
			double yOffset = featureList.overlaySettings.getYOffset();
			String alignment = featureList.overlaySettings.getAlignment();
			
			ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
			
			ArrayList<Feature> features = new ArrayList<>();
			features.addAll(featureList.getValues());
			features.removeIf((Feature f) -> !f.isEnabled());
			
			// calculate overlay width for alignment purposes
			features.sort(Comparator.comparing((Feature f) -> -WMinecraft.getFontRenderer().getStringWidth(f.getRenderName())));
			int biggestFeatureWidth = features.size() > 0 ? WMinecraft.getFontRenderer().getStringWidth(features.get(0).getRenderName()) : 0;
			biggestFeatureWidth *= scale;
			
			int titleWidth = WMinecraft.getFontRenderer().getStringWidth("Skyblock+ v" + ForgeWurst.VERSION);
			titleWidth *= 1.3 * scale;
			
			int overlayWidth = Math.max(biggestFeatureWidth, titleWidth);
			int overlayHeight = (int) ((14 + 9 * features.size()) * scale * scale - (6 + 5 * features.size()) * scale + 6 + 5 * features.size());
			
			// calculate starting x and y positions
			int titleOffset = alignment.equals("LEFT") ? 0 :
			      			  alignment.equals("RIGHT") ? overlayWidth - titleWidth : (overlayWidth - titleWidth) / 2;
			double x = 3 + xOffset * (sr.getScaledWidth() - overlayWidth - 6);
			double y = 3 + yOffset * (sr.getScaledHeight() - overlayHeight - 6);
			double scaledX = (x + titleOffset) / (scale * 1.3);
			double scaledY = y / (scale * 1.3);
			
			// draw title
			GL11.glPushMatrix();
			GL11.glScaled(scale * 1.3, scale * 1.3, 1);
			WMinecraft.getFontRenderer().drawStringWithShadow("Skyblock+ v" + ForgeWurst.VERSION, (float) (scaledX), (float) (scaledY), textColor1);
			GL11.glPopMatrix();
			y += 14 * scale * scale - 6 * scale + 6;

			// draw enabled features
			features.sort(Comparator.comparing(Feature::getName));
			GL11.glPushMatrix();
			GL11.glScaled(scale, scale, 1);
			for (Feature feature : features) {
				int featureWidth = WMinecraft.getFontRenderer().getStringWidth(feature.getRenderName());
				featureWidth *= scale;
				int featureOffset = alignment.equals("LEFT") ? 0 :
	      			  			    alignment.equals("RIGHT") ? overlayWidth - featureWidth : (overlayWidth - featureWidth) / 2;
				int color = feature.isSupressed() ? textColor2 : textColor1;
				scaledX = (x + featureOffset) / scale;
				scaledY = y / scale;
				WMinecraft.getFontRenderer().drawStringWithShadow(feature.getRenderName(), (float) (scaledX), (float) (scaledY), color);
				y += 9 * scale * scale - 5 * scale + 5;
			}
			GL11.glPopMatrix();
		}

		// pinned windows
		if (!(mc.currentScreen instanceof ClickGuiScreen))
			clickGui.renderPinnedWindows(event.getPartialTicks());
		
		if (blend)
			GL11.glEnable(GL11.GL_BLEND);
		else
			GL11.glDisable(GL11.GL_BLEND);
	}
}
