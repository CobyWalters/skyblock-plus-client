package net.wurstclient.forge.clickgui;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.TextInputSetting;
import net.wurstclient.forge.utils.ChatUtils;

public final class TextInputScreen extends GuiScreen {
	
	private final GuiScreen prevScreen;
	private final TextInputSetting textInputSetting;
	
	private GuiTextField fakeTextInputField;
	private GuiTextField displayTextInputField;
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
		fakeTextInputField = new GuiTextField(0, WMinecraft.getFontRenderer(), 0, 0, 0, 0);
		fakeTextInputField.setMaxStringLength(256);
		fakeTextInputField.setText(textInputSetting.getText());
		displayTextInputField = new GuiTextField(1, WMinecraft.getFontRenderer(), 100, height - 100, width - 200, 18);
		displayTextInputField.setMaxStringLength(256);
		displayTextInputField.setText(textInputSetting.getText());
		buttonList.add(clearButton = new GuiButton(0, width - 95, height - 101, 45, 20, "Clear"));
		buttonList.add(doneButton = new GuiButton(1, width / 2 - 100, height - 73, "Done"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled)
			return;
		
		switch (button.id) {
			case 0:
				fakeTextInputField.setText("");
				displayTextInputField.setText("");
				break;
			case 1:
				textInputSetting.setText(fakeTextInputField.getText());
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
		displayTextInputField.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		
		fakeTextInputField.setFocused(true);
		fakeTextInputField.textboxKeyTyped(typedChar, keyCode);
		fakeTextInputField.setFocused(false);
		displayTextInputField.setFocused(true);
		displayTextInputField.textboxKeyTyped(typedChar, keyCode);
		
		if (keyCode == Keyboard.KEY_RETURN)
			actionPerformed(doneButton);
		else if (keyCode == Keyboard.KEY_DELETE)
			actionPerformed(clearButton);
		else if (keyCode == Keyboard.KEY_ESCAPE)
			mc.displayGuiScreen(prevScreen);
		else {
			// find formatting errors
			String formattedText = fakeTextInputField.getText();
			
			if (formattedText.length() > 0 && formattedText.charAt(formattedText.length() - 1) == '\u00a7')
				formattedText = formattedText.substring(0, formattedText.length() - 1) + '&';
			for (int i = 0; i < formattedText.length() - 1; ++i) {
				if (formattedText.charAt(i) == '&' && Character.digit(formattedText.charAt(i + 1), 16) != -1)
					formattedText = formattedText.substring(0, i) + '\u00a7' + formattedText.substring(i + 1);
				else if (formattedText.charAt(i) == '\u00a7' && Character.digit(formattedText.charAt(i + 1), 16) == -1)
					formattedText = formattedText.substring(0, i) + '&' + formattedText.substring(i + 1);
			}
			
			ChatUtils.debugMessage("1: " + fakeTextInputField.getSelectedText());
			ChatUtils.debugMessage("2: " + displayTextInputField.getSelectedText());
			
			// correct the formatting errors
			int pos = fakeTextInputField.getCursorPosition();
			int pos2 = fakeTextInputField.getSelectionEnd();
			if (!fakeTextInputField.getText().equals(formattedText)) {
				fakeTextInputField.setText(formattedText);
				fakeTextInputField.setCursorPosition(pos);
				fakeTextInputField.setSelectionPos(pos2);
			}
			
			// apply the formatting that follows the cursor
			String closestFormatting = getClosestFormatting(formattedText, pos);
			formattedText = formattedText.substring(0, pos) + closestFormatting + formattedText.substring(pos);
			displayTextInputField.setText(formattedText);
			displayTextInputField.setCursorPosition(pos);
			displayTextInputField.setSelectionPos(pos2);
			
			ChatUtils.debugMessage("3: " + fakeTextInputField.getSelectedText());
			ChatUtils.debugMessage("4: " + displayTextInputField.getSelectedText());
		}

	}
	
	public String getClosestFormatting(String formattedText, int cursorPosition) {
		for (int i = cursorPosition - 1; i >= 0; --i)
			if (formattedText.charAt(i) == '\u00a7' && Character.digit(formattedText.charAt(i + 1), 16) != -1)
				return formattedText.substring(i, i + 2);
		return "";
	}
	
	@Override
	public void updateScreen() {
		displayTextInputField.updateCursorCounter();
		clearButton.enabled = !displayTextInputField.getText().equals("");
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(WMinecraft.getFontRenderer(), textInputSetting.getName(), width / 2, 12, 0xffffff);
		displayTextInputField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (displayTextInputField.getText().isEmpty() && !displayTextInputField.isFocused())
			drawString(WMinecraft.getFontRenderer(), "_", 104, height - 95, 0x808080);
	}
	
}