package edu.aau.se2.model;

import edu.aau.se2.server.networking.MainServerTestable;

public abstract class AbstractDatabaseTest {
    protected MainServerTestable server;
    protected DatabaseTestable[] dbs;

    protected AbstractDatabaseTest(int numClients) {
        server = new MainServerTestable();
        dbs = new DatabaseTestable[numClients];
        setupClients();
    }

    /**
     * Instantiate all database instances.
     */
    private void setupClients() {
        DatabaseTestable.setServerAddress("localhost");
        for (int i=0; i<dbs.length; i++) {
            dbs[i] = new DatabaseTestable();
        }
    }

    /**
     * Disconnect all database instances and then stop server.
     */
    protected void disconnectAll() {
        for (DatabaseTestable db : dbs) {
            db.closeConnection();
        }
        server.stop();
    }
}
