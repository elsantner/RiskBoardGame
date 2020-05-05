package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;

/**
 * @author Elias
 */
public class GameScreen implements Screen, OnTerritoryUpdateListener, OnNextTurnListener {
    private BoardStage boardStage;
    private Stage tmpHUDStage;
    private CardStage cardStage;
    private Database db;
    private InputMultiplexer inputMultiplexer;
    private ConfirmDialog dialog;
    private Table table;


    public GameScreen() {
        this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public GameScreen(int width, int height) {
        boardStage = new BoardStage(new FitViewport(width, height));
        tmpHUDStage = new Stage(new FitViewport(width, height));
        cardStage = new CardStage(new StretchViewport(width, height));

        db = Database.getInstance();
        boardStage.setListener(db);
        db.setTerritoryUpdateListener(this);
        db.setNextTurnListener(this);
        db.setCardsChangedListener(cardStage);
        // trigger player turn update because listener might not have been registered when
        // server message was received
        if (db.getCurrentPlayerToAct() != null) {   // only if initial army placing message was received already
            isPlayersTurnNow(db.getCurrentPlayerToAct().getUid(), db.isThisPlayersTurn());
        }

        this.dialog = new ConfirmDialog("Exchange cards",
                "Moechten Sie 3 Karten eintauschen?", "Ja", "Nein",
                new ConfirmDialog.OnClickListener() {
                    @Override
                    public void clicked(boolean result) {
                        inputMultiplexer.removeProcessor(tmpHUDStage);
                        db.exchangeCards();
                    }
                });
        dialog.setPosition(width/2f, height/2f);
    }

    public void setListener(OnBoardInteractionListener l) {
        boardStage.setListener(l);
    }

    public IGameBoard getGameBoard() {
        return this.boardStage;
    }

    @Override
    public void show() {
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new CustomGestureDetector(boardStage));
        inputMultiplexer.addProcessor(cardStage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        // render "unused" area in red
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        boardStage.draw();
        tmpHUDStage.draw();

        // todo remove (add button in Hud to show cards)
       /* if (cardStage.isUpdated()) {
            cardStage.updateActor();
        }
        cardStage.act();
        cardStage.draw();*/


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
        cardStage.dispose();
    }

    @Override
    public void territoryUpdated(int territoryID, int armyCount, int colorID) {
        boardStage.setArmyCount(territoryID, armyCount);
        boardStage.setArmyColor(territoryID, colorID);

        if (db.isInitialArmyPlacementFinished() && db.isThisPlayersTurn() && db.getCurrentArmyReserve() == 0) {
            //showFinishTurnDialog();
            db.finishTurn();
        }
    }

    private void showFinishTurnDialog() {
        inputMultiplexer.addProcessor(tmpHUDStage);
        ConfirmDialog dialog = new ConfirmDialog("Zug beenden",
                "Möchten Sie Ihren Zug beenden?", "Ja", "Nein",
                result -> {
                    inputMultiplexer.removeProcessor(tmpHUDStage);
                    db.finishTurn();
                });
        dialog.show(tmpHUDStage);
        dialog.setMovable(false);
    }

    private void showAskForCardExchange() {
        inputMultiplexer.addProcessor(tmpHUDStage);
        dialog.show(tmpHUDStage).setPosition(100f, 100f);
        dialog.setMovable(true);
    }

    @Override
    public void isPlayersTurnNow(int playerID, boolean isThisPlayer) {
        boardStage.setArmiesPlacable(isThisPlayer);
        if (db.getThisPlayer() != null && db.getThisPlayer().isAskForCardExchange()) {
            showAskForCardExchange();
        }
    }
}
