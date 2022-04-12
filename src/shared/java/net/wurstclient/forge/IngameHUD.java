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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.clickgui.ClickGui;
import net.wurstclient.forge.clickgui.ClickGuiScreen;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;

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
		
		CheckboxSetting disableOverlaySetting = (CheckboxSetting) ForgeWurst.getForgeWurst().getFeatures().clickGui.getSettings().get("disable overlay");
		
		if (!disableOverlaySetting.isChecked()) {
			// color
			clickGui.updateColors();
			int textColor1 = 0xffffff;
			int textColor2 = 0xaaaaaa;
			
			// title
			GL11.glPushMatrix();
			GL11.glScaled(1.33333333, 1.33333333, 1);
			WMinecraft.getFontRenderer().drawStringWithShadow("Skyblock+ v" + ForgeWurst.VERSION, 3, 3, textColor1);
			GL11.glPopMatrix();
			
			// feature list
			int y = 19;
			ArrayList<Feature> features = new ArrayList<>();
			features.addAll(featureList.getValues());
			features.sort(Comparator.comparing(Feature::getName));
			
			for (Feature feature : features) {
				if (!feature.isEnabled())
					continue;
				if (feature.isSupressed())
					WMinecraft.getFontRenderer().drawStringWithShadow(feature.getRenderName(), 2, y, textColor2);
				else
					WMinecraft.getFontRenderer().drawStringWithShadow(feature.getRenderName(), 2, y, textColor1);
				y += 9;
			}
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
