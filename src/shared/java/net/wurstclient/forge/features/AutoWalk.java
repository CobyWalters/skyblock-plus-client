/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.features;

import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.utils.KeyBindingUtils;

public class AutoWalk extends Feature {
	
	private CheckboxSetting bunnyHop = new CheckboxSetting("Jump while sprinting", false);
	
	public AutoWalk() {
		super("AutoWalk", "Makes you walk automatically.", true);
		setCategory(Category.MOVEMENT);
		addSetting(bunnyHop);
	}
	
	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
		KeyBindingUtils.resetPressed(mc.gameSettings.keyBindForward);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		KeyBindingUtils.setPressed(mc.gameSettings.keyBindForward, true);
		if (bunnyHop.isChecked()) {
			EntityPlayerSP player = event.getPlayer();
			if (!player.onGround || player.isSneaking() || player.isInsideOfMaterial(Material.WATER))
				return;
			
			if (player.isSprinting() && (player.moveForward != 0 || player.moveStrafing != 0))
				player.jump();
		}
	}
	
}