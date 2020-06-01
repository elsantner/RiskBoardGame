package edu.aau.se2.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;

import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;
import edu.aau.se2.view.game.Territory;

public class ChangeNameStage extends AbstractStage {
    private Label testLabel;

    public ChangeNameStage(AbstractScreen screen, Viewport vp){
        super(vp, screen);
        setupChangeNameUI();
    }

    private void setupChangeNameUI() {
        Viewport vp = getViewport();
        Table table = new Table();
        table.top();
        table.setFillParent(true);

        Label testLabel = new Label("TEST", new Label.LabelStyle(generateFont(), Color.WHITE));
        table.add(testLabel).width(vp.getScreenWidth() / 3f).padTop(vp.getWorldHeight() * 0.01f).padLeft(vp.getWorldWidth() * 0.01f).left();
        this.addActor(table);

    }

    private BitmapFont generateFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/CenturyGothic.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 32;
        BitmapFont font = generator.generateFont(parameter);
        font.getData().setScale((getViewport().getWorldWidth() * 1.5f) / Territory.REFERENCE_WIDTH);
        generator.dispose();
        return font;
    }
}
