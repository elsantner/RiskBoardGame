package edu.aau.se2.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.TextMessage;
import edu.aau.se2.server.networking.dto.UserList;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;
import edu.aau.se2.server.networking.kryonet.RegisterClasses;

import static java.lang.Thread.sleep;

public class MainServer {
    private static NetworkServerKryo server;
    private static int lobbyNumber = 0;
    private static ArrayList<Lobby> lobbys;
    private final static Logger LOGGER = Logger.getLogger(MainServer.class.getName());


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
                        if (((TextMessage) arg).getText().equals("host")) {
                            hostLobby();
                        }
                    }
                }
            });
            server.start();
        } catch (IOException e) {
            LOGGER.severe("Connection error: " + e.getMessage());

        }
    }


    private static void hostLobby() {
        lobbys.add(new Lobby("Us 1(host" + ++lobbyNumber + ")"));

        // only for testing
        lobbys.get(lobbyNumber - 1).addUser(new User("User 2"));
        lobbys.get(lobbyNumber - 1).addUser(new User("User 3"));
        lobbys.get(lobbyNumber - 1).getUser(1).setReady(true);

        server.broadcastMessage(new TextMessage("Lobby " + lobbyNumber + " erstellt!"));

        server.broadcastMessage(new UserList((ArrayList<User>) lobbys.get(lobbyNumber - 1).getUsers()));

        try {
            sleep(6000);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        lobbys.get(lobbyNumber - 1).setUser(1, new User("User 4"));
        lobbys.get(lobbyNumber - 1).addUser(new User("User 5", true));
        lobbys.get(lobbyNumber - 1).getUser(2).setReady(true);

        server.broadcastMessage(new UserList((ArrayList<User>) lobbys.get(lobbyNumber - 1).getUsers()));

    }
}
