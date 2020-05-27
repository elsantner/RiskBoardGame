package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import edu.aau.se2.view.asset.AssetName;

public class AttackDisplay extends Group {

    private AssetManager assetManager;
    private BitmapFont font;
    private Table tableContainer;
    private Texture attackArrow;
    private Drawable background;
    private Label labelAttacker;
    private Label labelDefender;
    private Label labelTerritoryAttacker;
    private Label labelTerritoryDefender;
    private Label labelArmyCount;
    private Label labelArmiesLostAttacker;
    private Label labelArmiesLostDefender;

    public AttackDisplay(AssetManager assetManager) {
        this.assetManager = assetManager;
        getAssets();
        setupDisplay();
    }

    @Override
    public void setVisible(boolean visible) {
        if (tableContainer != null) {
            tableContainer.setVisible(visible);
        }
    }

    private void getAssets() {
        font = assetManager.get(AssetName.FONT_1);
        attackArrow = assetManager.get(AssetName.ATTACK_ARROW);
        background = new TextureRegionDrawable(new TextureRegion(assetManager.get(AssetName.BG_ATTACK_DISPLAY, Texture.class)));
    }

    private void setupDisplay() {

        labelAttacker = new Label("",
                new Label.LabelStyle(font, new Color(1, 1, 1, 1)));
        labelDefender = new Label("",
                new Label.LabelStyle(font, new Color(1, 1, 1, 1)));
        labelTerritoryAttacker = new Label("",
                new Label.LabelStyle(font, new Color(1, 1, 1, 1)));
        labelTerritoryDefender = new Label("",
                new Label.LabelStyle(font, new Color(1, 1, 1, 1)));
        labelArmyCount = new Label("",
                new Label.LabelStyle(font, new Color(0.6f, 0, 0, 1)));
        labelArmiesLostAttacker = new Label("",
                new Label.LabelStyle(font, new Color(0.6f, 0, 0, 1)));
        labelArmiesLostDefender = new Label("",
                new Label.LabelStyle(font, new Color(0.6f, 0, 0, 1)));

        labelAttacker.setOrigin(Align.center);
        labelDefender.setOrigin(Align.center);
        labelTerritoryAttacker.setOrigin(Align.center);
        labelTerritoryDefender.setOrigin(Align.center);
        Image imgArrow = new Image(attackArrow);

        // do not use getWidth() --> returns 0 for whatever reason
        int width = Gdx.graphics.getWidth();

        tableContainer = new Table();
        tableContainer.setFillParent(true);
        tableContainer.setOrigin(Align.center);

        Table tableContent = new Table();
        tableContent.pad(width / 30f);

        tableContent.add(labelAttacker).minHeight(font.getLineHeight());
        tableContent.add(labelArmyCount).minHeight(font.getLineHeight());
        tableContent.add(labelDefender).minHeight(font.getLineHeight());
        tableContent.row();
        tableContent.add(labelTerritoryAttacker).minHeight(font.getLineHeight());
        tableContent.add(imgArrow).minWidth(imgArrow.getMinWidth()).padLeft(width / 30f).padRight(width / 30f);
        tableContent.add(labelTerritoryDefender).minHeight(font.getLineHeight());
        tableContent.row();
        tableContent.add(labelArmiesLostAttacker).minHeight(font.getLineHeight());
        tableContent.add().minHeight(font.getLineHeight()).expand().fill();
        tableContent.add(labelArmiesLostDefender).minHeight(font.getLineHeight());
        tableContent.row();


        Container<Table> container = new Container<>(tableContent);
        container.setBackground(background);

        tableContainer.add(container).width(Gdx.graphics.getWidth() * 0.50f);
        tableContainer.row();
        this.addActor(tableContainer);
    }

    public void updateData(String attacker, String defender, String fromTerritory, String toTerritory, int armyCount, int armiesLostAttacker, int armiesLostDefender) {
        labelAttacker.setText(attacker);
        labelDefender.setText(defender);
        labelTerritoryAttacker.setText(fromTerritory);
        labelTerritoryDefender.setText(toTerritory);
        labelArmyCount.setText(Integer.toString(armyCount));
        labelArmiesLostAttacker.setText(Integer.toString(-armiesLostAttacker));
        labelArmiesLostDefender.setText(Integer.toString(-armiesLostDefender));
        labelArmiesLostAttacker.setVisible(armiesLostAttacker != -1 && armiesLostDefender != -1);
        labelArmiesLostDefender.setVisible(armiesLostAttacker != -1 && armiesLostDefender != -1);
    }
}
