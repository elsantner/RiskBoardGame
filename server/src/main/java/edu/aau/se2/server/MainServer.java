package edu.aau.se2.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.CreateLobby;
import edu.aau.se2.server.networking.dto.TextMessage;
import edu.aau.se2.server.networking.dto.UserList;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;
import edu.aau.se2.server.networking.kryonet.RegisterClasses;

import static java.lang.Thread.sleep;

public class MainServer {
    private static NetworkServerKryo server;
    private static int lobbyNumber = 0;
    private static ArrayList<Lobby> lobbys;
    private static final Logger LOGGER = Logger.getLogger(MainServer.class.getName());


    public static void main(String[] args) {
        lobbys = new ArrayList<>();
        try {
            server = new NetworkServerKryo();

            RegisterClasses.registerClasses(server);

            server.registerCallback(new Callback<BaseMessage>() {
                @Override
                public void callback(BaseMessage arg) {
                    if (arg instanceof TextMessage) {
                        server.broadcastMessage(new TextMessage("Received: " + ((TextMessage) arg).getText()));
                    } else if (arg instanceof CreateLobby) {
                        hostLobby(((CreateLobby) arg).getUserName());
                    }
                }
            });
            server.start();
        } catch (IOException e) {
            LOGGER.severe("Connection error: " + e.getMessage());

        }
    }

    private static void hostLobby(String userName) {
        lobbys.add(new Lobby(userName + " lobby: " + lobbyNumber, lobbyNumber));

        // only for testing
        lobbys.get(lobbyNumber).addUser(new User("User 2: lobby:" + lobbyNumber));
        lobbys.get(lobbyNumber).addUser(new User("User 3: lobby:" + lobbyNumber));
        lobbys.get(lobbyNumber).getUser(1).setReady(true);

        server.broadcastLobbyMessage((lobbyNumber), new TextMessage("Lobby " + lobbyNumber + " erstellt!"));
        server.broadcastLobbyMessage((lobbyNumber), new UserList((ArrayList<User>) lobbys.get(lobbyNumber).getUsers()));


        // to long sleep, makes other devices disconnect (they need "keepalive" signal)
        try {
            sleep(2000);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        lobbys.get(lobbyNumber).setUser(1, new User("User 4: lobby:" + lobbyNumber));
        lobbys.get(lobbyNumber).addUser(new User("User 5: lobby:" + lobbyNumber, true));
        lobbys.get(lobbyNumber).getUser(2).setReady(true);

        server.broadcastLobbyMessage((lobbyNumber), new UserList((ArrayList<User>) lobbys.get(lobbyNumber).getUsers()));
        lobbyNumber++;
    }
}
