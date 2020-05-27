package edu.aau.se2.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.Align;

import edu.aau.se2.RiskGame;
import edu.aau.se2.model.Database;

public abstract class AbstractScreen implements Screen, InputProcessor {
    private RiskGame game;
    private InputMultiplexer im;

    public AbstractScreen(RiskGame game) {
        this.game = game;
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        im = new InputMultiplexer();
        im.addProcessor(this);
        Gdx.input.setInputProcessor(im);
    }

    public RiskGame getGame() {
        return game;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACK) {
            handleBackButton();
            return true;
        }

        return false;
    }

    public void addInputProcessor(InputProcessor ip) {
        im.addProcessor(ip);
    }

    public void handleBackButton() {
        Database.getInstance().returnToMainMenu();
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public void showDialog(Dialog dialog, Stage stage, float scale) {
        dialog.show(stage);
        dialog.setScale(scale);
        dialog.setOrigin(Align.center);
    }
}
