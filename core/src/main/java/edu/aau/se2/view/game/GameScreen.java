package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.utils.viewport.FitViewport;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;

/**
 * @author Elias
 */
public class GameScreen implements Screen, OnTerritoryUpdateListener, OnNextTurnListener {
    private BoardStage boardStage;
    private Database db;

    public GameScreen() {
        this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
    public GameScreen(int width, int height) {
        boardStage = new BoardStage(new FitViewport(width, height));
        db = Database.getInstance();
        boardStage.setListener(db);
        db.setTerritoryUpdateListener(this);
        db.setNextTurnListener(this);
        // trigger player turn update because listener might not have been registered when
        // server message was received
        isPlayersTurnNow(db.getCurrentPlayerToAct().getUid(), db.isThisPlayersTurn());
    }

    public void setListener(OnBoardInteractionListener l) {
        boardStage.setListener(l);
    }

    public IGameBoard getGameBoard() {
        return this.boardStage;
    }

    @Override
    public void show() {
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(boardStage));
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        // render "unused" area in red
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boardStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // currently unused but needed because of interface implementation
    }

    @Override
    public void pause() {
        // currently unused but needed because of interface implementation
    }

    @Override
    public void resume() {
        // currently unused but needed because of interface implementation
    }

    @Override
    public void hide() {
        // currently unused but needed because of interface implementation
    }

    @Override
    public void dispose() {
        boardStage.dispose();
    }

    @Override
    public void territoryUpdated(int territoryID, int armyCount, int colorID) {
        boardStage.setArmyCount(territoryID, armyCount);
        boardStage.setArmyColor(territoryID, colorID);
    }

    @Override
    public void isPlayersTurnNow(int playerID, boolean isThisPlayer) {
        boardStage.setArmiesPlacable(isThisPlayer);
    }
}
