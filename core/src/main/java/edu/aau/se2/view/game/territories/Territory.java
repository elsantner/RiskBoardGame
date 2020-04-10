package edu.aau.se2.view.game.territories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Territory {
    private static Territory[] territories = new Territory[42];
    private static boolean initialized = false;
    // These values give the screen resolution when the coordinates of the territories where recorded
    private static final int referenceWidth = 2392, referenceHeight = 1440;

    public static boolean isInitialized() {
        return initialized;
    }

    public static void init(int screenWidth, int screenHeight) {
        initTerritories(screenWidth, screenHeight);
    }

    /**
     * Creates all 42 territories and sets up their coordinates relative to current screen resolution.
     * @param screenWidth Current screen width
     * @param screenHeight Current screen height
     */
    private static void initTerritories(int screenWidth, int screenHeight) {
        territories[0] = new Territory(TerritoryID.Argentina, "Argentinien", createArray(screenWidth, screenHeight, 754,146, 726,191, 828,318, 706,371, 672,204, 686,156), null, null);
        territories[1] = new Territory(TerritoryID.Brazil, "Brasilien", createArray(screenWidth, screenHeight, 834,326,813,348,795,426,716,482,742,554,779,581,850,593,993,510,932,400), null, null);
        territories[2] = new Territory(TerritoryID.Peru, "Peru", createArray(screenWidth, screenHeight, 804,357,793,423,717,477,718,515,625,537,695,433,706,381), null, null);
        territories[3] = new Territory(TerritoryID.Venezuela, "Venezuela", createArray(screenWidth, screenHeight, 625,541,730,522,726,551,751,554,753,588,775,596,791,573,837,596,684,638), null, null);
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

    private int id;
    private String name;
    private Array<Vector2> polygonHitbox;
    private Array<Territory> adjacent;
    private Vector2 armyPosition;

    private Territory(int id, String name, Array<Vector2> hitbox, Array<Territory> adjacentTerritories, Vector2 armyPosition) {
        this.id = id;
        this.polygonHitbox = hitbox;
        this.adjacent = adjacentTerritories;
        this.name = name;
        this.armyPosition = armyPosition;
    }

    public int getID() {
        return id;
    }

    public Array<Vector2> getPolygonHitbox() {
        return polygonHitbox;
    }

    public String getName() {
        return name;
    }

    public Vector2 getArmyPosition() {
        return armyPosition;
    }
}
