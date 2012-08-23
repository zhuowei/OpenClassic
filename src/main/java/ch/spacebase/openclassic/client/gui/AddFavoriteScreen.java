package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.render.RenderHelper;

import com.mojang.minecraft.SessionData;

import org.lwjgl.input.Keyboard;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class AddFavoriteScreen extends GuiScreen {

	private GuiScreen parent;
	private TextBox name;
	private TextBox url;
	
	private boolean error = false;

	public AddFavoriteScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		Keyboard.enableRepeatEvents(true);
		
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, OpenClassic.getGame().getTranslator().translate("gui.add-favorite.add")));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.cancel")));
		
		this.name = new TextBox(2, this.getWidth() / 2 - 100, this.getHeight() / 2 - 50, this);
		this.name.setFocus(true);
		this.url = new TextBox(3, this.getWidth() / 2 - 100, this.getHeight() / 2 - 10, this);
		this.attachWidget(this.name);
		this.attachWidget(this.url);
		
		this.getWidget(0, Button.class).setActive(false);
	}

	public void onClose() {
		Keyboard.enableRepeatEvents(false);
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 0 && !this.name.getText().equals("")) {
			if(this.url.getText().length() <= 0) {
				this.error = true;
			}
			
			SessionData.favorites.put(this.name.getText(), this.url.getText());
			SessionData.saveFavorites();
			
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
		
		if(button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}

	public void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(0, Button.class).setActive(this.name.getText().length() > 0);
	}

	public void render() {
		RenderHelper.getHelper().drawDirtBG();
		
		if(this.error) RenderHelper.getHelper().renderText(Color.RED + OpenClassic.getGame().getTranslator().translate("gui.add-favorite.enter-url"), this.getWidth() / 2, 40);
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.add-favorite.enter-name"), this.getWidth() / 2, this.getHeight() / 2 - 65);
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.add-favorite.enter-url"), this.getWidth() / 2, this.getHeight() / 2 - 25);
		super.render();
	}
}
