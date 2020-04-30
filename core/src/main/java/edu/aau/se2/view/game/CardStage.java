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

import java.util.ArrayList;
import java.util.List;

public class CardStage extends Stage {

    private Table cardContainer;
    private ArrayList<String> cardNames;


    public CardStage(Viewport viewport) {
        super(viewport);
        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        Label nameLabel = new Label("", skin);

        /*
        This is a simple scrollable list of cards.
        cardContainer: inner table of image actors
        the container is surrounded by a ScrollPane
        ScrollPane is inside outer table, that is added as actor
         */

        this.cardNames = new ArrayList<>();
        this.cardContainer = new Table();

        this.addCard("card_alaska");
        this.addCard("card_siberia");
        this.addCard("card_brazil");
        this.addCard("card_ural");

        this.cardContainer.setDebug(true);

        ScrollPane scrollPane = new ScrollPane(cardContainer);
        Table outer = new Table();
        outer.setFillParent(true);
        outer.add(nameLabel).expand();
        outer.row();

        outer.add(scrollPane).fill().bottom().pad(0, 0, 20f, 0);

        this.addActor(outer);

    }

    // todo wildcard!!
    public void addCard(String name) {

        this.cardNames.add(name);
        Texture texture = new Texture(Gdx.files.internal("cards/" + name + ".png"));
        Image im = new Image(texture);
        this.cardContainer.add(im).pad(0, 8f, 0, 8f);
    }

    public Table getCardContainer() {
        return cardContainer;
    }

    public List<String> getCardNames() {
        return cardNames;
    }
}
