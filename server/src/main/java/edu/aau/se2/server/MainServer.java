package edu.aau.se2.server;

import java.io.IOException;
import java.util.ArrayList;

import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.TextMessage;
import edu.aau.se2.server.networking.dto.UserList;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;

public class MainServer {
    private static NetworkServerKryo server;
    private static int lobbyNumber = 0;
    private static ArrayList<Lobby> lobbys;

    public static void main(String[] args) {
        lobbys = new ArrayList<>();
        try {
            server = new NetworkServerKryo();
            server.registerClass(TextMessage.class);
            server.registerClass(ArrayList.class);
            server.registerClass(User.class);
            server.registerClass(UserList.class);
            server.registerCallback(new Callback<BaseMessage>() {
                @Override
                public void callback(BaseMessage arg) {
                    if (arg instanceof TextMessage) {
                        server.broadcastMessage(new TextMessage("Received: " + ((TextMessage) arg).text));

                        if (((TextMessage) arg).text.equals("host")) {

                            lobbys.add(new Lobby("User: " + lobbyNumber++));
                            lobbys.get(lobbyNumber-1).addUser("User2");
                            lobbys.get(lobbyNumber-1).addUser("User3");
                            server.broadcastMessage(new TextMessage("Lobby " + lobbyNumber + " erstellt!"));

                            server.broadcastMessage(new UserList(lobbys.get(lobbyNumber-1).getUsers()));
                        }
                    }
                }
            });
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
