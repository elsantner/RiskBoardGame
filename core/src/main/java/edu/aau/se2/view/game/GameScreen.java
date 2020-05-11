package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.List;

import edu.aau.se2.RiskGame;
import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnAttackUpdatedListener;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnPhaseChangedListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;
import edu.aau.se2.server.data.Attack;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.asset.AssetName;
import edu.aau.se2.view.dices.DiceStage;

public class GameScreen extends AbstractScreen implements OnTerritoryUpdateListener, OnNextTurnListener,
        OnHUDInteractionListener, OnPhaseChangedListener, OnBoardInteractionListener, OnAttackUpdatedListener {
    private BoardStage boardStage;
    private DiceStage diceStage;
    private CardStage cardStage;
    private HudStage hudStage;
    private Database db;

    public GameScreen(RiskGame game) {
        this(game, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public GameScreen(RiskGame game, int width, int height) {
        super(game);
        boardStage = new BoardStage(this, new FitViewport(width, height));
        db = Database.getInstance();
        cardStage = new CardStage(this, new FitViewport(width, height));

        List<Integer> results = DiceStage.rollDice(true);
        diceStage = new DiceStage(new FitViewport(width, height), this, results, true);

        boardStage.setListener(this);
        hudStage = new HudStage(this, new FitViewport(width, height), db.getCurrentPlayers(), this);
        db.setTerritoryUpdateListener(this);
        db.setNextTurnListener(this);
        db.setPhaseChangedListener(this);
        db.setCardsChangedListener(cardStage);
        db.setAttackUpdatedListener(this);

        // trigger player turn update because listener might not have been registered when
        // server message was received
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
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new CustomGestureDetector(boardStage));
        inputMultiplexer.addProcessor(diceStage);
        inputMultiplexer.addProcessor(hudStage);
        inputMultiplexer.addProcessor(cardStage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        // render "unused" area in red
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boardStage.draw();
        diceStage.act(delta);
        diceStage.draw();

        if(hudStage.getShowCards()){
            if (cardStage.isUpdated()) {
                cardStage.updateActor();
            }
            cardStage.act();
            cardStage.draw();
        }

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
        cardStage.dispose();
        // clear all graphical territory data
        Territory.dispose();
    }

    @Override
    public void territoryUpdated(int territoryID, int armyCount, int colorID) {
        int playerColor = db.getCurrentPlayerToAct().getColorID();
        boardStage.setArmyCount(territoryID, armyCount);
        boardStage.setArmyColor(territoryID, colorID);
        hudStage.setPlayerTerritoryCount(territoryID, playerColor);
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
                        hudStage.setCurrentAttack(null);
                    }
                    boardStage.setInteractable(true);
                });
        showDialog(dialog);
    }

    private void showSelectCountDialog(int fromTerritoryID, int toTerritoryID) {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);
        SelectCountDialog dialog = new SelectCountDialog(uiSkin, "Einheitenanzahl", "Wie viele Einheiten wollen Sie verschieben?", 1,
                db.getTerritoryByID(fromTerritoryID).getArmyCount() - 1,
                result -> {
                    if (result > 0) {
                        db.armyMoved(fromTerritoryID, toTerritoryID, result);
                    }
                    boardStage.setInteractable(true);
                });
        showDialog(dialog);
    }

    private void showStartAttackDialog(int fromTerritoryID, int onTerritoryID) {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);
        SelectCountDialog dialog = new SelectCountDialog(uiSkin, "Angriff starten", "Wuerfelanzahl waehlen", 1,
                Math.min(db.getTerritoryByID(fromTerritoryID).getArmyCount() - 1, 3),
                result -> {
                    if (result > 0) {
                        db.attackStarted(fromTerritoryID, onTerritoryID, result);
                    }
                    boardStage.setInteractable(true);
                });
        showDialog(dialog);
    }

    private void showOccupyTerritoryDialog(int fromTerritoryID, int toTerritoryID) {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);
        String toTerritoryName = Territory.getByID(toTerritoryID).getTerritoryName();
        SelectCountDialog dialog = new SelectCountDialog(uiSkin, "Territorium einnehmen",
                String.format("Einheiten nach '%s' verschieben", toTerritoryName), 1,
                db.getTerritoryByID(fromTerritoryID).getArmyCount() - 1,

                result -> {
                    if (result > 0) {
                        db.occupyTerritory(toTerritoryID, fromTerritoryID, result);
                    }
                    boardStage.setInteractable(true);
                });
        dialog.setAbortAllowed(false);
        showDialog(dialog);
    }

    private void showDialog(Dialog dialog) {
        dialog.show(hudStage);
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
        hudStage.isPlayersTurnNow(playerID, isThisPlayer);
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
        hudStage.setPhase(newPhase);
        boardStage.setPhase(newPhase);
        diceStage.setPhase(newPhase);
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
    public void attackStarted(int fromTerritoryID, int onTerritoryID, int count) {
        showStartAttackDialog(fromTerritoryID, onTerritoryID);
    }

    @Override
    public void attackStarted() {
        attackUpdated();
        hudStage.setPhaseSkipable(false);
        boardStage.attackStartable(false);
    }

    @Override
    public void attackUpdated() {
        Attack a = db.getAttack();
        hudStage.setCurrentAttack(a);
        if (a != null && a.isOccupyRequired() && db.isThisPlayersTurn()) {
            showOccupyTerritoryDialog(a.getFromTerritoryID(), a.getToTerritoryID());
        }
    }

    @Override
    public void attackFinished() {
        attackUpdated();
        hudStage.setPhaseSkipable(true);
        boardStage.attackStartable(true);
    }

    public void setPlayersDataOnHud(List<Player> currentPlayers) {
        hudStage.setCurrentPlayersColorOnHud(currentPlayers);
    }
}
