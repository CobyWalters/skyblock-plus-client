/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.features;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.utils.ChatUtils;

public class Fullbright extends Feature {
	
	public Fullbright() {
		super("Fullbright", "Allows you to see in the dark.", false);
		setCategory(Category.RENDER);
	}
	
	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		if (isEnabled()) {
			if (mc.gameSettings.gammaSetting < 16)
				mc.gameSettings.gammaSetting = Math.min(mc.gameSettings.gammaSetting * 1.1F, 16);
			return;
		}
		mc.gameSettings.gammaSetting = Math.min(mc.gameSettings.gammaSetting, 16);
		if (mc.gameSettings.gammaSetting > 1F)
			mc.gameSettings.gammaSetting = Math.max(mc.gameSettings.gammaSetting * 0.9F, 1F);
		else
			MinecraftForge.EVENT_BUS.unregister(this);
	}
}
