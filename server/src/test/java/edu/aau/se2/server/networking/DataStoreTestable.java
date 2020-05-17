package edu.aau.se2.server.networking;

import java.util.ArrayList;
import java.util.List;

import edu.aau.se2.server.data.DataStore;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;

public class DataStoreTestable extends DataStore {
    public DataStoreTestable() {
        super();
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(playersOnline.values());
    }

    public List<Lobby> getLobbies() {
        return new ArrayList<>(lobbies.values());
    }
}
