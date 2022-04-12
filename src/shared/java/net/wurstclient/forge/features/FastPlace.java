/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.features;

import java.util.Arrays;
import java.lang.reflect.Field;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.BlockListSetting;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.utils.BlockUtils;

public class FastPlace extends Feature {
	
	private EnumSetting<Orientation> orientation1 =
			new EnumSetting<>("Orientation 1", Orientation.values(), Orientation.DEFAULT);
	private CheckboxSetting restrictOrientation1 = 
			new CheckboxSetting("Enable orientation 1", "Allows placement with\n" + "orientation 1", false);
	private EnumSetting<Orientation> orientation2 =
			new EnumSetting<>("Orientation 2", Orientation.values(), Orientation.DEFAULT);
	private CheckboxSetting restrictOrientation2 =
			new CheckboxSetting("Enable orientation 2", "Allows placement with\n" + "orientation 2", false);
	private BlockListSetting blocks =
			new BlockListSetting("Only place on");
	private CheckboxSetting restrictPlacement =
			new CheckboxSetting("Block whitelist", 
								"Restricts placement to\n" +
								 "blocks on the list",
								false);

	public FastPlace() {
		super("FastPlace", "Allows you to place blocks 5 times faster.", true);
		setCategory(Category.UTILITY);
		addSetting(orientation1);
		addSetting(restrictOrientation1);
		addSetting(orientation2);
		addSetting(restrictOrientation2);
		addSetting(blocks);
		addSetting(restrictPlacement);
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
	public void onClientTick(ClientTickEvent event) {
		
		if (event.phase == Phase.START) {
			EntityPlayerSP player = WMinecraft.getPlayer();
			
			if (player == null || !(player.inventory.getCurrentItem().getItem() instanceof ItemBlock))
				return;

			RayTraceResult r = mc.objectMouseOver;
			Block b = (r != null && r.typeOfHit == Type.BLOCK) ?
				BlockUtils.getBlock(r.getBlockPos()) : null;
			if (b == null)
				return;
			
			if (!mc.gameSettings.keyBindSneak.isKeyDown() && !BlockUtils.canPlaceOn(b))
				return;
			
			boolean isValidOrientation = 
				(!restrictOrientation1.isChecked() && !restrictOrientation2.isChecked()) ||
				(restrictOrientation1.isChecked() && orientation1.getSelected().validate(r)) ||
				(restrictOrientation2.isChecked() && orientation2.getSelected().validate(r));
			boolean isValidBlock = 
				!restrictPlacement.isChecked() || 
				blocks.getBlockNames().indexOf(BlockUtils.getName(b)) != -1;

			try {
				Field rightClickDelayTimer = mc.getClass().getDeclaredField(
					wurst.isObfuscated() ? "field_71467_ac" : "rightClickDelayTimer");
				rightClickDelayTimer.setAccessible(true);
				if (rightClickDelayTimer.getInt(mc) > 0 && isValidOrientation && isValidBlock) {
					rightClickDelayTimer.setInt(mc, 0);
				} else {
					rightClickDelayTimer.setInt(mc, 4);
				}
			} catch(ReflectiveOperationException e) {
				setEnabled(false);
				throw new RuntimeException(e);
			}
		}
	}

	private enum Orientation {
		
		DEFAULT("All sides", r -> true),
		
		NORTH("North of", r -> r.sideHit == EnumFacing.NORTH),
		
		EAST("East of", r -> r.sideHit == EnumFacing.EAST),
		
		SOUTH("South of", r -> r.sideHit == EnumFacing.SOUTH),
		
		WEST("West of", r -> r.sideHit == EnumFacing.WEST),
		
		ABOVE("Above", r -> r.sideHit == EnumFacing.UP),
	
		BELOW("Below", r -> r.sideHit == EnumFacing.DOWN),
		
		BESIDE("Beside", r -> Arrays.asList(EnumFacing.HORIZONTALS).indexOf(r.sideHit) != -1);
		
		private String name;
		private Predicate<RayTraceResult> validator;
		
		private Orientation(String name, Predicate<RayTraceResult> validator) {
			this.name = name;
			this.validator = validator;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public boolean validate(RayTraceResult r) {
			return r != null && r.typeOfHit == Type.BLOCK && validator.test(r);
		}
	}
	
}