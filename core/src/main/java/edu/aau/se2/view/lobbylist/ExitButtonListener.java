package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import edu.aau.se2.model.Database;

public class ExitButtonListener extends ClickListener {

    private static final String TAG = ExitButtonListener.class.getName();

    private Database db;

    public ExitButtonListener() {
        this(Database.getInstance());
    }

    public ExitButtonListener(Database db) {
        super();
        this.db = db;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        Gdx.app.log(TAG, "Verlassen");
        db.leaveLobby();
        return true;
    }
}
