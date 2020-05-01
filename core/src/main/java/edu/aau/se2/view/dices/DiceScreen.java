package edu.aau.se2.view.dices;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DiceScreen extends ScreenAdapter {

    private static final int ATTACKER_DICE_COUNT = 3;
    private static final int DEFENDER_DICE_COUNT = 2;

    private Texture background;
    private TextureRegion[] diceTextures = new TextureRegion[6];
    private SpriteBatch batch;


    private Stage stage;
    private List<Integer> results;

    private boolean attacker;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.begin();
        batch.draw(background, 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.end();

        stage.act();
        stage.draw();
    }

    public DiceScreen(boolean attacker) {

        this.attacker = attacker;

        results = new ArrayList<>();

        rollDice();
    }

    private void rollDice() {
        for (int i = 0; i < (attacker ? ATTACKER_DICE_COUNT : DEFENDER_DICE_COUNT); i++) {
            results.add(ThreadLocalRandom.current().nextInt(1, 6 + 1));
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        background = new Texture(Gdx.files.internal("lobbylist/lobbyScreen.png"));
        Texture dicesTexture = new Texture(
                Gdx.files.internal("dices/dices_" + (attacker ? "attacker" : "defender") + ".JPG"));
        for (int i = 0; i < 6; i++) {
            diceTextures[i] = new TextureRegion(dicesTexture, i*128, 0, 128, 128);
        }
        batch = new SpriteBatch();

        this.stage = new Stage(new StretchViewport(1920,1080));
        stage.stageToScreenCoordinates(new Vector2(0,0));
        Gdx.input.setInputProcessor(this.stage);

        Table outerTable = new Table();
        outerTable.setFillParent(true);
        outerTable.pad(120f);

        Gdx.app.log("Test", "test");

        for(Integer r : results) {
            Image dice = new Image(diceTextures[r-1]);
            dice.addAction(new DiceAnimationAction(12, diceTextures, r));
            outerTable.add(dice).pad(20);
        }

        this.stage.addActor(outerTable);
    }

    @Override
    public void dispose() {
        background.dispose();
    }
}
