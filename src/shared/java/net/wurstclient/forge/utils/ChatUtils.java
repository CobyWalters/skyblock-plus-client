/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public final class ChatUtils {

	public static void component(ITextComponent component) {
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(
			new TextComponentString("\u00a7c[\u00a76Skyblock+\u00a7c]\u00a7r ").appendSibling(component));
	}
	
	public static void message(String message) {
		component(new TextComponentString(message));
	}
	
	public static void debugMessage(String message) {
		message(message.replace("",  " "));
	}
	
	public static void warning(String message) {
		message("\u00a7c[\u00a76\u00a7lWARNING\u00a7c]\u00a7r " + message);
	}
	
	public static void error(String message) {
		message("\u00a7c[\u00a74\u00a7lERROR\u00a7c]\u00a7r " + message);
	}
	
	public static void sendMessage(EntityPlayerSP player, String message) {
		player.sendChatMessage(message);
	}
	
	public static String getUnformattedText(String text) {
		for (int i = 0; i < text.length() - 1; ++i)
			if ((int) text.charAt(i) == 167 && isFormattingCode(text.charAt(i + 1)))
				text = text.substring(0, i) + text.substring(i + 2);
		return text;
	}
	
	private static boolean isFormattingCode(char c) {
		return Character.digit(c, 16) != -1 || c == 'k' || c == 'l' || c == 'm' || c == 'n' || c == 'o' || c == 'r';
	}
}
