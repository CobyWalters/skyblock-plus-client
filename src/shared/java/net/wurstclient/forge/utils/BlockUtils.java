/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.AbstractIterator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WBlock;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.compatibility.WPlayerController;
import net.wurstclient.forge.compatibility.WVec3d;

public final class BlockUtils {
	
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final List<Integer> fortuneIDs = Arrays.asList(16, 21, 56, 73, 74, 89, 129, 153);
	private static final List<Integer> interactableBlocks =
		Arrays.asList(23, 25, 54, 58, 61, 69, 77, 96, 107, 116, 130, 138, 143, 145, 146, 151, 154, 158, 183,
					  184, 185, 186, 187, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231,
					  232, 233, 234, 324, 355, 379, 427, 428, 429, 430, 431);
	public static IBlockState getState(BlockPos pos) {
		return WMinecraft.getWorld().getBlockState(pos);
	}
	
	public static Block getBlock(BlockPos pos) {
		return getState(pos).getBlock();
	}
	
	public static int getId(BlockPos pos) {
		return Block.getIdFromBlock(getBlock(pos));
	}
	
	public static String getName(Block block) {
		return "" + Block.REGISTRY.getNameForObject(block);
	}
	
	public static Material getMaterial(BlockPos pos) {
		return getState(pos).getMaterial();
	}
	
	public static AxisAlignedBB getBoundingBox(BlockPos pos) {
		return getState(pos).getBoundingBox(WMinecraft.getWorld(), pos).offset(pos);
	}
	
	public static boolean canBeClicked(BlockPos pos) {
		return getBlock(pos).canCollideCheck(getState(pos), false);
	}
	
	public static boolean canSilkHarvest(BlockPos pos) {
		return getBlock(pos).canSilkHarvest(mc.world, pos, getState(pos), mc.player);
	}
	
	public static boolean canFortuneHarvest(BlockPos pos) {
		return fortuneIDs.indexOf(getId(pos)) != -1;
	}

	public static float getHardness(BlockPos pos) {
		return getState(pos).getPlayerRelativeBlockHardness(WMinecraft.getPlayer(), WMinecraft.getWorld(), pos);
	}
	
