package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import edu.aau.se2.model.Database;

public class ExitButtonListener extends ClickListener {

    private final String TAG = ExitButtonListener.class.getName();

    private Game game;

    public ExitButtonListener(Game game) {
        super();
        this.game = game;
    }

    // TODO add network client

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        System.out.println("event = " + event + ", x = " + x + ", y = " + y + ", pointer = " + pointer + ", button = " + button);
        Gdx.app.log(TAG, "Verlassen");
        Database.getInstance().leaveLobby();
        return true;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
