package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.net.PacketType;
import org.lwjgl.input.Keyboard;

public final class ChatInputScreen extends GuiScreen {

	public final void onOpen() {
		Keyboard.enableRepeatEvents(true);
		
		this.clearWidgets();
		this.attachWidget(new TextBox(0, 2, this.getHeight() - 14, this.getWidth() - 4, 12, this, true, true, true));
	}

	public final void onClose() {
		Keyboard.enableRepeatEvents(false);
	}

	public final void onKeyPress(char c, int key) {
		if (key == Keyboard.KEY_RETURN) {
			String message = this.getWidget(0, TextBox.class).getText().trim();
			if (message.length() > 0) {
				if(GeneralUtils.getMinecraft().netManager != null && GeneralUtils.getMinecraft().netManager.isConnected()) {
					GeneralUtils.getMinecraft().netManager.netHandler.send(PacketType.CHAT_MESSAGE, new Object[] { (byte) -1, message });
				} else if(message.startsWith("/")) {
					OpenClassic.getClient().processCommand(OpenClassic.getClient().getPlayer(), message.substring(1));
				}
			}

			GeneralUtils.getMinecraft().setCurrentScreen(null);
		}
		
		super.onKeyPress(c, key);
	}

	public final void onMouseClick(int x, int y, int button) {
		TextBox text = this.getWidget(0, TextBox.class);
		if (button == 0 && GeneralUtils.getMinecraft().hud.clickedPlayer != null) {
			if (text.getText().length() > 0 && !text.getText().endsWith(" ")) {
				text.setText(text.getText() + " ");
			}

			text.setText(text.getText() + GeneralUtils.getMinecraft().hud.clickedPlayer);
			if (text.getText().length() > 62 - GeneralUtils.getMinecraft().data.username.length()) {
				text.setText(text.getText().substring(0, 62 - GeneralUtils.getMinecraft().data.username.length()));
			}
		}

		super.onMouseClick(x, y, button);
	}
}
