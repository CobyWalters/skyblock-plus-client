/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import java.util.Collection;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.Feature;

public abstract class WFeatureList {
	
	private static IForgeRegistry<Feature> registry; {
		if (registry != null)
			throw new IllegalStateException("Multiple instances of featureList!");
		
		RegistryBuilder<Feature> registryBuilder = new RegistryBuilder<>();
		registryBuilder.setName(new ResourceLocation(ForgeWurst.MODID, "features"));
		registryBuilder.setType(Feature.class);
		registryBuilder.disableSaving();
		registry = registryBuilder.create();
	}
	
	protected final <T extends Feature> T register(T feature) {
		feature.setRegistryName(ForgeWurst.MODID, feature.getName().toLowerCase());
		registry.register(feature);
		return feature;
	}
	
	public final IForgeRegistry<Feature> getRegistry() {
		return registry;
	}
	
	@SuppressWarnings("deprecation")
	public final Collection<Feature> getValues() {
		try {
			return registry.getValuesCollection();
			
		} catch (NoSuchMethodError e) {
			return registry.getValues();
		}
	}
	
	public final Feature get(String name) {
		ResourceLocation location =
			new ResourceLocation(ForgeWurst.MODID, name.toLowerCase());
		return registry.getValue(location);
	}
}
