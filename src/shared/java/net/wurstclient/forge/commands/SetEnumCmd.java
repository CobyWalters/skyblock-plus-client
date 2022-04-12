/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import net.wurstclient.forge.Command;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.Setting;

public final class SetEnumCmd extends Command {
	
	public SetEnumCmd() {
		super("setenum", "Modifies an enum setting.", "Syntax: .setenum <feature> <enum> <value>");
	}
	
	@Override
	public void call(String[] args) throws CmdException {
		
		if (args.length != 3)
			throw new CmdSyntaxError();
		
		Feature feature = wurst.getFeatures().get(args[0]);
		if (feature == null)
			throw new CmdError("Feature \"" + args[0] + "\" could not be found.");
		
		Setting setting =
			feature.getSettings().get(args[1].toLowerCase().replace("_", " "));
		if (setting == null)
			throw new CmdError("Setting \"" + args[0] + " " + args[1] + "\" could not be found.");
		
		if (!(setting instanceof EnumSetting))
			throw new CmdError(feature.getName() + " " + setting.getName() + " is not an enum.");
		EnumSetting<?> e = (EnumSetting<?>)setting;
		
		e.setSelected(args[2].replace("_", " "));
	}
	
}
