package edu.aau.se2.view.lobbylist;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.aau.se2.model.Database;
import edu.aau.se2.view.GdxTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(GdxTestRunner.class)
public class ExitButtonListenerTest {

    @Test
    public void touchDown() {
        Database db = mock(Database.class);
        ExitButtonListener listener = new ExitButtonListener(db);
        listener.touchDown(null, 0, 0, 0, 0);
        verify(db).leaveLobby();
    }
}