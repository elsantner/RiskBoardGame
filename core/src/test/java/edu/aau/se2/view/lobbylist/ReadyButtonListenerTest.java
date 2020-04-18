package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.aau.se2.GdxTestRunner;

@RunWith(GdxTestRunner.class)
public class ReadyButtonListenerTest {

    @Test
    public void touchDown() {
        ClickListener listener = new ReadyButtonListener();
        // TODO set mocked network client
        // TODO verify(client).sendMessage(any());
    }
}