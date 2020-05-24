package edu.aau.se2.model;

import java.util.List;

import edu.aau.se2.model.listener.OnArmiesMovedListener;
import edu.aau.se2.model.listener.OnArmyReserveChangedListener;
import edu.aau.se2.model.listener.OnAttackUpdatedListener;
import edu.aau.se2.model.listener.OnCardsChangedListener;
import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.model.listener.OnErrorListener;
import edu.aau.se2.model.listener.OnGameStartListener;
import edu.aau.se2.model.listener.OnJoinedLobbyListener;
import edu.aau.se2.model.listener.OnLeftGameListener;
import edu.aau.se2.model.listener.OnLeftLobbyListener;
import edu.aau.se2.model.listener.OnLobbyListChangedListener;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnPhaseChangedListener;
import edu.aau.se2.model.listener.OnPlayersChangedListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.prelobby.LobbyListMessage;

public class ListenerManager {
    private OnGameStartListener gameStartListener;
    private OnPlayersChangedListener playersChangedListener;
    private OnTerritoryUpdateListener territoryUpdateListener;
    private OnNextTurnListener nextTurnListener;
    private OnCardsChangedListener cardsChangedListener;
    private OnJoinedLobbyListener joinedLobbyListener;
    private OnConnectionChangedListener connectionChangedListener;
    private OnArmyReserveChangedListener armyReserveChangedListener;
    private OnLobbyListChangedListener lobbyListChangedListener;
    private OnLeftLobbyListener onLeftLobbyListener;
    private OnErrorListener errorListener;
    private OnPhaseChangedListener phaseChangedListener;
    private OnArmiesMovedListener armiesMovedListener;
    private OnAttackUpdatedListener attackUpdatedListener;
    private OnLeftGameListener leftGameListener;

    public void setArmiesMovedListener(OnArmiesMovedListener l) {
        this.armiesMovedListener = l;
    }

    public void setPhaseChangedListener(OnPhaseChangedListener l) {
        this.phaseChangedListener = l;
    }

    public void setErrorListener(OnErrorListener l) {
        this.errorListener = l;
    }

    public void setLeftLobbyListener(OnLeftLobbyListener l) {
        this.onLeftLobbyListener = l;
    }

    public void setLobbyListChangedListener(OnLobbyListChangedListener l) {
        this.lobbyListChangedListener = l;
    }

    public void setConnectionChangedListener(OnConnectionChangedListener l) {
        this.connectionChangedListener = l;
    }

    public void setGameStartListener(OnGameStartListener l) {
        this.gameStartListener = l;
    }

    public void setPlayersChangedListener(OnPlayersChangedListener l) {
        this.playersChangedListener = l;
    }

    public void setTerritoryUpdateListener(OnTerritoryUpdateListener l) {
        this.territoryUpdateListener = l;
    }

    public void setNextTurnListener(OnNextTurnListener l) {
        this.nextTurnListener = l;
    }

    public void setCardsChangedListener(OnCardsChangedListener l) {
        this.cardsChangedListener = l;
    }

    public void setJoinedLobbyListener(OnJoinedLobbyListener l) {
        this.joinedLobbyListener = l;
    }

    public void setArmyReserveChangedListener(OnArmyReserveChangedListener l) {
        this.armyReserveChangedListener = l;
    }

    public void setAttackUpdatedListener(OnAttackUpdatedListener l) {
        this.attackUpdatedListener = l;
    }

    public void setLeftGameListener(OnLeftGameListener l) {
        this.leftGameListener = l;
    }

    void notifyPhaseChangedListener(Database.Phase phase) {
        if (phaseChangedListener != null) {
            phaseChangedListener.phaseChanged(phase);
        }
    }

    void notifyArmyReserveChangedListener(int newValue, boolean isInitialCount) {
        if (armyReserveChangedListener != null) {
            armyReserveChangedListener.newArmyCount(newValue, isInitialCount);
        }
    }

    void notifyGameStartListener(List<Player> players, int initialArmyCount) {
        if (gameStartListener != null) {
            gameStartListener.onGameStarted(players, initialArmyCount);
        }
    }

    void notifyPlayersChangedListener(List<Player> players) {
        if (playersChangedListener != null) {
            playersChangedListener.playersChanged(players);
        }
    }

    void notifyConnectedListener(Player thisPlayer) {
        if (connectionChangedListener != null) {
            connectionChangedListener.connected(thisPlayer);
        }
    }

    void notifyDisconnectedListener() {
        if (connectionChangedListener != null) {
            connectionChangedListener.disconnected();
        }
    }

    void notifyJoinedLobbyListener(int lobbyID, Player host, List<Player> players) {
        if (joinedLobbyListener != null) {
            joinedLobbyListener.joinedLobby(lobbyID, host, players);
        }
    }

    void notifyRefreshCardListener(String[] cardNames) {
        if (cardsChangedListener != null) {
            cardsChangedListener.refreshCards(cardNames);
        }
    }

    void notifySingleNewCardListener(String cardName) {
        if (cardsChangedListener != null) {
            cardsChangedListener.singleNewCard(cardName);
        }
    }

    void notifyNextTurnListener(int playerToActID, boolean isThisPlayer) {
        if (nextTurnListener != null) {
            nextTurnListener.isPlayersTurnNow(playerToActID, isThisPlayer);
        }
    }

    void notifyLobbyListChangedListener(List<LobbyListMessage.LobbyData> lobbyList) {
        if (lobbyListChangedListener != null) {
            lobbyListChangedListener.lobbyListChanged(lobbyList);
        }
    }

    void notifyLeftLobbyListener(boolean wasClosed) {
        if (onLeftLobbyListener != null) {
            onLeftLobbyListener.leftLobby(wasClosed);
        }
    }

    void notifyErrorListener(int errorCode) {
        if (errorListener != null) {
            errorListener.onError(errorCode);
        }
    }

    void notifyTerritoryUpdateListener(int territoryID, int armyCount, int colorID) {
        if (territoryUpdateListener != null) {
            territoryUpdateListener.territoryUpdated(territoryID, armyCount, colorID);
        }
    }

    void notifyArmiesMovedListener(int playerID, int fromTerritoryID, int toTerritoryID, int count) {
        if (armiesMovedListener != null) {
            armiesMovedListener.armiesMoved(playerID, fromTerritoryID, toTerritoryID, count);
        }
    }

    void notifyAttackStartedListener() {
        if (attackUpdatedListener != null) {
            attackUpdatedListener.attackStarted();
        }
    }

    void notifyAttackUpdatedListener() {
        if (attackUpdatedListener != null) {
            attackUpdatedListener.attackUpdated();
        }
    }

    void notifyAttackFinishedListener() {
        if (attackUpdatedListener != null) {
            attackUpdatedListener.attackFinished();
        }
    }

    void notifyLeftGameListener(int[] ids) {
        if (leftGameListener != null) {
            leftGameListener.removePlayerTerritories(ids);
        }
    }
}
