package edu.aau.se2.server.networking.kryonet;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.NetworkServer;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.CreateLobby;

public class NetworkServerKryo implements NetworkServer, KryoNetComponent {
    private Server server;
    private Callback<BaseMessage> messageCallback;

    private List<List<Connection>> connections;
    private int lobbyCount = 0;

    public NetworkServerKryo() {
        server = new Server();
        connections = new ArrayList<>();
    }

    @Override
    public void registerClass(Class c) {
        server.getKryo().register(c);
    }

    @Override
    public void start() throws IOException {
        server.start();
        server.bind(NetworkConstants.TCP_PORT);
        server.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof CreateLobby) {
                    connections.add(new ArrayList<>());
                    connections.get(lobbyCount).add(connection);
                    lobbyCount++;
                }
                if (messageCallback != null && object instanceof BaseMessage)
                    messageCallback.callback((BaseMessage) object);
            }
        });
    }

    @Override
    public void stop() {
        server.stop();
    }

    @Override
    public void registerCallback(Callback<BaseMessage> callback) {
        this.messageCallback = callback;
    }

    @Override
    public void broadcastMessage(BaseMessage message) {
        for (Connection connection : server.getConnections())
            connection.sendTCP(message);
    }

    public void broadcastLobbyMessage(int id, BaseMessage message) {
        for (Connection connection : connections.get(id)
        ) {
            connection.sendTCP(message);
        }
    }
}
