package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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

/**
 * @author Elias
 */
public class BoardStage extends Stage implements IGameBoard, GestureDetector.GestureListener {
    private static float MAX_ZOOM_FACTOR = 1, MIN_ZOOM_FACTOR = 0.25f;
    private float prevZoomFactor = 1;

    private Image imgRiskBoard;
    private OrthographicCamera cam;
    private OnBoardInteractionListener boardListener;
    private boolean interactable = true, armiesPlacable = false, attackAllowed = false;

    public BoardStage(Viewport vp) {
        super(vp);
        cam = (OrthographicCamera) this.getCamera();
        // init territories (relevant for scaling to current resolution)
        if (Territory.isNotInitialized()) {
            Territory.init(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        loadAssets();
        setupBoardImage();
        setupTerritories();
    }

    public void setListener(OnBoardInteractionListener l) {
        this.boardListener = l;
    }

    private void setupTerritories() {
        for (Territory t: Territory.getAll()) {
            this.addActor(t);
        }
    }

    private void loadAssets() {
        imgRiskBoard = new Image(new Texture(AssetName.RISK_BOARD));
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
        if (zoomFactor > MAX_ZOOM_FACTOR || zoomFactor < MIN_ZOOM_FACTOR) {
            throw new IllegalArgumentException("zoomFactor must be between " + MIN_ZOOM_FACTOR + " and " + MAX_ZOOM_FACTOR);
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
     * Sets the camera zoom to its maximum (centered around (x,y)) if the current zoom factor is not at a minimum.
     * Otherwise set the camera zoom to its minimum.
     */
    private void toggleZoom(float x, float y) {
        if (getZoomFactor() == MAX_ZOOM_FACTOR) {
            setZoomFactor(MIN_ZOOM_FACTOR);
        }
        else {
            setZoomFactor(MAX_ZOOM_FACTOR);
        }
        moveCameraToPosWithinBoardBounds(x, y);
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        // every time the player touches the screen, remember current zoom factor
        prevZoomFactor = getZoomFactor();
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        // place 1 army if
        if (interactable) {
            Vector3 inWorldPos = cam.unproject(new Vector3(x, y, 0));
            Territory t = Territory.getByPosition(inWorldPos.x, inWorldPos.y);
            if (t != null && armiesPlacable && boardListener != null) {
                boardListener.armyPlaced(t.getID(), 1);
            } else if (count == 2) {
                toggleZoom(x, y);
            }
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
        if (interactable) {
            float newZoomFactor = prevZoomFactor * initialDistance / distance;
            if (newZoomFactor <= MAX_ZOOM_FACTOR && newZoomFactor >= MIN_ZOOM_FACTOR) {
                // set zoom, but consider the zoom factor where the last gesture ended
                setZoomFactor(newZoomFactor);
                moveCameraWithinBoardBounds(0, 0);
            }
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

    @Override
    public void dispose() {
        super.dispose();
        imgRiskBoard = null;
    }

    @Override
    public void setInteractable(boolean interactable) {
        this.interactable = interactable;
    }

    @Override
    public boolean isInteractable() {
        return interactable;
    }

    @Override
    public void setArmiesPlacable(boolean armiesPlacable) {
        this.armiesPlacable = armiesPlacable;
    }

    @Override
    public boolean isArmiesPlacable() {
        return armiesPlacable;
    }

    @Override
    public void setAttackAllowed(boolean attackAllowed) {
        this.attackAllowed = attackAllowed;
    }

    @Override
    public boolean isAttackAllowed() {
        return attackAllowed;
    }

    @Override
    public void setArmyCount(int territoryID, int count) {
        Territory.getByID(territoryID).setArmyCount(count);
    }

    @Override
    public void setArmyColor(int territoryID, Color color) {
        Territory.getByID(territoryID).setArmyColor(color);
    }
}
