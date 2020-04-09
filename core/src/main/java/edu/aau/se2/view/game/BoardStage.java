package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * @author Elias
 */
public class BoardStage extends Stage implements GestureDetector.GestureListener {
    private Texture texRiskBoard;
    private Image imgRiskBoard;

    private float prevZoomFactor = 1;

    public BoardStage() {
        super(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        loadAssets();
        setupBoardImage();
    }

    private void loadAssets() {
        texRiskBoard = new Texture("riskBoard.jpg");
        imgRiskBoard= new Image(texRiskBoard);
    }

    private void setupBoardImage() {
        imgRiskBoard.setWidth(Gdx.graphics.getWidth());
        imgRiskBoard.setHeight(Gdx.graphics.getHeight());

        this.addActor(imgRiskBoard);
    }

    public float getZoomFactor() {
        return ((OrthographicCamera) this.getCamera()).zoom;
    }

    public void setZoomFactor(float zoomFactor) {
        if (zoomFactor > 1 || zoomFactor < 0) {
            throw new IllegalArgumentException("zoomFactor must be between 0 and 1");
        }
        ((OrthographicCamera) this.getCamera()).zoom = zoomFactor;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        // every time the player touches the screen, remember current zoom factor
        prevZoomFactor = getZoomFactor();
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        float newZoomFactor = prevZoomFactor * initialDistance/distance;
        if (newZoomFactor <= 1) {
            // set zoom, but consider the zoom factor where the last gesture ended
            setZoomFactor(newZoomFactor);
        }
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }
}
