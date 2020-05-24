package edu.aau.se2.view.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.Viewport;

import edu.aau.se2.model.Database;
import edu.aau.se2.server.logic.TerritoryHelper;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;
import edu.aau.se2.view.asset.AssetName;

/**
 * @author Elias
 */
public class BoardStage extends AbstractStage implements IGameBoard, GestureDetector.GestureListener {
    private static final float MAX_ZOOM_FACTOR = 1;
    private static final float MIN_ZOOM_FACTOR = 0.25f;
    private float prevZoomFactor = 1;

    private Database db;
    private Image imgRiskBoard;
    private OrthographicCamera cam;
    private OnBoardInteractionListener boardListener;
    private boolean interactable = true;
    private boolean attackStartable = true;
    private Color[] playerColors;
    private Database.Phase phase;
    private Territory selectedTerritory;

    public BoardStage(AbstractScreen screen, Viewport vp) {
        this(screen, vp, new Color[]{Color.BLACK, Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED, Color.ORANGE});
    }

    public BoardStage(AbstractScreen screen, Viewport vp, Color[] playerColors) {
        super(vp, screen);
        if (playerColors.length != 6) {
            throw new IllegalArgumentException("player colors must contain exactly 6 colors");
        }
        this.db = Database.getInstance();
        this.playerColors = playerColors;
        cam = (OrthographicCamera) this.getCamera();
        // init territories (relevant for scaling to current resolution)
        if (Territory.isNotInitialized()) {
            Territory.init(vp.getScreenWidth(), vp.getScreenHeight(), getScreen().getGame().getAssetManager());
        }
        loadAssets();
        setupBoardImage(vp.getScreenWidth(), vp.getScreenHeight());
        setupTerritories();
        this.phase = Database.Phase.NONE;
    }

    public void setListener(OnBoardInteractionListener l) {
        this.boardListener = l;
    }

    private void setupTerritories() {
        for (Territory t: Territory.getAll()) {
            t.setScale(getViewport().getWorldWidth() / Territory.REFERENCE_WIDTH);
            this.addActor(t);
        }
    }

    private void loadAssets() {
        imgRiskBoard = new Image(getScreen().getGame().getAssetManager().get(AssetName.RISK_BOARD, Texture.class));
    }

    private void setupBoardImage(int screenWidth, int screenHeight) {
        imgRiskBoard.setWidth(screenWidth);
        imgRiskBoard.setHeight(screenHeight);
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
        if (interactable) {
            Vector3 inWorldPos = cam.unproject(new Vector3(x, y, 0));
            Territory t = Territory.getByPosition(inWorldPos.x, inWorldPos.y);

            if (t != null && boardListener != null && db.isThisPlayersTurn()) {
                // place army if in game setup or army placing phase
                if (phase == Database.Phase.NONE || phase == Database.Phase.PLACING) {
                    boardListener.armyPlaced(t.getID(), 1);
                }
                // if in moving phase ...
                else if (phase == Database.Phase.MOVING) {
                    handleMoveArmies(t);
                }
                // if in attacking phase ...
                else if (phase == Database.Phase.ATTACKING && attackStartable) {
                    handleAttack(t);
                }
            }
            else if (t == null && selectedTerritory != null && count == 1) {
                clearTerritorySelection();
            }

            else if (count == 2) {
                toggleZoom(x, y);
            }
        }

        return false;
    }

    private void handleAttack(Territory t) {
        // remember first clicked territory
        edu.aau.se2.server.data.Territory clickedTerritory = db.getLobby().getTerritoryByID(t.getID());
        if (selectedTerritory == null) {
            if (clickedTerritory.getOccupierPlayerID() == db.getThisPlayer().getUid() &&
                    clickedTerritory.getArmyCount() > 1) {
                selectedTerritory = t;
                if (highlightAttackableTerritories(t) == 0) {
                    clearTerritorySelection();
                }
            }
        }
        // move to second clicked territory if it is a neighbour of first clicked
        else if (TerritoryHelper.areNeighbouring(selectedTerritory.getID(), t.getID()) &&
                !clickedTerritory.isNotOccupied() && clickedTerritory.getOccupierPlayerID() != db.getThisPlayer().getUid()) {

            boardListener.attackStarted(selectedTerritory.getID(), t.getID(), -1);
            clearTerritorySelection();
        }
    }

    private int highlightAttackableTerritories(Territory t) {
        int highlightedCount = 0;
        for (int territoryID: TerritoryHelper.getNeighbouringTerritories(t.getID())) {
            Territory neighbour = Territory.getByID(territoryID);
            if (!db.getLobby().getTerritoryByID(territoryID).isNotOccupied() &&
                    db.getLobby().getTerritoryByID(territoryID).getOccupierPlayerID() != db.getThisPlayer().getUid()) {
                neighbour.setHighlighted(true);
                highlightedCount++;
            }
        }
        return highlightedCount;
    }

    private void handleMoveArmies(Territory t) {
        // remember first clicked territory
        edu.aau.se2.server.data.Territory clickedTerritory = db.getLobby().getTerritoryByID(t.getID());
        if (selectedTerritory == null) {
            if (clickedTerritory.getOccupierPlayerID() == db.getThisPlayer().getUid() &&
                    clickedTerritory.getArmyCount() > 1) {

                selectedTerritory = t;
                if (highlightMovableTerritories(t) == 0) {
                    clearTerritorySelection();
                }
            }
        }
        // move to second clicked territory if it is a neighbour of first clicked
        else if (TerritoryHelper.areNeighbouring(selectedTerritory.getID(), t.getID()) &&
                (clickedTerritory.getOccupierPlayerID() == db.getThisPlayer().getUid() ||
                        clickedTerritory.isNotOccupied())) {

            boardListener.armyMoved(selectedTerritory.getID(), t.getID(), -1);
            clearTerritorySelection();
        }
    }

    private void clearTerritorySelection() {
        clearTerritoryHighlights();
        selectedTerritory = null;
    }

    private int highlightMovableTerritories(Territory t) {
        int highlightedCount = 0;
        for (int territoryID: TerritoryHelper.getNeighbouringTerritories(t.getID())) {
            Territory neighbour = Territory.getByID(territoryID);
            if (db.getLobby().getTerritoryByID(territoryID).getOccupierPlayerID() == db.getThisPlayer().getUid() ||
                    db.getLobby().getTerritoryByID(territoryID).isNotOccupied()) {
                neighbour.setHighlighted(true);
                highlightedCount++;
            }
        }
        return highlightedCount;
    }

    private void clearTerritoryHighlights() {
        for (Territory t: Territory.getAll()) {
            t.setHighlighted(false);
        }
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
        if (interactable) {
            moveCameraWithinBoardBounds(deltaX, deltaY);
        }
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
        // currently unused but needed because of interface implementation
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
    public void setArmyCount(int territoryID, int count) {
        Territory.getByID(territoryID).setArmyCount(count);
    }

    @Override
    public void setArmyColor(int territoryID, int colorID) {
        Territory.getByID(territoryID).setArmyColor(playerColors[colorID]);
    }

    public void setPhase(Database.Phase newPhase) {
        this.phase = newPhase;
        clearTerritoryHighlights();
    }

    public void attackStartable(boolean attackStartable) {
        this.attackStartable = attackStartable;
    }
}
