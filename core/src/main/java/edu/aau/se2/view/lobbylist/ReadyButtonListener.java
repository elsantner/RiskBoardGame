package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import edu.aau.se2.model.Database;

public class ReadyButtonListener extends ClickListener {

    private static final String TAG = ReadyButtonListener.class.getName();

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        Gdx.app.log(TAG, "Bereit");
        Database.getInstance().togglePlayerReady();
        return true;
    }
}
