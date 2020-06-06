package edu.aau.se2.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.aau.se2.model.listener.OnNicknameChangeListener;

import static org.junit.Assert.assertEquals;


public class NicknameTest extends AbstractDatabaseTest {
    private static final int NUM_CLIENTS = 1;
    private AtomicInteger changedNicknameCount;

    public NicknameTest() {
        super(NUM_CLIENTS);
    }

    @Before
    public void setup() throws IOException {
        server.start();
        setupScenario();
    }

    private void setupScenario() {
        changedNicknameCount = new AtomicInteger(0);
    }

    @Test
    public void testChangeName() throws InterruptedException, IOException {
        for (DatabaseTestable db : dbs) {
            db.getListeners().setNicknameListener(new OnNicknameChangeListener() {
                @Override
                public void nicknameChanged(String nickname)  {
                    changedNicknameCount.addAndGet(1);
                }
            });
        }

        for(DatabaseTestable db : dbs){
            db.connect();
        }

        Thread.sleep(1000);

        dbs[0].setPlayerNickname("test");
        int clientId = dbs[0].getThisPlayer().getUid();
        Thread.sleep(2000);

        for (DatabaseTestable db : dbs) {
            if(db.getThisPlayer().getUid() == clientId){
                assertEquals("test", db.getThisPlayer().getNickname());
            }
        }
        assertEquals(NUM_CLIENTS, changedNicknameCount.get());
    }

    @After
    public void teardown() {
        disconnectAll();
    }
}