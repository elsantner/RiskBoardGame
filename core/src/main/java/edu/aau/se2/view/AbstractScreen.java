package edu.aau.se2.view;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.Align;

import edu.aau.se2.RiskGame;

public abstract class AbstractScreen implements Screen {
    private RiskGame game;

    public AbstractScreen(RiskGame game) {
        this.game = game;
    }

    public RiskGame getGame() {
        return game;
    }

    public void showDialog(Dialog dialog, Stage stage, float scale) {
        dialog.show(stage);
        dialog.setScale(scale);
        dialog.setOrigin(Align.center);
    }
}
