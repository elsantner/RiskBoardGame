package edu.aau.se2.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnArmyReserveChangedListener;
import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.networking.Callback;

public class DatabaseTestable extends Database {
    public DatabaseTestable() {
        super();
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

    public static void setupGame(DatabaseTestable[] dbs, int timeoutMS) throws TimeoutException {
        for (DatabaseTestable db : dbs) {
            db.setupInitialArmyPlacingListener();
            db.setPlayerReady(true);
        }

        // wait until initial army placement is finished
        long startTime = System.currentTimeMillis();
        while (!dbs[dbs.length - 1].isInitialArmyPlacementFinished()) {
            if ((System.currentTimeMillis() - startTime) > timeoutMS) {
                throw new TimeoutException("setup game did not finish within " + timeoutMS + " ms");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // to to loop header
            }
        }
    }

    public static void placeTurnArmies(DatabaseTestable db, int timeoutMS) throws TimeoutException {
        db.setArmyReserveChangedListener(new OnArmyReserveChangedListener() {
            @Override
            public void newArmyCount(int armyCount, boolean isInitialCount) {
                if (armyCount > 0) {
                    db.armyPlaced(db.getNextTerritoryToPlaceArmiesOn().getId(), 1);
                }
            }
        });
        db.armyPlaced(db.getNextTerritoryToPlaceArmiesOn().getId(), 1);

        // wait until initial army placement is finished
        long startTime = System.currentTimeMillis();
        while (db.getCurrentArmyReserve() != 0) {
            if ((System.currentTimeMillis() - startTime) > timeoutMS) {
                throw new TimeoutException("setup game did not finish within " + timeoutMS + " ms");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // to to loop header
            }
        }
    }

    /**
     * Sets up a lobby with all Databases (host = dbs[0]).
     * @param dbs Databases to join lobby
     * @throws IOException If connection error occurs.
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
        long startTime = System.currentTimeMillis();
        while (joinCountRemaining.get() != 0) {
            if ((System.currentTimeMillis() - startTime) > timeoutMS) {
                throw new TimeoutException("setup lobby did not finish within " + timeoutMS + " ms");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // to to loop header
            }
        }
    }

    private void connectAndHost(Callback<Integer> cb) throws IOException {
        setConnectionChangedListener(new OnConnectionChangedListener() {
            @Override
            public void connected(Player thisPlayer) {
                hostLobby();
            }

            @Override
            public void disconnected() {

            }
        });
        setJoinedLobbyListener((lobbyID, host, players) -> cb.callback(lobbyID));
        connect();
    }

    public void connectAndJoin(int lobbyID, Callback<Void> cb) throws IOException {
        setConnectionChangedListener(new OnConnectionChangedListener() {
            @Override
            public void connected(Player thisPlayer) {
                joinLobby(lobbyID);
            }

            @Override
            public void disconnected() {

            }
        });
        setJoinedLobbyListener((lID, host, players) -> cb.callback(null));
        connect();
    }

    public void setupInitialArmyPlacingListener() {
        setNextTurnListener((playerID, isThisPlayer) -> {
            if (isThisPlayer && !isInitialArmyPlacementFinished()) {
                armyPlaced(getNextTerritoryToPlaceArmiesOn().getId(), 1);
            }
        });
    }

    public Territory getNextTerritoryToPlaceArmiesOn() {
        List<Territory> unoccupiedTerritories = getUnoccupiedTerritories();
        if (!unoccupiedTerritories.isEmpty()) {
            return getRandomTerritory(unoccupiedTerritories);
        }
        else {
            return getRandomTerritory(getMyTerritories());
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
}
