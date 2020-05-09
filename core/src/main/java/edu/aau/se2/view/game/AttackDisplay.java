package edu.aau.se2.view.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import edu.aau.se2.model.Database;
import edu.aau.se2.view.asset.AssetName;

public class AttackDisplay extends Group {

    private AssetManager assetManager;
    private BitmapFont font;
    private Table table;

    public AttackDisplay(AssetManager assetManager, String attackerName, String defenderName,
                         String attackerTerritoryName, String defenderTerritoryName, int count) {
        this.assetManager = assetManager;
        font = assetManager.get(AssetName.FONT_1);
        setupTable(attackerName, defenderName, attackerTerritoryName, defenderTerritoryName, count);
    }

    private void setupTable(String attackerName, String defenderName,
                            String attackerTerritoryName, String defenderTerritoryName, int count) {
        Label labelPlayers = new Label(attackerName + " greift " + defenderName + " an!",
                new Label.LabelStyle(font, new Color(0.6f, 0, 0, 1)));
        Label labelTerritories = new Label(attackerTerritoryName + " --> " + defenderTerritoryName,
                new Label.LabelStyle(font, new Color(0.6f, 0, 0, 1)));

        labelPlayers.setOrigin(Align.center);
        labelTerritories.setOrigin(Align.center);

        table = new Table();
        table.setFillParent(true);
        table.add(labelPlayers).minHeight(font.getLineHeight());
        table.row();
        table.add(labelTerritories).minHeight(font.getLineHeight());
        table.row();

        this.addActor(table);
    }

    @Override
    public void setOrigin (int alignment) {
        super.setOrigin(alignment);
        table.setOrigin(alignment);
    }
}
