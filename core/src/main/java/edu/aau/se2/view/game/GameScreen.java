package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.List;

import edu.aau.se2.RiskGame;
import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnArmyReserveChangedListener;
import edu.aau.se2.model.listener.OnAttackUpdatedListener;
import edu.aau.se2.model.listener.OnLeftGameListener;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnPhaseChangedListener;
import edu.aau.se2.model.listener.OnPlayerLostListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;
import edu.aau.se2.model.listener.OnVictoryListener;
import edu.aau.se2.server.data.Attack;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.asset.AssetName;
import edu.aau.se2.view.dices.DiceStage;

public class GameScreen extends AbstractScreen implements OnTerritoryUpdateListener, OnNextTurnListener,
        OnHUDInteractionListener, OnPhaseChangedListener, OnBoardInteractionListener, OnAttackUpdatedListener,
        OnArmyReserveChangedListener, OnLeftGameListener, OnPlayerLostListener, OnVictoryListener {
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

        boardStage.setListener(this);
        hudStage = new HudStage(this, new FitViewport(width, height), db.getLobby().getPlayers(), this);
        db.getListeners().setTerritoryUpdateListener(this);
        db.getListeners().setNextTurnListener(this);
        db.getListeners().setPhaseChangedListener(this);
        db.getListeners().setCardsChangedListener(cardStage);
        db.getListeners().setAttackUpdatedListener(this);
        db.getListeners().setArmyReserveChangedListener(this);
        db.getListeners().setLeftGameListener(this);
        db.getListeners().setPlayerLostListener(this);
        db.getListeners().setVictoryListener(this);

        diceStage = new DiceStage(new FitViewport(width, height), this);

        // trigger player turn update because listener might not have been registered when
        // server message was received
        if (db.getLobby().getPlayerToAct() != null) {   // only if initial army placing message was received already
            isPlayersTurnNow(db.getLobby().getPlayerToAct().getUid(), db.isThisPlayersTurn());
            setPlayersDataOnHud(db.getLobby().getPlayers());
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
        addInputProcessor(hudStage);
        addInputProcessor(new CustomGestureDetector(boardStage));
        addInputProcessor(diceStage);
        addInputProcessor(cardStage);
    }

    @Override
    public void render(float delta) {
        // render "unused" area in red
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boardStage.draw();
        diceStage.act(delta);
        diceStage.draw();

        hudStage.getViewport().apply();
        hudStage.update();
        hudStage.draw();

        if (hudStage.getShowCards()) {
            if (cardStage.isUpdated()) {
                cardStage.updateActor();
            }
            cardStage.act();
            cardStage.draw();
        }
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
    public void handleBackButton() {
        hudStage.showLeaveDialog();
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
        int playerColor = db.getLobby().getPlayerToAct().getColorID();
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
                db.getLobby().getTerritoryByID(fromTerritoryID).getArmyCount() - 1,
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
                Math.min(db.getLobby().getTerritoryByID(fromTerritoryID).getArmyCount() - 1, 3),
                result -> {
                    if (result > 0) {
                        db.startAttack(fromTerritoryID, onTerritoryID, result);
                    }
                    boardStage.setInteractable(true);
                });
        showDialog(dialog);
    }

    private void showStartDefendDialog(int onTerritoryID) {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);
        SelectCountDialog dialog = new SelectCountDialog(uiSkin, "Verteidigen", "Wuerfelanzahl waehlen", 1,
                Math.min(db.getLobby().getTerritoryByID(onTerritoryID).getArmyCount(), 2),
                result -> {
                    if (result > 0) {
                        db.sendDefenderDiceCount(result);
                    }
                    boardStage.setInteractable(true);
                });
        dialog.setAbortAllowed(false);

        super.showDialog(dialog, hudStage, 2, Align.bottomRight);
    }

    private void showOccupyTerritoryDialog(int fromTerritoryID, int toTerritoryID) {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);
        String toTerritoryName = Territory.getByID(toTerritoryID).getTerritoryName();
        SelectCountDialog dialog = new SelectCountDialog(uiSkin, "Territorium einnehmen",
                String.format("Einheiten nach '%s' verschieben", toTerritoryName), 1,
                db.getLobby().getTerritoryByID(fromTerritoryID).getArmyCount() - 1,

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
        super.showDialog(dialog, hudStage, hudStage.getViewport().getWorldHeight() * 0.0027f);
    }

    private void showAskForCardExchange() {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);

        boolean state = hudStage.getShowCards();
        if (!state) {
            hudStage.setShowCards(true);
        }
        ConfirmDialog dialog = new ConfirmDialog(uiSkin, "Kartentausch",
                "Moechten Sie 3 Karten eintauschen?", "Ja", "Nein",
                result -> {
                    db.exchangeCards(result);
                    if (!state) {
                        hudStage.setShowCards(false);
                    }
                    boardStage.setInteractable(true);
                });
        showDialog(dialog);
    }

    private void showPlayerLostDialog(String playerName, boolean thisPlayerLost) {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);
        Dialog dialog;
        if (thisPlayerLost) {
            dialog = new Dialog("Du hast verloren!", uiSkin) {
                @Override
                protected void result(Object object) {
                    super.result(object);
                    boardStage.setInteractable(true);
                    this.hide();
                    this.remove();
                }
            };
            dialog.text("Du kannst das Spiel nun verlassen");
            dialog.button("Okay");

        } else {
            dialog = new Dialog("Spieler hat verloren", uiSkin) {
                @Override
                protected void result(Object object) {
                    super.result(object);
                    boardStage.setInteractable(true);
                    this.hide();
                    this.remove();
                }
            };
            dialog.text("Spieler " + playerName + " hat das Spiel verloren!");
            dialog.button("Okay");
        }

        dialog.setMovable(false);
        showDialog(dialog);
    }

    private void showVictoryDialog(String playerName, boolean thisPLayerWon) {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        boardStage.setInteractable(false);
        Dialog dialog;
        if (thisPLayerWon) {
            dialog = new Dialog("Glueckwunsch!", uiSkin) {
                @Override
                protected void result(Object object) {
                    super.result(object);
                    db.leaveLobby();
                    this.hide();
                    this.remove();
                }
            };
            dialog.text("Du hast das Spiel gewonnen!");
            dialog.button("Spiel verlassen");

        } else {
            dialog = new Dialog("Spielende", uiSkin) {
                @Override
                protected void result(Object object) {
                    super.result(object);
                    db.leaveLobby();
                    this.hide();
                    this.remove();
                }
            };
            dialog.text("Spieler " + playerName + " hat das Spiel gewonnen!");
            dialog.button("Spiel verlassen");
        }

        dialog.setMovable(false);
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
    public void startAttack(int fromTerritoryID, int onTerritoryID, int count) {
        Gdx.app.postRunnable(() -> showStartAttackDialog(fromTerritoryID, onTerritoryID));
    }

    @Override
    public void attackStarted() {
        attackUpdated();

        Gdx.app.postRunnable(() -> {
            hudStage.setPhaseSkipable(false);
            boardStage.attackStartable(false);

            Attack a = db.getLobby().getCurrentAttack();
            if (a != null && db.isThisPlayersTurn()) {
                List<Integer> result = DiceStage.rollDice(a.getAttackerDiceCount());
                db.sendAttackerResults(result, false);
                diceStage.showResults(result, true);
            }

            if (a != null && db.isThisPlayerDefender()) {
                showStartDefendDialog(a.getToTerritoryID());
            }
        });
    }

    @Override
    public void attackUpdated() {
        Attack a = db.getLobby().getCurrentAttack();
        Gdx.app.postRunnable(() -> {
            hudStage.setCurrentAttack(a);

            if (a != null && a.isOccupyRequired() && db.isThisPlayersTurn()) {
                diceStage.hide();
                showOccupyTerritoryDialog(a.getFromTerritoryID(), a.getToTerritoryID());
            } else if (a != null && a.getDefenderDiceCount() != -1 && a.getDefenderDiceResults() == null && db.isThisPlayerDefender()) {
                List<Integer> result = DiceStage.rollDice(a.getDefenderDiceCount());
                db.sendDefenderResults(result);
                diceStage.showResults(result, false);
            } else if (a != null) {
                if (a.getAttackerDiceResults() != null) {
                    diceStage.showResults(a.getAttackerDiceResults(), true);
                }
                if (a.getDefenderDiceResults() != null) {
                    diceStage.showResults(a.getDefenderDiceResults(), false);
                }
            }
        });
    }

    @Override
    public void attackFinished() {
        attackUpdated();

        Gdx.app.postRunnable(() -> {
            hudStage.setPhaseSkipable(true);
            boardStage.attackStartable(true);
            diceStage.hide();
        });
    }

    public void setPlayersDataOnHud(List<Player> currentPlayers) {
        hudStage.setCurrentPlayersColorOnHud(currentPlayers);
    }

    @Override
    public void newArmyCount(int armyCount, boolean isInitialCount) {
        hudStage.setArmyReserveCount(armyCount);
    }

    @Override
    public void removePlayerTerritories(List<Integer> ids) {
        for (int i : ids) {
            boardStage.setArmyCount(i, 0);
        }
    }

    @Override
    public void informPlayersThatPlayerLost(String playerName, boolean thisPlayerLost) {
        showPlayerLostDialog(playerName, thisPlayerLost);
    }

    @Override
    public void playerWon(String playerName, boolean thisPlayerWon) {
        showVictoryDialog(playerName, thisPlayerWon);
    }
}
