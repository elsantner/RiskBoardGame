package edu.aau.se2.server;

import java.io.IOException;
import java.util.ArrayList;
import edu.aau.se2.server.data.DataStore;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;

public class MainServer {
    private static NetworkServerKryo server;
    private static DataStore ds;

    public static void main(String[] args) {
        try {
            // TODO: Remove after testing
            ds = DataStore.getInstance();
            ds.createLobby();

            server = new NetworkServerKryo();
            registerClasses(server);
            server.registerCallback(new Callback<BaseMessage>() {
                @Override
                public void callback(BaseMessage arg) {
                    //TODO: when all players are ready, trigger this StartGameMessage
                    //handleReadyMessage(0);
                }
            });
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleReadyMessage(int lobbyID) {
        Lobby lobby = ds.getLobbyByID(lobbyID);
        if (lobby != null && lobby.canStartGame()) {
            lobby.setupForGameStart();
            StartGameMessage sgm = new StartGameMessage(lobbyID, 0, lobby.getPlayers(),
                    ArmyCountHelper.getStartCount(lobby.getPlayers().size()));
            server.broadcastMessage(sgm);
        }
    }

    private static void registerClasses(NetworkServerKryo server) {
        server.registerClass(Player.class);
        server.registerClass(ArrayList.class);
        server.registerClass(StartGameMessage.class);
    }
}
