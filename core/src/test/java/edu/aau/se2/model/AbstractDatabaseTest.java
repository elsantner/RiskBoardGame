package edu.aau.se2.model;

import com.badlogic.gdx.Preferences;

import javax.naming.ldap.PagedResultsControl;

import edu.aau.se2.server.networking.MainServerTestable;
import static org.mockito.Mockito.mock;

public abstract class AbstractDatabaseTest {
    private static Preferences prefMock = mock(Preferences.class);
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
            dbs[i] = new DatabaseTestable(prefMock);
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
