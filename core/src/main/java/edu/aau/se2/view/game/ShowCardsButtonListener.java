package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


public class ShowCardsButtonListener extends ClickListener  {
    private static final String TAG = ShowCardsButtonListener.class.getName();

    public ShowCardsButtonListener() {
        super();
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        Gdx.app.log(TAG, "Spielkarten");
        System.out.println("### show cardStage here");
        return true;
    }
}
