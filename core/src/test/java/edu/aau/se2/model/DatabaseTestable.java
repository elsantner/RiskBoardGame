package edu.aau.se2.model;

import com.badlogic.gdx.Preferences;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.networking.Callback;

public class DatabaseTestable extends Database {
    public DatabaseTestable(Preferences prefs) {
        super(prefs);
    }

    /**
     * Sets the server address. This can only be done before a instance of Database is created.
     * Note: This method is designed to be used for testing purposes only!
     * @param serverAddress New server address
     */
    public static void setServerAddress(String serverAddress) {
        Database.serverAddress = serverAddress;
    }

    public static DatabaseTestable getClientToAct(DatabaseTestable[] dbs) {
        for (DatabaseTestable db : dbs) {
            if (db.isThisPlayersTurn()) {
                return db;
            }
        }
        return null;
    }

    public static DatabaseTestable getDifferentClient(DatabaseTestable[] dbs, DatabaseTestable client) {
        for (DatabaseTestable db : dbs) {
            if (db.getThisPlayer().getUid() != client.getThisPlayer().getUid()) {
                return db;
            }
        }
        return null;
    }

    /**
     * Starts the game.
     * Requires lobby to be setup before.
     * @param dbs Database clients
     * @param timeoutMS Maximum wait time
     * @throws TimeoutException If timeout is exceeded
     */
    public static void startLobby(DatabaseTestable[] dbs, int timeoutMS) throws TimeoutException {
        AtomicInteger msgsReceived = new AtomicInteger(0);
        for (DatabaseTestable db : dbs) {
            db.getListeners().setNextTurnListener((playerID, isThisPlayer) -> msgsReceived.addAndGet(1));
            db.setPlayerReady(true);
        }

        // wait until initial army placement is finished
        wait(() -> msgsReceived.get() == dbs.length, timeoutMS);
        for (DatabaseTestable db : dbs) {
            db.getListeners().setNextTurnListener(null);
        }
    }

    /**
     * Places all initial armies
     * Requires game to be started before.
     * @param dbs Database clients
     * @param timeoutMS Maximum wait time
     * @throws TimeoutException If timeout is exceeded
     */
    public static void setupGame(DatabaseTestable[] dbs, int timeoutMS) throws TimeoutException {
        AtomicBoolean firstTurnArmiesReceived = new AtomicBoolean(false);

        for (DatabaseTestable db : dbs) {
            db.setupInitialArmyPlacingListener();
        }
        DatabaseTestable clientToAct = getClientToAct(dbs);
        clientToAct.armyPlaced(clientToAct.getNextTerritoryToPlaceArmiesOn().getId(), 1);
        // this listener is called with isInitialCount==true once all initial armies are placed and the first proper turn has started
        clientToAct.getListeners().setArmyReserveChangedListener((armyCount, isInitialCount) -> {
            if (isInitialCount) {
                firstTurnArmiesReceived.set(true);
            }
        });

        // wait until initial army placement is finished
        wait(firstTurnArmiesReceived::get, timeoutMS);
        for (DatabaseTestable db : dbs) {
            db.getListeners().setNextTurnListener(null);
        }
        clientToAct.getListeners().setArmyReserveChangedListener(null);
    }

    /**
     * Places all armies of given Database.
     * @param db Database to place armies for. (Must be player to act in order for this method to work properly.)
     * @param timeoutMS Maximum wait time
     * @throws TimeoutException If timeout is exceeded
     */
    public static void placeTurnArmies(DatabaseTestable db, int timeoutMS) throws TimeoutException {
        db.getListeners().setArmyReserveChangedListener((armyCount, isInitialCount) -> {
            if (armyCount > 0) {
                db.armyPlaced(db.getNextTerritoryToPlaceArmiesOn().getId(), 1);
            }
        });
        db.armyPlaced(db.getNextTerritoryToPlaceArmiesOn().getId(), 1);

        // wait until army placement is finished
        wait(() -> db.getCurrentArmyReserve() == 0, timeoutMS);
        db.getListeners().setArmyReserveChangedListener(null);
    }

    /**
     * Sets up a lobby with all Databases (host = dbs[0]).
     * @param dbs Databases to join lobby
     * @param timeoutMS Maximum wait time
     * @throws IOException If connection error occurs.
     * @throws TimeoutException If timeout is exceeded
     */
    public static void setupLobby(DatabaseTestable[] dbs, int timeoutMS) throws IOException, TimeoutException {
        AtomicInteger joinCountRemaining = new AtomicInteger(dbs.length - 1);

        dbs[0].connectAndHost(lobbyID -> {
            for (int i=1; i<dbs.length; i++) {
                try {
                    dbs[i].connectAndJoin(lobbyID, argument -> {
                        joinCountRemaining.addAndGet(-1);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // wait until all clients have joined
        wait(() -> joinCountRemaining.get() == 0, timeoutMS);
        for (DatabaseTestable db : dbs) {
            db.getListeners().setConnectionChangedListener(null);
        }
    }

    private void connectAndHost(Callback<Integer> cb) throws IOException {
        getListeners().setConnectionChangedListener(new OnConnectionChangedListener() {
            @Override
            public void connected(Player thisPlayer) {
                hostLobby();
            }

            @Override
            public void disconnected() {

            }
        });
        getListeners().setJoinedLobbyListener((lobbyID, host, players) -> cb.callback(lobbyID));
        connect();
    }

    public void connectAndJoin(int lobbyID, Callback<Void> cb) throws IOException {
        getListeners().setConnectionChangedListener(new OnConnectionChangedListener() {
            @Override
            public void connected(Player thisPlayer) {
                joinLobby(lobbyID);
            }

            @Override
            public void disconnected() {

            }
        });
        getListeners().setJoinedLobbyListener((lID, host, players) -> cb.callback(null));
        connect();
    }

    public void setupInitialArmyPlacingListener() {
        getListeners().setNextTurnListener((playerID, isThisPlayer) -> {
            if (isThisPlayer && !getLobby().areInitialArmiesPlaced()) {
                armyPlaced(getNextTerritoryToPlaceArmiesOn().getId(), 1);
            }
        });
    }

    public Territory getNextTerritoryToPlaceArmiesOn() {
        List<Territory> unoccupiedTerritories = Arrays.asList(getLobby().getUnoccupiedTerritories());
        if (!unoccupiedTerritories.isEmpty()) {
            return getRandomTerritory(unoccupiedTerritories);
        }
        else {
            return getRandomTerritory(Arrays.asList(getMyTerritories()));
        }
    }

    private Territory getRandomTerritory(List<Territory> territories) {
        Random rand = new Random();
        return territories.get(territories.size() > 1 ? rand.nextInt(territories.size()-1) : 0);
    }

    public Territory getMyTerritory(int minArmyCount) {
        for (Territory t : getMyTerritories()) {
            if (t.getArmyCount() >= minArmyCount) {
                return t;
            }
        }
        return null;
    }

    public Territory getDifferentMyTerritory(int territoryID) {
        for (Territory t : getMyTerritories()) {
            if (t.getId() != territoryID) {
                return t;
            }
        }
        return null;
    }

    private static void wait(WaitingCondition condition, int timeoutMS) throws TimeoutException {
        long startTime = System.currentTimeMillis();
        while (!condition.isDone()) {
            if ((System.currentTimeMillis() - startTime) > timeoutMS) {
                throw new TimeoutException();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {/*goto loop header*/}
        }
    }

    private interface WaitingCondition {
        boolean isDone();
    }
}
