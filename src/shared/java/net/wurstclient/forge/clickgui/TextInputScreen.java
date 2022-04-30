package net.wurstclient.forge.clickgui;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.TextInputSetting;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.SkyblockUtils;

public final class TextInputScreen extends GuiScreen {
	
	private final GuiScreen prevScreen;
	private final TextInputSetting textInputSetting;
	
	private GuiTextField textInputField;
	
	private GuiButton clearButton;
	private GuiButton doneButton;
	
	public TextInputScreen(GuiScreen prevScreen, TextInputSetting setting) {
		this.prevScreen = prevScreen;
		textInputSetting = setting;
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void initGui() {
		textInputField = new GuiTextField(1, WMinecraft.getFontRenderer(), 100, height - 100, width - 200, 18);
		textInputField.setMaxStringLength(256);
		textInputField.setText(textInputSetting.getText());
		textInputField.setFocused(true);
		buttonList.add(clearButton = new GuiButton(0, width - 95, height - 101, 45, 20, "Clear"));
		buttonList.add(doneButton = new GuiButton(1, width / 2 - 100, height - 73, "Done"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled)
			return;
		
		switch (button.id) {
			case 0:
				textInputField.setText("");
				break;
			case 1:
				removeDisplayFormatting();
				textInputSetting.setText(textInputField.getText());
				mc.displayGuiScreen(prevScreen);
				break;
		}
	}
	
	@Override
	public void confirmClicked(boolean result, int id) {
		super.confirmClicked(result, id);
		mc.displayGuiScreen(this);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		textInputField.mouseClicked(mouseX, mouseY, mouseButton);
		removeDisplayFormatting();
		String rank = ForgeWurst.getForgeWurst().getFeatureController().getPlayerRank();
		if (!SkyblockUtils.canUseColoredChat(rank))
			return;
		addDisplayFormatting();
		addFormattingAfterCursor();
		addFormattingAfterLineScrollOffset();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		
		removeDisplayFormatting();
		textInputField.textboxKeyTyped(typedChar, keyCode);
		
		if (keyCode == Keyboard.KEY_RETURN)
			actionPerformed(doneButton);
		else if (keyCode == Keyboard.KEY_DELETE)
			actionPerformed(clearButton);
		else if (keyCode == Keyboard.KEY_ESCAPE)
			mc.displayGuiScreen(prevScreen);
		else {
			String rank = ForgeWurst.getForgeWurst().getFeatureController().getPlayerRank();
			if (!SkyblockUtils.canUseColoredChat(rank))
				return;
			addDisplayFormatting();
			addFormattingAfterCursor();
			addFormattingAfterLineScrollOffset();
		}

	}
	
	private void removeDisplayFormatting() {
		String text = textInputField.getText();
		int cursorPos = textInputField.getCursorPosition();
		int selectionPos = textInputField.getSelectionEnd();
		for (int i = 0; i < text.length() - 1; ++i) {
			if (text.charAt(i) == '\u00a7' && Character.digit(text.charAt(i + 1), 16) != -1) {
				text = text.substring(0, i) + text.substring(i + 2);
				cursorPos = i < cursorPos - 1 ? cursorPos - 2 : i < cursorPos ? cursorPos - 1 : cursorPos;
				selectionPos = i < selectionPos - 1 ? selectionPos - 2 : i < selectionPos ? selectionPos - 1 : selectionPos;
				i--;
			}
		}
		if (!textInputField.getText().equals(text)) {
			textInputField.setText(text);
			textInputField.setCursorPosition(cursorPos);
			textInputField.setSelectionPos(selectionPos);
		}
	}

	private void addDisplayFormatting() {
		String text = textInputField.getText();
		int cursorPos = textInputField.getCursorPosition();
		int selectionPos = textInputField.getSelectionEnd();
		for (int i = 0; i < text.length() - 1; ++i) {
			if (text.charAt(i) == '&' && Character.digit(text.charAt(i + 1), 16) != -1) {
				text = text.substring(0, i) + "\u00a7" + text.charAt(i + 1) + text.substring(i);
				if (i <= cursorPos )
					cursorPos += 2;
				if (i <= selectionPos)
					selectionPos += 2;
				i += 2;
			}
		}
		if (textInputField.getText().equals(text))
			return;
		if (text.length() > textInputField.getMaxStringLength())
			return;
		textInputField.setText(text);
		textInputField.setCursorPosition(cursorPos);
		textInputField.setSelectionPos(selectionPos);
	}
	
	private void addFormattingAfterCursor() {
		String text = textInputField.getText();
		int cursorPos = textInputField.getCursorPosition();
		int selectionPos = textInputField.getSelectionEnd();
		int formattingIndex = Math.max(cursorPos, selectionPos);
		String formatting = getClosestFormatting(text, formattingIndex);
		if (formatting.equals(""))
			return;
		text = text.substring(0, formattingIndex) + formatting + text.substring(formattingIndex);
		if (selectionPos > cursorPos)
			selectionPos += formatting.length();
		if (text.length() > textInputField.getMaxStringLength())
			return;
		textInputField.setText(text);
		textInputField.setCursorPosition(cursorPos);
		textInputField.setSelectionPos(selectionPos);
	}

	private void addFormattingAfterLineScrollOffset() {
		String text = textInputField.getText();
		int lineScrollOffset = getLineScrollOffset();
		int cursorPos = textInputField.getCursorPosition();
		int selectionPos = textInputField.getSelectionEnd();
		if (cursorPos == lineScrollOffset || lineScrollOffset == 0 || text.charAt(lineScrollOffset) == '\u00a7')
			return;
		String formatting = getClosestFormatting(text, lineScrollOffset);
		if (formatting.equals(""))
			return;
		String newText = text.substring(0, lineScrollOffset) + formatting + text.substring(lineScrollOffset);
		if (newText.length() > textInputField.getMaxStringLength())
			return;
		textInputField.setText(newText);
		textInputField.setCursorPosition(cursorPos + formatting.length());
		textInputField.setSelectionPos(selectionPos + formatting.length());
		if (lineScrollOffset == getLineScrollOffset())
			return;
		++lineScrollOffset;
		formatting = getClosestFormatting(text, lineScrollOffset);
		newText = text.substring(0, lineScrollOffset) + formatting + text.substring(lineScrollOffset);
		if (newText.length() > textInputField.getMaxStringLength())
			return;
		textInputField.setText(newText);
		textInputField.setCursorPosition(cursorPos + formatting.length());
		textInputField.setSelectionPos(selectionPos + formatting.length());
	}
	
	private int getLineScrollOffset() {
		try {
			Field lineScrollOffset = textInputField.getClass().getDeclaredField(
				ForgeWurst.getForgeWurst().isObfuscated() ? "field_146225_q" : "lineScrollOffset");
			lineScrollOffset.setAccessible(true);
			return lineScrollOffset.getInt(textInputField);
		} catch(ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getClosestFormatting(String formattedText, int cursorPosition) {
		for (int i = cursorPosition - 2; i >= 0; --i)
			if (formattedText.charAt(i) == '\u00a7' && Character.digit(formattedText.charAt(i + 1), 16) != -1)
				return formattedText.substring(i, i + 2);
		return "";
	}
	
	@Override
	public void updateScreen() {
		textInputField.updateCursorCounter();
		clearButton.enabled = !textInputField.getText().equals("");
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		String formattingHelp = "\u00a7aa\u00a7bb\u00a7cc\u00a7dd\u00a7ee\u00a7ff\u00a700\u00a711\u00a722\u00a733\u00a744\u00a755\u00a766\u00a777\u00a788\u00a799\u00a7r";
		String rank = ForgeWurst.getForgeWurst().getFeatureController().getPlayerRank();
		if (SkyblockUtils.canUseColoredChat(rank))
			drawCenteredString(WMinecraft.getFontRenderer(), textInputSetting.getName() + " (formatting help: " + formattingHelp + ")", width / 2, height - 112, 0xffffff);
		else
			drawCenteredString(WMinecraft.getFontRenderer(), textInputSetting.getName(), width / 2, height - 112, 0xffffff);
		textInputField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (textInputField.getText().isEmpty() && !textInputField.isFocused())
			drawString(WMinecraft.getFontRenderer(), "_", 104, height - 95, 0x808080);
	}
	
}