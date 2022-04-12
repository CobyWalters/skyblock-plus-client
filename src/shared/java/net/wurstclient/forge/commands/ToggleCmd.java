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

public final class ToggleCmd extends Command {
	
	public ToggleCmd() {
		super("t", "Toggles a feature.", "Syntax: .t <feature> [on|off]");
	}
	
	@Override
	public void call(String[] args) throws CmdException {
		if (args.length < 1 || args.length > 2)
			throw new CmdSyntaxError();
		
		Feature feature = wurst.getFeatures().get(args[0]);
		if (feature == null)
			throw new CmdError("Unknown feature: " + args[0]);
		
		if (args.length == 1)
			feature.setEnabled(!feature.isEnabled());
		else
			switch(args[1].toLowerCase()) {
				case "on":
				feature.setEnabled(true);
				break;
				
				case "off":
				feature.setEnabled(false);
				break;
				
				default:
				throw new CmdSyntaxError();
			}
	}
}
