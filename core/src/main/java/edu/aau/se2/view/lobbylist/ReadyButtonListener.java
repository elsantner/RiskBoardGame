package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import edu.aau.se2.model.Database;

public class ReadyButtonListener extends ClickListener {

    private final String TAG = ReadyButtonListener.class.getName();

    // TODO add network client

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        System.out.println("event = " + event + ", x = " + x + ", y = " + y + ", pointer = " + pointer + ", button = " + button);
        Gdx.app.log(TAG, "Bereit");
        Database.getInstance().togglePlayerReady();
        return true;
    }
}
