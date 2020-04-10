package edu.aau.se2.view.game.territories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class Territory extends Actor {
    private static Territory[] territories = new Territory[42];
    private static boolean initialized = false;
    // These values give the screen resolution when the coordinates of the territories where recorded
    private static final int referenceWidth = 2392, referenceHeight = 1440;

    public static boolean isInitialized() {
        return initialized;
    }

    public static void init(int screenWidth, int screenHeight) {
        initTerritories(screenWidth, screenHeight);
        initialized = true;
    }

    /**
     * Creates all 42 territories and sets up their coordinates relative to current screen resolution.
     * @param screenWidth Current screen width
     * @param screenHeight Current screen height
     */
    private static void initTerritories(int screenWidth, int screenHeight) {
        territories[0] = new Territory(TerritoryID.Argentina, "Argentinien", createArray(screenWidth, screenHeight, 754,146, 726,191, 828,318, 706,371, 672,204, 686,156), null, new Vector2(737,300));
        territories[1] = new Territory(TerritoryID.Brazil, "Brasilien", createArray(screenWidth, screenHeight, 834,326,813,348,795,426,716,482,742,554,779,581,850,593,993,510,932,400), null, new Vector2(852,485));
        territories[2] = new Territory(TerritoryID.Peru, "Peru", createArray(screenWidth, screenHeight, 804,357,793,423,717,477,718,515,625,537,695,433,706,381), null, new Vector2(734,419));
        territories[3] = new Territory(TerritoryID.Venezuela, "Venezuela", createArray(screenWidth, screenHeight, 625,541,730,522,726,551,751,554,753,588,775,596,791,573,837,596,684,638), null, new Vector2(696,590));
        //territories[4] = new Territory(TerritoryID.Alaska, "Alaska", createArray(screenWidth, screenHeight, 313,963,317,990,254,1033,233,1035,233,1118,110,1128,91,1063,124,989,71,947,89,925,212,1020), null, null);
    }

    /**
     * Transforms the given coordinates into an array of 2d vectors and maps them to the current
     * screen resolution.
     * @param screenWidth Current screen width
     * @param screenHeight Current screen height
     * @param coords Coordinates given as pairs (x1, y1, x2, y2, ...)
     * @return Array of mapped 2d vectors
     */
    private static Array<Vector2> createArray(int screenWidth, int screenHeight, float... coords) {
        if (coords.length % 2 != 0) {
            throw new IllegalArgumentException("must have even number of ordinates");
        }
        Array<Vector2> array = new Array<>();
        for (int i=0; i<coords.length; i+=2) {
            array.add(new Vector2(coords[i]*((float) screenWidth/(float) referenceWidth),
                    coords[i+1] * ((float) screenHeight/(float) referenceHeight)));
        }
        return array;
    }

    public static Territory getByID(int id) {
        try {
            return territories[id - 1];
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("territory with id " + id + " does not exist");
        }
    }

    /**
     * Finds the territory which contains the point (x,y).
     * @param x x value of point
     * @param y y value of point
     * @return The territory which contains the point (x,y), or null if no territory contains (x,y).
     */
    public static Territory getByPosition(float x, float y) {
        for (Territory t: territories) {
            if (Intersector.isPointInPolygon(t.getPolygonHitbox(), new Vector2(x, y))) {
                return t;
            }
        }
        return null;
    }

    private int id;
    private String name;
    private Array<Vector2> polygonHitbox;
    private Array<Territory> adjacent;
    private Vector2 armyPosition;
    private Color armyColor;
    private int armyCount;
    private BitmapFont font;
    private Texture armyCirlce;

    private Territory(int id, String name, Array<Vector2> hitbox, Array<Territory> adjacentTerritories, Vector2 armyPosition) {
        this.id = id;
        this.polygonHitbox = hitbox;
        this.adjacent = adjacentTerritories;
        this.name = name;
        this.armyPosition = armyPosition;
        this.armyColor = null;
        this.armyCount = 0;
        this.font = new BitmapFont();
        this.armyCirlce = new Texture("armyCircle.png");
    }

    public int getID() {
        return id;
    }

    public Array<Vector2> getPolygonHitbox() {
        return polygonHitbox;
    }

    public String getTerritoryName() {
        return name;
    }

    public Vector2 getArmyPosition() {
        return armyPosition;
    }

    public Color getArmyColor() {
        return armyColor;
    }

    public void setArmyColor(Color armyColor) {
        this.armyColor = armyColor;
    }

    public int getArmyCount() {
        return armyCount;
    }

    public void setArmyCount(int armyCount) {
        this.armyCount = armyCount;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // draw army counter
        if (armyCount > 0 && armyCirlce != null) {
            batch.setColor(armyColor);
            batch.draw(armyCirlce, armyPosition.x - 16, armyPosition.y - 16);
            font.draw(batch, Integer.toString(armyCount), armyPosition.x - 4, armyPosition.y + 4);
        }
    }
}
