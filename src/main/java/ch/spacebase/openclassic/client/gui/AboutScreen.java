package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;

import com.mojang.minecraft.Minecraft;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class AboutScreen extends GuiScreen {

	private GuiScreen parent;

	public AboutScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 6 + 120 + 12, this, true, "Back to Menu"));
	}

	public void onButtonClick(Button button) {
		if (button.getId() == 0) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDirtBG();
		
		RenderHelper.getHelper().renderText("Minecraft Classic", this.getWidth() / 2, (this.getHeight() / 2) - 32);
		RenderHelper.getHelper().renderText("Version " + Minecraft.VERSION, this.getWidth() / 2, (this.getHeight() / 2) - 21);
		RenderHelper.getHelper().renderText("Modded By Steveice10 (Steveice10@gmail.com)", this.getWidth() / 2, (this.getHeight() / 2) - 10);
		super.render();
	}
}
