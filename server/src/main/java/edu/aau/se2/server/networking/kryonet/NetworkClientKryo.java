package edu.aau.se2.server.networking.kryonet;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.NetworkClient;
import edu.aau.se2.server.networking.dto.BaseMessage;

public class NetworkClientKryo implements NetworkClient, KryoNetComponent {
    private Logger log;
    private Client client;
    private Callback<BaseMessage> callback;

    public NetworkClientKryo() {
        setupLogger();
        client = new Client();
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (callback != null && object instanceof BaseMessage) {
                    callback.callback((BaseMessage) object);
                }
            }
        });
    }

    private void setupLogger() {
        log = Logger.getLogger("Client");
        if (log.getHandlers().length == 0) {
            Handler handlerObj = new ConsoleHandler();
            handlerObj.setLevel(Level.INFO);
            log.addHandler(handlerObj);
        }
        log.setLevel(Level.INFO);
        log.setUseParentHandlers(false);
    }

    @Override
    public void registerClass(Class<?> c) {
        client.getKryo().register(c);
    }

    @Override
    public void connect(String host) throws IOException {
        client.start();
        client.setKeepAliveTCP(8000);
        client.connect(5000, host, NetworkConstants.TCP_PORT);
    }

    @Override
    public void disconnect() {
        client.stop();
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public void registerCallback(Callback<BaseMessage> callback) {
        this.callback = callback;
    }

    @Override
    public void sendMessage(BaseMessage message) {
        log.info(String.format("[Client] Sending %s", message.getClass().getSimpleName()));
        client.sendTCP(message);
    }

    @Override
    public void registerConnectionListener(OnConnectionChangedListener listener) {
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                super.connected(connection);
                listener.connected();
            }

            @Override
            public void disconnected(Connection connection) {
                super.disconnected(connection);
                listener.disconnected();
            }
        });
    }
}
