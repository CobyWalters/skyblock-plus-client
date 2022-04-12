/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.features;

import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WGetAmbientOcclusionLightValueEvent;
import net.wurstclient.fmlevents.WRenderBlockModelEvent;
import net.wurstclient.fmlevents.WRenderTileEntityEvent;
import net.wurstclient.fmlevents.WSetOpaqueCubeEvent;
import net.wurstclient.fmlevents.WShouldSideBeRenderedEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.settings.BlockListSetting;
import net.wurstclient.forge.utils.BlockUtils;

public class XRay extends Feature {
	
	private BlockListSetting blocks =
		new BlockListSetting("Blocks",
							 Blocks.COAL_ORE, Blocks.COAL_BLOCK, Blocks.IRON_ORE, Blocks.IRON_BLOCK,
							 Blocks.GOLD_ORE, Blocks.GOLD_BLOCK, Blocks.LAPIS_ORE, Blocks.LAPIS_BLOCK,
							 Blocks.REDSTONE_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.REDSTONE_BLOCK,
							 Blocks.DIAMOND_ORE, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_ORE,
							 Blocks.EMERALD_BLOCK, Blocks.QUARTZ_ORE, Blocks.LAVA, Blocks.MOB_SPAWNER,
							 Blocks.PORTAL, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME);
	
	private ArrayList<String> blockNames;
	
	public XRay() {
		super("X-Ray", "Allows you to see ores through walls.", true);
		setCategory(Category.RENDER);
		addSetting(blocks);
	}
	
	@Override
	public String getRenderName() {
		return "X-Ray";
	}
	
	@Override
	protected void onEnable() {
		blockNames = new ArrayList<>(blocks.getBlockNames());
		mc.renderGlobal.loadRenderers();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
		mc.renderGlobal.loadRenderers();
		
		if (!wurst.getFeatures().fullbright.isEnabled())
			mc.gameSettings.gammaSetting = 0.5F;
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		mc.gameSettings.gammaSetting = 16;
	}
	
	@SubscribeEvent
	public void onSetOpaqueCube(WSetOpaqueCubeEvent event) {
		event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onGetAmbientOcclusionLightValue(WGetAmbientOcclusionLightValueEvent event) {
		event.setLightValue(1);
	}
	
	@SubscribeEvent
	public void onShouldSideBeRendered(WShouldSideBeRenderedEvent event) {
		event.setRendered(isVisible(event.getState().getBlock()));
	}
	
	@SubscribeEvent
	public void onRenderBlockModel(WRenderBlockModelEvent event) {
		if (!isVisible(event.getState().getBlock()))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onRenderTileEntity(WRenderTileEntityEvent event)  {
		if (!isVisible(event.getTileEntity().getBlockType()))
			event.setCanceled(true);
	}
	
	private boolean isVisible(Block block) {
		String name = BlockUtils.getName(block);
		int index = Collections.binarySearch(blockNames, name);
		return index >= 0;
	}
	
}