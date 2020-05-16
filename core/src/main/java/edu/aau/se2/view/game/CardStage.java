package edu.aau.se2.view.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.model.listener.OnCardsChangedListener;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;

public class CardStage extends AbstractStage implements OnCardsChangedListener {

    private static final String TAG = "CardStage";
    private Logger log;
    private boolean updated;

    private Table cardContainer;
    private Table outer;
    private ArrayList<String> cardNames;
    private ScrollPane scrollPane;
    private AssetManager assetManager;
    private static final String WILD1 = "card_wild1";
    private static final String WILD2 = "card_wild2";
    private static final String WILD_PATH = "cards/card_wild.png";
    private Viewport viewport = getViewport();

    /**
     * This is a simple scrollable list of cards.
     * cardContainer: inner table of image actors
     * the container is surrounded by a ScrollPane
     * ScrollPane is inside outer table, that is added as actor
     */
    public CardStage(AbstractScreen screen, Viewport viewport) {
        super(viewport, screen);
        this.updated = false;
        this.log = Logger.getLogger(TAG);
        this.assetManager = this.getScreen().getGame().getAssetManager();

        this.cardNames = new ArrayList<>();
        this.cardContainer = new Table();
        scrollPane = new ScrollPane(cardContainer);
        outer = new Table().bottom();
        outer.setFillParent(true);

        outer.pad(0, viewport.getWorldWidth() * 0.092f, 0, viewport.getWorldWidth() * 0.092f);
        outer.add(scrollPane).bottom().pad(0, 0, viewport.getWorldHeight() * 0.012f, 0);
        this.addActor(outer);
    }

    public void updateActor() {
        outer.removeActor(scrollPane);
        this.cardContainer = new Table();

        for (String s : cardNames.toArray(new String[0])
        ) {
            addCard(s);
        }

        scrollPane = new ScrollPane(cardContainer);
        outer.add(scrollPane).bottom().pad(0, 0, viewport.getWorldHeight() * 0.012f, 0);

        this.updated = false;
    }

    public void addCard(String cardName) {
        Texture texture;
        String filename = "cards/" + cardName + ".png";

        if (cardName.equals(WILD1) || cardName.equals(WILD2)) {
            if (assetManager.isLoaded(WILD_PATH)) {
                texture = assetManager.get(WILD_PATH);
            } else {
                texture = assetManager.finishLoadingAsset(WILD_PATH);
            }
        } else {
            if (assetManager.isLoaded(filename)) {
                texture = assetManager.get(filename);
            } else {
                texture = assetManager.finishLoadingAsset(filename);
            }
        }
        Image im = new Image(texture);
        this.cardContainer.add(im).pad(0, viewport.getWorldWidth() * 0.002f, 0, viewport.getWorldWidth() * 0.002f)
                .height(viewport.getWorldHeight() / 2.2f).width((viewport.getWorldHeight() / 2.2f) / 1.568f);
    }

    @Override
    public void singleNewCard(String cardName) {
        this.cardNames.add(cardName);
        if (!assetManager.isLoaded(WILD_PATH) && (cardName.equals(WILD1) || cardName.equals(WILD2))) {
            assetManager.load((WILD_PATH), Texture.class);
        } else if (!(cardName.equals(WILD1) || cardName.equals(WILD2))) {
            assetManager.load(("cards/" + cardName + ".png"), Texture.class);
        }
        Collections.sort(cardNames);
        log.log(Level.INFO, "A Card has been added to the list: " + cardName);
        this.updated = true;
    }

    @Override
    public void refreshCards(String[] cardNames) {
        this.cardNames = new ArrayList<>(Arrays.asList(cardNames));
        this.updated = true;
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
