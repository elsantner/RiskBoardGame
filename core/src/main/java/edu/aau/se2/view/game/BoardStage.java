package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.Viewport;

import edu.aau.se2.view.asset.AssetName;
import edu.aau.se2.view.game.territories.Territory;
import edu.aau.se2.view.game.territories.TerritoryID;

/**
 * @author Elias
 */
public class BoardStage extends Stage implements GestureDetector.GestureListener {
    private static float MAX_ZOOM_FACTOR = 1, MIN_ZOOM_FACTOR = 0.25f;

    private Texture texRiskBoard;
    private Image imgRiskBoard;
    private OrthographicCamera cam;

    private float prevZoomFactor = 1;
    private long prevTapTime = 0;

    public BoardStage(Viewport vp) {
        super(vp);
        cam = (OrthographicCamera) this.getCamera();
        // init territories (relevant for scaling to current resolution)
        if (!Territory.isInitialized()) {
            Territory.init(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        loadAssets();
        setupBoardImage();
        setupTerritories();
    }

    private void setupTerritories() {
        for (Territory t: Territory.getAll()) {
            this.addActor(t);
        }
    }

    private void loadAssets() {
        texRiskBoard = new Texture(AssetName.RISK_BOARD);
        imgRiskBoard = new Image(texRiskBoard);
    }

    private void setupBoardImage() {
        imgRiskBoard.setWidth(Gdx.graphics.getWidth());
        imgRiskBoard.setHeight(Gdx.graphics.getHeight());

        this.addActor(imgRiskBoard);
    }

    public float getZoomFactor() {
        return cam.zoom;
    }

    public void setZoomFactor(float zoomFactor) {
        if (zoomFactor > 1 || zoomFactor < 0) {
            throw new IllegalArgumentException("zoomFactor must be between 0 and 1");
        }
        cam.zoom = zoomFactor;
    }

    /**
     * Moves the camera position by deltaX and deltaY while staying inside the bounds of the
     * camera viewport and considering zoom factor.
     * @param deltaX The change of the x position
     * @param deltaY The change of the x position
     */
    private void moveCameraWithinBoardBounds(float deltaX, float deltaY) {
        // zoom factor is incorporated to make (absolute) camera movement slower when zoomed in
        // so it's always the same visual velocity
        float newX = cam.position.x - deltaX*getZoomFactor();
        float newY = cam.position.y + deltaY*getZoomFactor();
        moveCameraToPosWithinBoardBounds(newX, cam.viewportHeight-newY);
    }

    /**
     * Moves the camera position as close to x and y as possible while staying inside the bounds of
     * the camera viewport and considering zoom factor.
     * @param x The x position
     * @param y The y position
     */
    private void moveCameraToPosWithinBoardBounds(float x, float y) {
        // the camera position is the center coordinate of the displayed area --> cam.viewport.../2
        cam.position.x = MathUtils.clamp(x,
                cam.viewportWidth/2 - (cam.viewportWidth/2)*(1-getZoomFactor()),
                cam.viewportWidth/2 + (cam.viewportWidth/2)*(1-getZoomFactor()));
        cam.position.y = MathUtils.clamp(cam.viewportHeight-y,
                cam.viewportHeight/2 - (cam.viewportHeight/2)*(1-getZoomFactor()),
                cam.viewportHeight/2 + (cam.viewportHeight/2)*(1-getZoomFactor()));
        cam.update();
    }

    /**
     * Zooms the camera if a double tap happened (2 taps within .5 second).
     * @return True if the zoom was performed, otherwise false
     */
    private boolean zoomOnDoubleTap(float x, float y) {
        if (System.currentTimeMillis() - prevTapTime < 500) {
            prevTapTime = 0;
            if (getZoomFactor() == MAX_ZOOM_FACTOR) {
                setZoomFactor(MIN_ZOOM_FACTOR);
            }
            else {
                setZoomFactor(MAX_ZOOM_FACTOR);
            }
            moveCameraToPosWithinBoardBounds(x, y);
            return true;
        }
        else {
            prevTapTime = System.currentTimeMillis();
            return false;
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        // every time the player touches the screen, remember current zoom factor
        prevZoomFactor = getZoomFactor();
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        zoomOnDoubleTap(x, y);

        Vector3 inWorldPos = cam.unproject(new Vector3(x, y, 0));
        System.out.println((int)inWorldPos.x + "," + (int)inWorldPos.y + ",");
        Territory t = Territory.getByPosition(inWorldPos.x, inWorldPos.y);
        if (t != null) {
            t.setArmyCount(t.getArmyCount()+1);
        }
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
        moveCameraWithinBoardBounds(deltaX, deltaY);
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        float newZoomFactor = prevZoomFactor * initialDistance/distance;
        if (newZoomFactor <= MAX_ZOOM_FACTOR && newZoomFactor >= MIN_ZOOM_FACTOR) {
            // set zoom, but consider the zoom factor where the last gesture ended
            setZoomFactor(newZoomFactor);
            moveCameraWithinBoardBounds(0, 0);
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
