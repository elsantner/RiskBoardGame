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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.model.listener.OnCardsChangedListener;

public class CardStage extends Stage implements OnCardsChangedListener {

    private static final String TAG = "CardStage";
    private Logger log;
    private boolean updated;

    private Table cardContainer;
    private Table outer;
    private ArrayList<String> cardNames;
    private ScrollPane scrollPane;
    private Skin skin;
    private Label nameLabel;


    public CardStage(Viewport viewport) {
        super(viewport);
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        nameLabel = new Label("", skin);
        this.updated = false;
        this.log = Logger.getLogger(TAG);

        /*
        This is a simple scrollable list of cards.
        cardContainer: inner table of image actors
        the container is surrounded by a ScrollPane
        ScrollPane is inside outer table, that is added as actor
         */

        this.cardNames = new ArrayList<>();
        this.cardContainer = new Table();
        this.cardContainer.setDebug(true);

        scrollPane = new ScrollPane(cardContainer);
        outer = new Table();
        outer.setFillParent(true);
        outer.add(nameLabel).expand();
        outer.row();

        outer.add(scrollPane).fill().bottom().pad(0, 0, 20f, 0);

        this.addActor(outer);
    }

    public void updateActor() {

        outer.remove();

        this.cardContainer = new Table();
        this.cardContainer.setDebug(true);

        for (String s : cardNames.toArray(new String[0])
        ) {
            addCard(s);
        }


        scrollPane = new ScrollPane(cardContainer);
        outer = new Table();
        outer.setFillParent(true);
        outer.add(nameLabel).expand();
        outer.row();

        outer.add(scrollPane).fill().bottom().pad(0, 0, 20f, 0);

        this.addActor(outer);


        this.updated = false;
    }

    public void addCard(String cardName) {

        Texture texture;
        if (cardName.equals("card_wild1") || cardName.equals("card_wild2")) {
            texture = new Texture(Gdx.files.internal("cards/card_wild.png"));
        } else {
            log.info(cardName);
            texture = new Texture(Gdx.files.internal("cards/" + cardName + ".png"));
        }
        Image im = new Image(texture);
        this.cardContainer.add(im).pad(0, 8f, 0, 8f);
    }

    @Override
    public void singleNewCard(String cardName) {
        addCard(cardName);
        this.cardNames.add(cardName);
        Collections.sort(cardNames);
        log.log(Level.INFO, "A Card has been added to the list: {0}" , cardName);
        this.cardContainer = new Table();
        this.updated = true;

    }

    @Override
    public void refreshCards(String[] cardNames) {
        // not yet implemented
    }

    public Table getCardContainer() {
        return cardContainer;
    }

    public List<String> getCardNames() {
        return cardNames;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
}
