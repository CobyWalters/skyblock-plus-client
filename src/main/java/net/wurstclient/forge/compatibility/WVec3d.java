/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import net.minecraft.util.math.Vec3d;

public final class WVec3d {
	
	public static double getX(Vec3d vec) {
		return vec.x;
	}
	
	public static double getY(Vec3d vec) {
		return vec.y;
	}
	
	public static double getZ(Vec3d vec) {
		return vec.z;
	}
}
