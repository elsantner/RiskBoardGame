package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.List;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;
import edu.aau.se2.server.data.Player;

/**
 * @author Elias
 */
public class GameScreen implements Screen, OnTerritoryUpdateListener, OnNextTurnListener {
    private BoardStage boardStage;
    private Stage tmpHUDStage;
    private HudStage hudStage;
    private Database db;
    private InputMultiplexer inputMultiplexer;
    private List<Player> currentPlayers;
    private Player thisPlayer;

    public GameScreen() {
        this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public GameScreen(int width, int height) {
        boardStage = new BoardStage(new FitViewport(width, height));
        tmpHUDStage = new Stage(new FitViewport(width, height));
        db = Database.getInstance();
        boardStage.setListener(db);
        db.setTerritoryUpdateListener(this);
        db.setNextTurnListener(this);
        // trigger player turn update because listener might not have been registered when
        // server message was received
        hudStage = new HudStage(new FitViewport(width, height), db.getCurrentPlayers());
        if (db.getCurrentPlayerToAct() != null) {   // only if initial army placing message was received already
            isPlayersTurnNow(db.getCurrentPlayerToAct().getUid(), db.isThisPlayersTurn());
            setPlayersDataOnHud(db.getCurrentPlayers());
        }
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
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        // render "unused" area in red
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //boardStage.draw();
        //tmpHUDStage.draw();
        boardStage.getViewport().apply();
        boardStage.draw();
        hudStage.getViewport().apply();
        hudStage.update();
        hudStage.draw();
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
        hudStage.dispose();
    }

    @Override
    public void territoryUpdated(int territoryID, int armyCount, int colorID) {
        boardStage.setArmyCount(territoryID, armyCount);
        boardStage.setArmyColor(territoryID, colorID);
        if (db.isInitialArmyPlacementFinished() && db.isThisPlayersTurn() && db.getCurrentArmyReserve() == 0) {
            showFinishTurnDialog();
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

    @Override
    public void isPlayersTurnNow(int playerID, boolean isThisPlayer) {
        boardStage.setArmiesPlacable(isThisPlayer);
        hudStage.setArmiesPlacable(isThisPlayer);
    }

    public void setPlayersDataOnHud(List<Player> currentPlayers) {
        //hudStage.setCurrentPlayersColorOnHud(colorID);
        hudStage.setCurrentPlayersColorOnHud(currentPlayers);
        //currentPlayers = db.getCurrentPlayers();

        //System.out.println("##########db.getCurrentPlayerToAct() " + db.getCurrentPlayerToAct());
        //System.out.println("##########db.getCurrentPlayerToAct() " + db.getCurrentPlayers());
        //thisPlayer = db.getThisPlayer();
        //System.out.println("#####thisplayer " + db.getCurrentPlayerToAct().getColorID() + " ### " + db.getCurrentPlayerToAct().getNickname() + " ### " + db.getCurrentPlayerToAct().getArmyReserveCount());
        //users = db.getCurrentPlayers();
        //System.out.println("#### users : " + users);
        /*for (Player us : users
        ) {
            String name = us.getNickname();
            System.out.println("##########name " + us.getNickname());
        }*/
    }
}
