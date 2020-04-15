package edu.aau.se2.server;

import java.io.IOException;
import java.util.ArrayList;
import edu.aau.se2.server.data.DataStore;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;

public class MainServer {

    public static void main(String[] args) {
        try {
            new MainServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private NetworkServerKryo server;
    private DataStore ds;

    public MainServer() {
        // TODO: Remove after lobbies are truly implemented
        ds = DataStore.getInstance();
        ds.createLobby();

        server = new NetworkServerKryo();
        SerializationRegister.registerClassesForComponent(server);
        server.registerCallback(new Callback<BaseMessage>() {
            @Override
            public void callback(BaseMessage arg) {
                if (arg instanceof ReadyMessage) {
                    handleReadyMessage((ReadyMessage) arg);
                }
            }
        });
    }

    public void start() throws IOException {
        server.start();
    }
    public void stop() {
        server.stop();
    }

    private synchronized void handleReadyMessage(ReadyMessage msg) {
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
        // TODO: remove once joinLobby is implemented
        lobby.addPlayer(new Player(msg.getFromPlayerID(), "Player" + msg.getFromPlayerID()));

        lobby.setPlayerReady(msg.getFromPlayerID(), msg.isReady());

        if (!lobby.isStarted() && lobby.canStartGame()) {
            lobby.setupForGameStart();
            lobby.setStarted(true);
            StartGameMessage sgm = new StartGameMessage(msg.getLobbyID(), 0, lobby.getPlayers(),
                    ArmyCountHelper.getStartCount(lobby.getPlayers().size()));
            server.broadcastMessage(sgm);
        }
    }
}
