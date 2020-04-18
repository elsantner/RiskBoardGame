package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.Game;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.aau.se2.GdxTestRunner;
import edu.aau.se2.RiskGame;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(GdxTestRunner.class)
public class ExitButtonListenerTest {

    private Game game = mock(RiskGame.class);

    @Test
    public void touchDown() {
        ExitButtonListener listener = new ExitButtonListener(game);
        listener.touchDown(null, 0, 0, 0, 0);
        // TODO fix test
        //verify(game).setScreen(any());
    }
}