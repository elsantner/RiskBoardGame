package edu.aau.se2.server.networking.kryonet;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;

import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.NetworkClient;
import edu.aau.se2.server.networking.dto.BaseMessage;

public class NetworkClientKryo implements NetworkClient, KryoNetComponent {
    private Client client;
    private Callback<BaseMessage> callback;

    public NetworkClientKryo() {
        client = new Client();
    }

    @Override
    public void registerClass(Class c) {
        client.getKryo().register(c);
    }

    @Override
    public void connect(String host) throws IOException {
        client.start();
        client.connect(5000, host, NetworkConstants.TCP_PORT);

        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (callback != null && object instanceof BaseMessage)
                    callback.callback((BaseMessage) object);
            }
        });
    }

    @Override
    public void disconnect() {
        client.stop();
    }

    @Override
    public void registerCallback(Callback<BaseMessage> callback) {
        this.callback = callback;
    }

    @Override
    public void sendMessage(BaseMessage message) {
        client.sendTCP(message);
    }
}
