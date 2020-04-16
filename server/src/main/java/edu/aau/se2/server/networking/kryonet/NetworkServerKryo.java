package edu.aau.se2.server.networking.kryonet;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.aau.se2.server.data.DataStore;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.NetworkServer;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.ConnectedMessage;

public class NetworkServerKryo implements NetworkServer, KryoNetComponent {
    private Server server;
    private Callback<BaseMessage> messageCallback;
    private Map<Integer, Connection> connections;

    public NetworkServerKryo() {
        server = new Server();
        connections = new HashMap<>();
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
            @Override
            public void received(Connection connection, Object object) {
                if (messageCallback != null && object instanceof BaseMessage)
                    messageCallback.callback((BaseMessage) object);
            }

            @Override
            public void connected(Connection connection) {
                super.connected(connection);
                Player newPlayer = DataStore.getInstance().newPlayer();
                connections.put(newPlayer.getUid(), connection);
                broadcastMessage(new ConnectedMessage(newPlayer), newPlayer);
            }

            @Override
            public void disconnected(Connection connection) {
                super.disconnected(connection);
                connections.values().remove(connection);
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

    public void broadcastMessage(BaseMessage message, Player recipient) {
        broadcastMessage(message, Collections.singletonList(recipient));
    }

    public void broadcastMessage(BaseMessage message, List<Player> recipients) {
        for (Player p: recipients) {
            connections.get(p.getUid()).sendTCP(message);
        }
    }
}