	public static void placeBlockSimple(BlockPos pos) {
		EnumFacing side = null;
		EnumFacing[] sides = EnumFacing.values();
		
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
		double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
		
		Vec3d[] hitVecs = new Vec3d[sides.length];
		for (int i = 0; i < sides.length; i++)
			hitVecs[i] = posVec.add(new Vec3d(sides[i].getDirectionVec()).scale(0.5));
		
		for (int i = 0; i < sides.length; i++) {
			// check if neighbor can be right clicked
			if (!canBeClicked(pos.offset(sides[i])))
				continue;
			
			// check line of sight
			if (WMinecraft.getWorld().rayTraceBlocks(eyesPos, hitVecs[i], false, true, false) != null)
				continue;
			
			side = sides[i];
			break;
		}
		
		if (side == null) {
			for (int i = 0; i < sides.length; i++) {
				// check if neighbor can be right clicked
				if (!canBeClicked(pos.offset(sides[i])))
					continue;
				
				// check if side is facing away from player
				if (distanceSqPosVec > eyesPos.squareDistanceTo(hitVecs[i]))
					continue;
				
				side = sides[i];
				break;
			}
		}
		
		if (side == null)
			return;
		
		Vec3d hitVec = hitVecs[side.ordinal()];
		
		// face block
		RotationUtils.faceVectorPacket(hitVec);
		if (RotationUtils.getAngleToLastReportedLookVec(hitVec) > 1)
			return;
		
		// check timer
		try {
			Field rightClickDelayTimer = mc.getClass().getDeclaredField(
				ForgeWurst.getForgeWurst().isObfuscated() ? "field_71467_ac" : "rightClickDelayTimer");
			rightClickDelayTimer.setAccessible(true);
			
			if (rightClickDelayTimer.getInt(mc) > 0)
				return;
			
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		
		// place block
		WPlayerController.processRightClickBlock(pos.offset(side), side.getOpposite(), hitVec);
		
		// swing arm
		WMinecraft.getPlayer().connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
		
		// reset timer
		try {
			Field rightClickDelayTimer = mc.getClass().getDeclaredField(
				ForgeWurst.getForgeWurst().isObfuscated() ? "field_71467_ac" : "rightClickDelayTimer");
			rightClickDelayTimer.setAccessible(true);
			rightClickDelayTimer.setInt(mc, 4);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean breakBlockSimple(BlockPos pos) {
		
		EnumFacing side = null;
		EnumFacing[] sides = EnumFacing.values();
		
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d relCenter = getState(pos).getBoundingBox(WMinecraft.getWorld(), pos).getCenter();
		Vec3d center = new Vec3d(pos).add(relCenter);
		
		Vec3d[] hitVecs = new Vec3d[sides.length];
		for (int i = 0; i < sides.length; i++) {
			Vec3i dirVec = sides[i].getDirectionVec();
			Vec3d relHitVec = new Vec3d(WVec3d.getX(relCenter) * dirVec.getX(),
										WVec3d.getY(relCenter) * dirVec.getY(),
										WVec3d.getZ(relCenter) * dirVec.getZ());
			hitVecs[i] = center.add(relHitVec);
		}
		
		for (int i = 0; i < sides.length; i++) {
			// check line of sight
			if (WMinecraft.getWorld().rayTraceBlocks(eyesPos, hitVecs[i], false, true, false) != null)
				continue;
			
			side = sides[i];
			break;
		}
		
		if (side == null) {
			double distanceSqToCenter = eyesPos.squareDistanceTo(center);
			for (int i = 0; i < sides.length; i++) {
				// check if side is facing towards player
				if (eyesPos.squareDistanceTo(hitVecs[i]) >= distanceSqToCenter)
					continue;
				side = sides[i];
				break;
			}
		}
		
		// player is inside of block, side doesn't matter
		if (side == null)
			side = sides[0];
		
		// face block
		RotationUtils.faceVectorPacket(hitVecs[side.ordinal()]);
		
		// damage block
		if (!mc.playerController.onPlayerDamageBlock(pos, side))
			return false;
		
		// swing arm
		WMinecraft.getPlayer().connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
		
		return true;
	}
	
	public static void breakBlocksPacketSpam(Iterable<BlockPos> blocks) {
		
		Vec3d eyesPos = RotationUtils.getEyesPos();
		NetHandlerPlayClient connection = WMinecraft.getPlayer().connection;
		
		for(BlockPos pos : blocks) {
			Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
			double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
			
			for(EnumFacing side : EnumFacing.values()) {
				Vec3d hitVec =
					posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
				
				// check if side is facing towards player
				if(eyesPos.squareDistanceTo(hitVec) >= distanceSqPosVec)
					continue;
				
				// break block
				connection.sendPacket(new CPacketPlayerDigging(
					Action.START_DESTROY_BLOCK, pos, side));
				connection.sendPacket(new CPacketPlayerDigging(
					Action.STOP_DESTROY_BLOCK, pos, side));
				
				break;
			}
		}
	}
	
	public static Iterable<BlockPos> getValidBlocks(double blockRange, BlockValidator validator) {
		
		BlockPos playerPos = new BlockPos(RotationUtils.getEyesPos());
		BlockPos min = playerPos.add(-blockRange, -blockRange, -blockRange);
		BlockPos max = playerPos.add(blockRange, blockRange, blockRange);
		
		return () -> new AbstractIterator<BlockPos>() {
			private BlockPos last;
			private BlockPos computeNextUnchecked() {
				if(last == null) {
					last = min;
					return last;
				}
				
				int x = last.getX();
				int y = last.getY();
				int z = last.getZ();
				
				if (z < max.getZ())
					z++;
				else if (x < max.getX()) {
					z = min.getZ();
					x++;
				} else if (y < max.getY()) {
					z = min.getZ();
					x = min.getX();
					y++;
				} else
					return null;
				
				last = new BlockPos(x, y, z);
				return last;
			}
			
			@Override
			protected BlockPos computeNext() {
				BlockPos pos;
				while((pos = computeNextUnchecked()) != null) {
					// skip air blocks
					if(WBlock.getMaterial(pos) == Material.AIR)
						continue;
					
					// check if block is valid
					if(!validator.isValid(pos))
						continue;
					return pos;
				}
				return endOfData();
			}
		};
	}
	
	public static boolean rightClickBlockLegit(BlockPos pos) {
		
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
		double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
		
		for(EnumFacing side : EnumFacing.values()) {
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			double distanceSqHitVec = eyesPos.squareDistanceTo(hitVec);
			
			// check if hitVec is within range (4.25 blocks)
			if(distanceSqHitVec > 18.0625)
				continue;
			
			// check if side is facing towards player
			if(distanceSqHitVec >= distanceSqPosVec)
				continue;
			
			// check line of sight
			if(WMinecraft.getWorld().rayTraceBlocks(eyesPos, hitVec, false,
				true, false) != null)
				continue;
			
			// face block
			if(!RotationUtils.faceVectorPacket(hitVec))
				return true;
			
			// place block
			WPlayerController.processRightClickBlock(pos, side, hitVec);
			//WPlayer.swingArmClient();
			
			//mc.rightClickDelayTimer = 4;
			
			return true;
		}
		
		return false;
	}
	
	public static boolean canPlaceOn(Block b) {
		return !interactableBlocks.contains(Block.getIdFromBlock(b));
	}
	
	public static interface BlockValidator {
		public boolean isValid(BlockPos pos);
	}
	
}
