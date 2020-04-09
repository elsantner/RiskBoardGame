package edu.aau.se2.server;

import java.io.IOException;

import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.TextMessage;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;

public class MainServer {
    private static NetworkServerKryo server;

    public static void main(String[] args) {
        try {
            server = new NetworkServerKryo();
            server.registerClass(TextMessage.class);
            server.registerCallback(new Callback<BaseMessage>() {
                @Override
                public void callback(BaseMessage arg) {
                    if (arg instanceof TextMessage) {
                        server.broadcastMessage(new TextMessage("Received: " + ((TextMessage) arg).text));
                    }
                }
            });
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
