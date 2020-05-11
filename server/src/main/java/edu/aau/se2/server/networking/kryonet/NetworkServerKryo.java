package edu.aau.se2.server.networking.kryonet;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import edu.aau.se2.server.data.DataStore;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.NetworkServer;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.prelobby.ConnectedMessage;

public class NetworkServerKryo implements NetworkServer, KryoNetComponent {
    private Server server;
    private Callback<BaseMessage> messageCallback;
    private BiMap<Integer, Connection> connections;
    private boolean running;
    private DataStore ds;

    public NetworkServerKryo(DataStore ds) {
        server = new Server();
        connections = HashBiMap.create();
        running = false;
        this.ds = ds;
    }

    @Override
    public void registerClass(Class<?> c) {
        server.getKryo().register(c);
    }

    @Override
    public void start() throws IOException {
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
                Player newPlayer = ds.newPlayer();
                connections.put(newPlayer.getUid(), connection);
                synchronized (newPlayer) {
                    try {
                        newPlayer.wait(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                Logger.getAnonymousLogger().info("Sending ConnectedMessage");
                broadcastMessage(new ConnectedMessage(newPlayer), newPlayer);
            }

            @Override
            public void disconnected(Connection connection) {
                super.disconnected(connection);
                Integer disconnectedPlayerID = connections.inverse().remove(connection);
                ds.removePlayer(disconnectedPlayerID);
            }
        });
        server.start();
        running = true;
    }

    @Override
    public void stop() {
        server.stop();
        running = false;
    }

    @Override
    public void registerCallback(Callback<BaseMessage> callback) {
        this.messageCallback = callback;
    }

    @Override
    public void broadcastMessage(BaseMessage message) {
        logBroadcast(message);
        for (Connection connection : server.getConnections())
            connection.sendTCP(message);
    }

    public void broadcastMessage(BaseMessage message, Player recipient) {
        broadcastMessage(message, Collections.singletonList(recipient));
    }

    public void broadcastMessage(BaseMessage message, List<Player> recipients) {
        logBroadcast(message);
        for (Player p: recipients) {
            if (p != null) {
                Connection connection = connections.get(p.getUid());
                if (connection != null) {
                    connection.sendTCP(message);
                }
            }
        }
    }

    private void logBroadcast(BaseMessage message) {
        Logger.getAnonymousLogger().info("Broadcasting " + message.getClass().getSimpleName());
    }

    public boolean isRunning() {
        return running;
    }
}
