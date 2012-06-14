package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ToggleButton;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.GameSettings;

public final class HacksScreen extends GuiScreen {

	private GuiScreen parent;
	private GameSettings settings;

	public HacksScreen(GuiScreen parent, GameSettings settings) {
		this.parent = parent;
		this.settings = settings;
	}

	public final void onOpen() {
		this.clearWidgets();
		for (int count = 0; count < this.settings.hacks; count++) {
			this.attachWidget(new ToggleButton(count, this.getWidth() / 2 - 155 + count % 2 * 160, this.getHeight() / 6 + 24 * (count >> 1), 155, 20, this, true, this.settings.getHack(count)));
		}
		
		this.attachWidget(new Button(100, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, this, true, "Done"));
	}

	public final void onButtonClick(Button button) {
		if (button.isActive()) {
			if (button.getId() < 100) {
				this.settings.toggleHack(button.getId());
				button.setText(this.settings.getHack(button.getId()));
			}

			if (button.getId() == 100) {
				OpenClassic.getClient().setCurrentScreen(this.parent);
			}
		}
	}

	public final void render() {
		if(GeneralUtils.getMinecraft().ingame) {
			RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 1610941696, -1607454624);
		} else {
			RenderHelper.getHelper().drawDirtBG();
		}
		
		RenderHelper.getHelper().renderText("Hacks", this.getWidth() / 2, 20);
		super.render();
	}
}
