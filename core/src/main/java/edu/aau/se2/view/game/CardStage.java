package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CardStage extends Stage {


    public CardStage(Viewport viewport) {
        super(viewport);
        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        Label nameLabel = new Label("", skin);

        /*
        This is a simple scrollable list of cards.
        table: inner table of image actors
        this table is surrounded by a ScrollPane
        ScrollPane is inside outer table, that is added as actor
         */

        Table table = new Table();
        Texture texture = new Texture(Gdx.files.internal("cards/card_afghanistan.png"));
        Image im = new Image(texture);
        table.add(im).pad(0, 0, 0, 20f);
        texture = new Texture(Gdx.files.internal("cards/card_alaska.png"));
        im = new Image(texture);
        table.add(im).pad(0, 0, 0, 20f);


        texture = new Texture(Gdx.files.internal("cards/card_argentina.png"));
        im = new Image(texture);
        table.add(im).pad(0, 0, 0, 20f);
        texture = new Texture(Gdx.files.internal("cards/card_brazil.png"));
        im = new Image(texture);
        table.add(im).pad(0, 0, 0, 20f);
        texture = new Texture(Gdx.files.internal("cards/card_alaska.png"));
        im = new Image(texture);
        table.add(im).pad(0, 0, 0, 20f);
        texture = new Texture(Gdx.files.internal("cards/card_alaska.png"));
        im = new Image(texture);
        table.add(im).pad(0, 0, 0, 20f);

        table.row();
        table.setDebug(true);


        ScrollPane scrollPane = new ScrollPane(table);
        Table outer = new Table();
        outer.setFillParent(true);
        outer.add(nameLabel).expand();
        outer.row();

        outer.add(scrollPane).fill().bottom().pad(0, 0, 20f, 0);

        this.addActor(outer);

    }

    @Override
    public void draw() {
        super.draw();
    }
}
