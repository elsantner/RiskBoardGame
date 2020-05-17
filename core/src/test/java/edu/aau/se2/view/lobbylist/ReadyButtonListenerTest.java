package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.aau.se2.model.Database;
import edu.aau.se2.view.GdxTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(GdxTestRunner.class)
public class ReadyButtonListenerTest {

    @Test
    public void touchDown() {
        Database db = mock(Database.class);
        ClickListener listener = new ReadyButtonListener(db);
        listener.touchDown(null, 0,0,0,0);
        verify(db).togglePlayerReady();
    }
}