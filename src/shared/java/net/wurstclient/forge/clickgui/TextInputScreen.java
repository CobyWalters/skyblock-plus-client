package net.wurstclient.forge.clickgui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.BlockListSetting;
import net.wurstclient.forge.settings.TextInputSetting;
import scala.Char;

public final class TextInputScreen extends GuiScreen {
	
	private final GuiScreen prevScreen;
	private final TextInputSetting textInputSetting;
	
	private GuiTextField textInputField;
	private GuiButton setButton;
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
	
	/*@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int mouseX = Mouse.getEventX() * width / mc.displayWidth;
		int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
	}*/
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		textInputField.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		
		textInputField.textboxKeyTyped(typedChar, keyCode);
		
		if (keyCode == Keyboard.KEY_RETURN)
			actionPerformed(setButton);
		else if (keyCode == Keyboard.KEY_DELETE)
			actionPerformed(clearButton);
		else if (keyCode == Keyboard.KEY_ESCAPE)
			actionPerformed(doneButton);
		else {
			String formattedText = textInputField.getText();
			if (formattedText.length() > 0 && formattedText.charAt(formattedText.length() - 1) == '\u00a7')
				formattedText = formattedText.substring(0, formattedText.length() - 1) + '&';
			for (int i = 0; i < formattedText.length() - 1; ++i) {
				if (formattedText.charAt(i) == '&' && Character.digit(formattedText.charAt(i + 1), 16) != -1)
					formattedText = formattedText.substring(0, i) + '\u00a7' + formattedText.substring(i + 1);
				else if (formattedText.charAt(i) == '\u00a7' && Character.digit(formattedText.charAt(i + 1), 16) == -1)
					formattedText = formattedText.substring(0, i) + '&' + formattedText.substring(i + 1);
			}
			if (formattedText.length() <= textInputField.getMaxStringLength())
				textInputField.setText(formattedText);
		}
	}
	
	@Override
	public void updateScreen() {
		textInputField.updateCursorCounter();
		clearButton.enabled = !textInputField.getText().equals("");
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(WMinecraft.getFontRenderer(), textInputSetting.getName(), width / 2, 12, 0xffffff);
		textInputField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (textInputField.getText().isEmpty() && !textInputField.isFocused())
			drawString(WMinecraft.getFontRenderer(), "_", 104, height - 95, 0x808080);
	}
	
}