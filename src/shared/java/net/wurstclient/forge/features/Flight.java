/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.features;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;

public class Flight extends Feature {
	
	private SliderSetting speed = new SliderSetting("Speed", 1, 0.05, 5, 0.05, ValueDisplay.DECIMAL);
	
	public Flight() {
		super("Flight",
			  "Allows you to fly.\n\n" +
			   "\u00a7c\u00a7lWARNING:\u00a7r You will take fall damage\n" +
			   "if you have not enabled /fly.\n\n" + 
			   "Requires /fly in skyblock servers.",
			  true);
		setCategory(Category.MOVEMENT);
		addSetting(speed);
	}
	
	@Override
	public String getRenderName() {
		return getName() + " [" + speed.getValueString() + "]";
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
	public void onUpdate(WUpdateEvent event) {
		EntityPlayerSP player = event.getPlayer();
		
		player.capabilities.isFlying = false;
		player.motionX = 0;
		player.motionY = 0;
		player.motionZ = 0;
		player.jumpMovementFactor = speed.getValueF();
		
		if (mc.gameSettings.keyBindJump.isKeyDown())
			player.motionY += speed.getValue();
		if (mc.gameSettings.keyBindSneak.isKeyDown())
			player.motionY -= speed.getValue();
	}
	
}