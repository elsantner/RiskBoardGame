package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnPhaseChangedListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;

/**
 * @author Elias
 */
public class GameScreen implements Screen, OnTerritoryUpdateListener, OnNextTurnListener, OnHUDInteractionListener, OnPhaseChangedListener, OnBoardInteractionListener {
    private BoardStage boardStage;
    private TempHUDStage tmpHUDStage;
    private Database db;
    private InputMultiplexer inputMultiplexer;
    private AssetManager assetManager;

    public GameScreen(AssetManager assetManager) {
        this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), assetManager);
    }

    public GameScreen(int width, int height, AssetManager assetManager) {
        this.assetManager = assetManager;
        boardStage = new BoardStage(new FitViewport(width, height));
        tmpHUDStage = new TempHUDStage(new FitViewport(width, height), assetManager, this);
        db = Database.getInstance();
        boardStage.setListener(this);
        db.setTerritoryUpdateListener(this);
        db.setNextTurnListener(this);
        db.setPhaseChangedListener(this);
        // trigger player turn update because listener might not have been registered when
        // server message was received
        if (db.getCurrentPlayerToAct() != null) {   // only if initial army placing message was received already
            isPlayersTurnNow(db.getCurrentPlayerToAct().getUid(), db.isThisPlayersTurn());
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
        inputMultiplexer.addProcessor(tmpHUDStage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        showSelectCountDialog(0, 0);
    }

    @Override
    public void render(float delta) {
        // render "unused" area in red
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boardStage.draw();
        tmpHUDStage.draw();
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

    private void showFinishTurnDialog() {
        boardStage.setInteractable(false);
        ConfirmDialog dialog = new ConfirmDialog("Zug beenden",
                "Moechten Sie Ihren Zug beenden?", "Ja", "Nein",
                result -> {
                    if (result) {
                        db.finishTurn();
                    }
                    boardStage.setInteractable(true);
                });
        showDialog(dialog);
    }

    private void showSkipAttackingPhaseDialog() {
        boardStage.setInteractable(false);
        ConfirmDialog dialog = new ConfirmDialog("Phase beenden",
                "Moechten Sie die Angriffsphase beenden?", "Ja", "Nein",
                result -> {
                    if (result) {
                        db.finishAttackingPhase();
                    }
                    boardStage.setInteractable(true);
                });
        showDialog(dialog);
    }

    private void showSelectCountDialog(int fromTerritoryID, int toTerritoryID) {
        boardStage.setInteractable(false);
        SelectCountDialog dialog = new SelectCountDialog("Einheitenanzahl", 1,
                db.getTerritoryByID(fromTerritoryID).getArmyCount() - 1,
                result -> {
                    if (result > 0) {
                        db.armyMoved(fromTerritoryID, toTerritoryID, result);
                    }
                    boardStage.setInteractable(true);
                });
        showDialog(dialog);
    }

    private void showDialog(Dialog dialog) {
        dialog.show(tmpHUDStage);
        dialog.scaleBy(3);
        dialog.setOrigin(Align.center);
    }

    @Override
    public void isPlayersTurnNow(int playerID, boolean isThisPlayer) {
        // currently unused
    }

    @Override
    public void stageSkipButtonClicked() {
        if (db.getCurrentPhase() == Database.Phase.ATTACKING) {
            showSkipAttackingPhaseDialog();
        }
        else if (db.getCurrentPhase() == Database.Phase.MOVING) {
            showFinishTurnDialog();
        }
    }

    @Override
    public void phaseChanged(Database.Phase newPhase) {
        tmpHUDStage.setPhase(newPhase);
        boardStage.setPhase(newPhase);
    }

    @Override
    public void armyPlaced(int territoryID, int count) {
        db.armyPlaced(territoryID, count);
    }

    @Override
    public void armyMoved(int fromTerritoryID, int toTerritoryID, int count) {
        showSelectCountDialog(fromTerritoryID, toTerritoryID);
    }

    @Override
    public void attackStarted(int fromTerritoryID, int onTerritoryID) {

    }
}
