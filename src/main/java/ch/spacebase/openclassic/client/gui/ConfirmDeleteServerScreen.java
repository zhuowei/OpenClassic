package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;

import com.mojang.minecraft.SessionData;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class ConfirmDeleteServerScreen extends GuiScreen {

	private GuiScreen parent;
	private String name;

	public ConfirmDeleteServerScreen(GuiScreen parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 102, this.getHeight() / 6 + 132, 100, 20, this, "Yes"));
		this.attachWidget(new Button(0, this.getWidth() / 2 + 2, this.getHeight() / 6 + 132, 100, 20, this, "No"));
	}

	public void onButtonClick(Button button) {
		if (button.getId() == 0) {
			SessionData.favorites.remove(name);
			SessionData.saveFavorites();
		}
		
		OpenClassic.getClient().setCurrentScreen(this.parent);
	}

	public void render() {
		RenderHelper.getHelper().drawDirtBG();
		
		RenderHelper.getHelper().renderText("Are you sure you want to delete server \"" + this.name + "\"?", this.getWidth() / 2, (this.getHeight() / 2) - 32);
		super.render();
	}
}
