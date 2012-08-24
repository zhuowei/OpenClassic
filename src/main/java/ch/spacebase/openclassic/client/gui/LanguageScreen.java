package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import java.util.ArrayList;
import java.util.List;

public class LanguageScreen extends GuiScreen {

	private GuiScreen parent;

	public LanguageScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 75, this.getHeight() / 6 + 156, 150, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")));
	
		List<String> languages = new ArrayList<String>();
		for(String language : OpenClassic.getGame().getTranslator().getLanguageNames()) {
			languages.add(language);
		}
		
		this.getWidget(0, ButtonList.class).setContents(languages);
	}

	public final void onButtonClick(Button button) {
		if (button.isActive()) {
			if (button.getId() == 1) {
				GeneralUtils.getMinecraft().setCurrentScreen(this.parent);
			}
		}
	}
	
	@Override
	public void onButtonListClick(ButtonList list, Button button) {
		if(button.isActive()) {
			OpenClassic.getGame().getConfig().setValue("options.language", button.getText());
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDirtBG();
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.language.select"), this.getWidth() / 2, 15, 16777215);
		RenderHelper.getHelper().renderText(String.format(OpenClassic.getGame().getTranslator().translate("gui.language.current"), OpenClassic.getGame().getConfig().getString("options.language")), this.getWidth() / 2, this.getHeight() / 2 + 48, 16777215);
		super.render();
	}
}
