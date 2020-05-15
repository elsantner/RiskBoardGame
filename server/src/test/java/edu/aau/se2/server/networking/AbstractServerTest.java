package edu.aau.se2.server.networking;

import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

public abstract class AbstractServerTest {
    protected MainServerTestable server;
    protected NetworkClientKryo[] clients;

    protected AbstractServerTest(int numClients) {
        server = new MainServerTestable();
        clients = new NetworkClientKryo[numClients];
        setupClients();
    }

    /**
     * Instantiate all clients and register serialization classes.
     */
    private void setupClients() {
        for (int i=0; i<clients.length; i++) {
            clients[i] = new NetworkClientKryo();
            SerializationRegister.registerClassesForComponent(clients[i]);
        }
    }

    /**
     * Disconnect all clients and then stop server.
     */
    protected void disconnectAll() {
        for (NetworkClientKryo c : clients) {
            c.disconnect();
        }
        server.stop();
    }
}
