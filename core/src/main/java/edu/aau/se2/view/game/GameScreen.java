package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

import edu.aau.se2.RiskGame;
import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnPhaseChangedListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.asset.AssetName;

/**
 * @author Elias
 */
public class GameScreen extends AbstractScreen implements OnTerritoryUpdateListener, OnNextTurnListener,
        OnHUDInteractionListener, OnPhaseChangedListener, OnBoardInteractionListener {
    private BoardStage boardStage;
    private TempHUDStage tmpHUDStage;
    private CardStage cardStage;
    private Database db;

    public GameScreen(RiskGame game) {
        this(game, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public GameScreen(RiskGame game, int width, int height) {
        super(game);
        boardStage = new BoardStage(this, new FitViewport(width, height));
        tmpHUDStage = new TempHUDStage(this, new FitViewport(width, height), this);
        cardStage = new CardStage(this, new FitViewport(width, height));
        db = Database.getInstance();
        boardStage.setListener(this);
        db.setTerritoryUpdateListener(this);
        db.setNextTurnListener(this);
        db.setPhaseChangedListener(this);
        db.setCardsChangedListener(cardStage);
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
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new CustomGestureDetector(boardStage));
        inputMultiplexer.addProcessor(tmpHUDStage);
        inputMultiplexer.addProcessor(cardStage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        // render "unused" area in red
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boardStage.draw();

        //todo remove (add button in Hud to show cards)
        /*
        if (cardStage.isUpdated()) {
            cardStage.updateActor();
        }
        cardStage.act();
        cardStage.draw();
        */

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
        cardStage.dispose();
        // clear all graphical territory data
        Territory.dispose();
    }

    @Override
    public void territoryUpdated(int territoryID, int armyCount, int colorID) {
        boardStage.setArmyCount(territoryID, armyCount);
        boardStage.setArmyColor(territoryID, colorID);
    }

    private void showFinishTurnDialog() {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);
        ConfirmDialog dialog = new ConfirmDialog(uiSkin, "Zug beenden",
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
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);
        ConfirmDialog dialog = new ConfirmDialog(uiSkin, "Phase beenden",
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
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);
        SelectCountDialog dialog = new SelectCountDialog(uiSkin, "Einheitenanzahl", 1,
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
        dialog.setOrigin(Align.center);
    }

    private void showAskForCardExchange() {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);
        ConfirmDialog dialog = new ConfirmDialog(uiSkin, "Kartentausch",
                "Moechten Sie 3 Karten eintauschen?", "Ja", "Nein",
                result -> {
                    db.exchangeCards(result);
                    boardStage.setInteractable(true);
                });
        showDialog(dialog);
    }

    @Override
    public void isPlayersTurnNow(int playerID, boolean isThisPlayer) {
        if (db.getThisPlayer() != null && playerID == db.getThisPlayer().getUid() && db.getThisPlayer().isAskForCardExchange()) {
            showAskForCardExchange();
        }
    }

    @Override
    public void stageSkipButtonClicked() {
        if (db.getCurrentPhase() == Database.Phase.ATTACKING) {
            showSkipAttackingPhaseDialog();
        } else if (db.getCurrentPhase() == Database.Phase.MOVING) {
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
        // TODO: implement attacking
    }
}
