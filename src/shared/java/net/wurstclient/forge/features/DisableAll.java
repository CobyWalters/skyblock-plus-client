package net.wurstclient.forge.features;

import java.lang.reflect.Field;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.FeatureList;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.InventoryUtils;
import net.wurstclient.forge.utils.RotationUtils;

public class DisableAll extends Feature {
		
	public DisableAll() {
		super("DisableAll", "Toggles off all hacks", true);
		setCategory(Category.UTILITY);
	}
	
	@Override
	protected void onEnable() {
		for (Feature f : wurst.getFeatures().getValues())
			if (f.isEnabled())
				f.setEnabled(false);
	}
	
	@Override
	protected void onDisable() {
		
	}
	
}