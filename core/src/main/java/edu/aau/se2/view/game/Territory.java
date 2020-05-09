package edu.aau.se2.view.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

import edu.aau.se2.view.asset.AssetName;
import edu.aau.se2.server.logic.TerritoryHelper;

/**
 * @author Elias
 */
public class Territory extends Actor {
    private static Territory[] territories = new Territory[42];
    private static boolean initialized = false;
    // These values give the screen resolution when the coordinates of the territories where recorded
    public static final int REFERENCE_WIDTH = 2392;
    public static final int REFERENCE_HEIGHT = 1440;
    private static final String ERR_MSG_NOT_INIT = "territories not initialized";

    public static boolean isNotInitialized() {
        return !initialized;
    }

    public static void dispose() {
        initialized = false;
        territories = new Territory[42];
    }

    public static void init(int screenWidth, int screenHeight, AssetManager assetManager) {
        initTerritories(screenWidth, screenHeight, assetManager);
        initialized = true;
    }

    /**
     * Creates all 42 territories and sets up their coordinates relative to current screen resolution.
     * @param screenWidth Current screen width
     * @param screenHeight Current screen height
     */
    private static void initTerritories(int screenWidth, int screenHeight, AssetManager assetManager) {
        territories[0] = new Territory(TerritoryHelper.ID.ARGENTINA, "Argentinien", createArray(screenWidth, screenHeight, 754,146, 726,191, 828,318, 706,371, 672,204, 686,156), scale(screenWidth, screenHeight, new Vector2(737,300)), assetManager);
        territories[1] = new Territory(TerritoryHelper.ID.BRAZIL, "Brasilien", createArray(screenWidth, screenHeight, 834,326,813,348,795,426,716,482,742,554,779,581,850,593,993,510,932,400), scale(screenWidth, screenHeight, new Vector2(852,485)), assetManager);
        territories[2] = new Territory(TerritoryHelper.ID.PERU, "Peru", createArray(screenWidth, screenHeight, 804,357,793,423,717,477,718,515,625,537,695,433,706,381), scale(screenWidth, screenHeight, new Vector2(734,419)), assetManager);
        territories[3] = new Territory(TerritoryHelper.ID.VENEZUELA, "Venezuela", createArray(screenWidth, screenHeight, 625,541,730,522,726,551,751,554,753,588,775,596,791,573,837,596,684,638), scale(screenWidth, screenHeight, new Vector2(696,590)), assetManager);
        territories[4] = new Territory(TerritoryHelper.ID.ALASKA, "Alaska", createArray(screenWidth, screenHeight, 313,963,317,990,254,1033,233,1035,233,1118,110,1128,91,1063,124,989,71,947,89,925,212,1020), scale(screenWidth, screenHeight, new Vector2(170,1063)), assetManager);
        territories[5] = new Territory(TerritoryHelper.ID.WESTERN_CANADA, "Alberta", createArray(screenWidth, screenHeight, 314,965,319,989,299,1009,281,1012,257,1033,486,1027,484,908,370,905), scale(screenWidth, screenHeight, new Vector2(406,967)), assetManager);
        territories[6] = new Territory(TerritoryHelper.ID.CENTRAL_AMERICA, "Zentralamerika", createArray(screenWidth, screenHeight, 405,778,452,715,510,673,559,666,637,612,659,631,602,710,583,709,554,690,539,731,549,758), scale(screenWidth, screenHeight, new Vector2(513,714)), assetManager);
        territories[7] = new Territory(TerritoryHelper.ID.EASTERN_US, "Ost-USA", createArray(screenWidth, screenHeight, 559,760,529,770,545,828,547,907,583,901,635,859,688,886,726,894,728,867,640,777,649,730,632,726,610,764), scale(screenWidth, screenHeight, new Vector2(593,810)), assetManager);
        territories[8] = new Territory(TerritoryHelper.ID.GREENLAND, "Grönland", createArray(screenWidth, screenHeight, 841,1023,804,1149,698,1195,768,1288,1005,1297,1000,1262,1066,1279,1032,1169,974,1148,1005,1119,999,1094,925,1073,874,1009), scale(screenWidth, screenHeight, new Vector2(894,1185)), assetManager);
        territories[9] = new Territory(TerritoryHelper.ID.NORTHWEST_TERRITORY, "Nordwest Territorium", createArray(screenWidth, screenHeight, 235,1118,236,1038,571,1030,642,1090,629,1123,593,1094,570,1171,539,1164,535,1103,357,1126), scale(screenWidth, screenHeight, new Vector2(379,1069)), assetManager);
        territories[10] = new Territory(TerritoryHelper.ID.CENTRAL_CANADA, "Ontario", createArray(screenWidth, screenHeight, 563,1024,488,1027,488,908,543,909,563,920,643,868,684,887,646,890,643,918,614,966), scale(screenWidth, screenHeight, new Vector2(535,949)), assetManager);
        territories[11] = new Territory(TerritoryHelper.ID.EASTERN_CANADA, "Quebec", createArray(screenWidth, screenHeight, 649,920,659,1034,726,996,742,1020,798,954,816,906,734,864,729,900,651,892), scale(screenWidth, screenHeight, new Vector2(709,943)), assetManager);
        territories[12] = new Territory(TerritoryHelper.ID.WESTERN_US, "West-USA", createArray(screenWidth, screenHeight, 405,782,370,904,546,904,546,831,528,818,527,770,432,773), scale(screenWidth, screenHeight, new Vector2(464,836)), assetManager);
        territories[13] = new Territory(TerritoryHelper.ID.CENTRAL_AFRICA, "Zentralafrika", createArray(screenWidth, screenHeight, 1195,562,1215,598,1317,629,1320,587,1377,576,1358,558,1354,495,1301,479,1259,511,1215,527), scale(screenWidth, screenHeight, new Vector2(1290,554)), assetManager);
        territories[14] = new Territory(TerritoryHelper.ID.EAST_AFRICA, "Ostafrika", createArray(screenWidth, screenHeight, 1403,447,1365,455,1354,496,1361,559,1381,576,1323,590,1315,654,1343,696,1376,713,1421,639,1478,637,1398,530,1415,459), scale(screenWidth, screenHeight, new Vector2(1385,615)), assetManager);
        territories[15] = new Territory(TerritoryHelper.ID.EGYPT, "Ägypten", createArray(screenWidth, screenHeight, 1375,715,1342,699,1329,682,1310,711,1263,706,1220,744,1231,801,1268,769,1293,783,1364,772), scale(screenWidth, screenHeight, new Vector2(1309,740)), assetManager);
        territories[16] = new Territory(TerritoryHelper.ID.MADAGASCAR, "Madagaskar", createArray(screenWidth, screenHeight, 1473,510,1478,451,1461,404,1429,392,1410,413,1429,473), scale(screenWidth, screenHeight, new Vector2(1451,441)), assetManager);
        territories[17] = new Territory(TerritoryHelper.ID.NORTH_AFRICA, "Nordafrika", createArray(screenWidth, screenHeight, 1187,586,1098,575,1035,669,1111,805,1136,810,1231,799,1219,742,1262,703,1310,703,1327,672,1314,630), scale(screenWidth, screenHeight, new Vector2(1163,685)), assetManager);
        territories[18] = new Territory(TerritoryHelper.ID.SOUTH_AFRICA, "Südafrika", createArray(screenWidth, screenHeight, 1218,527,1299,474,1353,492,1384,431,1352,340,1256,315,1224,399), scale(screenWidth, screenHeight, new Vector2(1302,411)), assetManager);
        territories[19] = new Territory(TerritoryHelper.ID.GREAT_BRITAIN, "Großbritannien", createArray(screenWidth, screenHeight, 1105,1013,1127,1009,1147,954,1127,926,1090,916,1051,929,1061,965), scale(screenWidth, screenHeight, new Vector2(1103,949)), assetManager);
        territories[20] = new Territory(TerritoryHelper.ID.ICELAND, "Island", createArray(screenWidth, screenHeight, 1078,1110,1157,1115,1155,1056,1066,1052), scale(screenWidth, screenHeight, new Vector2(1111,1087)), assetManager);
        territories[21] = new Territory(TerritoryHelper.ID.NORTHERN_EUROPE, "Nordeuropa", createArray(screenWidth, screenHeight, 1180,932,1199,983,1218,981,1214,957,1309,958,1337,919,1319,897,1282,892,1276,907,1211,911), scale(screenWidth, screenHeight, new Vector2(1269,931)), assetManager);
        territories[22] = new Territory(TerritoryHelper.ID.SCANDINAVIA, "Skandinavien", createArray(screenWidth, screenHeight, 1224,999,1190,990,1184,1030,1280,1114,1320,1129,1351,1109,1349,1068,1320,1013,1237,963,1223,999), scale(screenWidth, screenHeight, new Vector2(1249,1042)), assetManager);
        territories[23] = new Territory(TerritoryHelper.ID.SOUTHERN_EUROPE, "Südeuropa", createArray(screenWidth, screenHeight, 1211,865,1213,909,1279,894,1317,896,1326,910,1352,919,1352,887,1317,842,1295,815,1273,832,1236,801,1220,808,1234,845), scale(screenWidth, screenHeight, new Vector2(1293,865)), assetManager);
        territories[24] = new Territory(TerritoryHelper.ID.UKRAINE, "Ukraine", createArray(screenWidth, screenHeight, 1337,924,1286,964,1351,1113,1565,1115,1547,1053,1564,1000,1553,932,1428,919,1458,843,1453,825,1353,884), scale(screenWidth, screenHeight, new Vector2(1414,972)), assetManager);
        territories[25] = new Territory(TerritoryHelper.ID.WESTERN_EUROPE, "Westeuropa", createArray(screenWidth, screenHeight, 1212,865,1134,813,1093,810,1087,854,1108,867,1149,877,1115,906,1179,929,1213,911), scale(screenWidth, screenHeight, new Vector2(1175,881)), assetManager);
        territories[26] = new Territory(TerritoryHelper.ID.EASTERN_AUSTRALIA, "Ost-Australien", createArray(screenWidth, screenHeight, 1999,432,1998,455,2050,456,2069,403,2094,464,2125,395,2178,346,2176,293,2119,230,2076,232,2075,356,1998,355), scale(screenWidth, screenHeight, new Vector2(2116,333)), assetManager);
        territories[27] = new Territory(TerritoryHelper.ID.INDONESIA, "Indonesien", createArray(screenWidth, screenHeight, 1931,396,1855,379,1731,483,1748,503,1826,484,1881,569,1920,552,1907,499,1966,501), scale(screenWidth, screenHeight, new Vector2(1859,454)), assetManager);
        territories[28] = new Territory(TerritoryHelper.ID.NEW_GUINEA, "Neuguinea", createArray(screenWidth, screenHeight, 1990,542,2000,564,2099,532,2145,463,2142,440,2107,468,2045,474,2044,504,2007,514), scale(screenWidth, screenHeight, new Vector2(2069,503)), assetManager);
        territories[29] = new Territory(TerritoryHelper.ID.WESTERN_AUSTRALIA, "West-Australien", createArray(screenWidth, screenHeight, 2072,236,2057,231,2041,280,2004,305,1909,258,1880,358,1902,379,1942,389,1956,435,2001,429,2002,352,2076,355), scale(screenWidth, screenHeight, new Vector2(1956,338)), assetManager);
        territories[30] = new Territory(TerritoryHelper.ID.AFGHANISTAN, "Afghanistan", createArray(screenWidth, screenHeight, 1440,871,1430,918,1569,935,1648,859,1632,858,1621,789,1608,785,1596,804,1550,776,1519,814,1479,821,1457,891), scale(screenWidth, screenHeight, new Vector2(1544,861)), assetManager);
        territories[31] = new Territory(TerritoryHelper.ID.CHINA, "China", createArray(screenWidth, screenHeight, 1829,702,1815,725,1783,712,1766,744,1694,749,1665,774,1651,817,1629,822,1631,856,1712,925,1737,917,1739,879,1890,791,1893,743), scale(screenWidth, screenHeight, new Vector2(1745,801)), assetManager);
        territories[32] = new Territory(TerritoryHelper.ID.INDIA, "India", createArray(screenWidth, screenHeight, 1565,721,1546,774,1599,802,1608,788,1619,789,1630,825,1652,818,1665,778,1694,750,1730,745,1701,708,1653,667,1658,601,1637,599), scale(screenWidth, screenHeight, new Vector2(1631,727)), assetManager);
        territories[33] = new Territory(TerritoryHelper.ID.IRKUTSK, "Irkutsk", createArray(screenWidth, screenHeight, 1764,937,1750,984,1875,995,1948,1055,1980,1045,1950,1000,1961,903,1946,902,1932,947,1889,931,1895,903), scale(screenWidth, screenHeight, new Vector2(1845,953)), assetManager);
        territories[34] = new Territory(TerritoryHelper.ID.JAPAN, "Japan", createArray(screenWidth, screenHeight, 2067,912,2124,895,2104,801,2034,676,1982,702,2039,781), scale(screenWidth, screenHeight, new Vector2(2075,809)), assetManager);
        territories[35] = new Territory(TerritoryHelper.ID.KAMCHATKA, "Kamchatka", createArray(screenWidth, screenHeight, 1976,865,1945,870,1962,926,1948,1003,1992,1056,2064,1051,2068,1084,2045,1100,2078,1124,2110,1134,2278,1103,2245,1031,2180,998,2156,1009,2128,986,2138,953,2098,921,2088,972,2059,998,2018,999,1977,964,2006,933), scale(screenWidth, screenHeight, new Vector2(2137,1056)), assetManager);
        territories[36] = new Territory(TerritoryHelper.ID.MIDDLE_EAST, "Mittlerer Osten", createArray(screenWidth, screenHeight, 1481,820,1522,814,1567,725,1422,644,1365,769,1375,813,1313,819,1312,837,1322,846,1365,857), scale(screenWidth, screenHeight, new Vector2(1421,787)), assetManager);
        territories[37] = new Territory(TerritoryHelper.ID.MONGOLIA, "Mongolei", createArray(screenWidth, screenHeight, 1881,795,1750,872,1758,936,1896,904,1934,951,1952,898,1945,890,1956,862,1926,832,1945,812,1922,792,1889,935), scale(screenWidth, screenHeight, new Vector2(1842,868)), assetManager);
        territories[38] = new Territory(TerritoryHelper.ID.SOUTHEAST_ASIA, "Südost Asien", createArray(screenWidth, screenHeight, 1699,708,1742,750,1767,746,1781,711,1814,730,1829,701,1814,633,1769,584,1735,625), scale(screenWidth, screenHeight, new Vector2(1762,687)), assetManager);
        territories[39] = new Territory(TerritoryHelper.ID.SIBERIA, "Sibirien", createArray(screenWidth, screenHeight, 1635,1082,1598,1119,1624,1149,1772,1228,1847,1200,1898,1152,1906,1126,1851,1071,1885,1067,1865,995,1751,981,1759,934,1751,876,1724,927), scale(screenWidth, screenHeight, new Vector2(1763,1085)), assetManager);
        territories[40] = new Territory(TerritoryHelper.ID.URAL, "Ural", createArray(screenWidth, screenHeight, 1554,935,1558,1003,1546,1058,1571,1102,1557,1117,1581,1158,1594,1150,1594,1084,1635,1082,1725,925,1651,863), scale(screenWidth, screenHeight, new Vector2(1632,971)), assetManager);
        territories[41] = new Territory(TerritoryHelper.ID.YAKUTSK, "Jakutsk", createArray(screenWidth, screenHeight, 1898,1162,2077,1127,2044,1105,2073,1069,2052,1051,1981,1046,1948,1057,1866,995,1884,1069,1849,1070,1856,1087,1887,1099), scale(screenWidth, screenHeight, new Vector2(1959,1093)), assetManager);
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
            array.add(scale(screenWidth, screenHeight, new Vector2(coords[i], coords[i+1])));
        }
        return array;
    }

    /**
     * Scales the given coordinates to the current screen resolution.
     * @param screenWidth Current screen width
     * @param screenHeight Current screen height
     * @param coords Coordinates to scale
     * @return Mapped 2d vector
     */
    private static Vector2 scale(int screenWidth, int screenHeight, Vector2 coords) {
        coords.x *= (float)screenWidth/(float) REFERENCE_WIDTH;
        coords.y *= (float)screenHeight/(float) REFERENCE_HEIGHT;
        return coords;
    }

    public static Territory getByID(int id) {
        if (isNotInitialized()) {
            throw new IllegalStateException(ERR_MSG_NOT_INIT);
        }
        try {
            return territories[id];
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
        if (isNotInitialized()) {
            throw new IllegalStateException(ERR_MSG_NOT_INIT);
        }
        for (Territory t: territories) {
            if (Intersector.isPointInPolygon(t.getPolygonHitbox(), new Vector2(x, y))) {
                return t;
            }
        }
        return null;
    }

    public static Territory[] getAll() {
        if (isNotInitialized()) {
            throw new IllegalStateException(ERR_MSG_NOT_INIT);
        }
        return territories;
    }

    private int id;
    private String name;
    private Array<Vector2> polygonHitbox;
    private Vector2 armyPosition;
    private Color armyColor;
    private int armyCount;
    private BitmapFont font;
    private Texture armyCirlce;
    private boolean isHighlighted;

    private Territory(int id, String name, Array<Vector2> hitbox, Vector2 armyPosition, AssetManager assetManager) {
        this.id = id;
        this.polygonHitbox = hitbox;
        this.name = name;
        this.armyPosition = armyPosition;
        this.armyColor = Color.CLEAR;
        this.armyCount = -1;
        this.isHighlighted = false;
        getAssets(assetManager);
    }

    private void getAssets(AssetManager assetManager) {
        this.font = assetManager.get(AssetName.FONT_3);
        this.armyCirlce = assetManager.get(AssetName.ARMY_DISPLAY_CIRCLE);
    }

    public int getID() {
        return id;
    }

    private Array<Vector2> getPolygonHitbox() {
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

            // if highlighted draw white border
            if (isHighlighted) {
                batch.setColor(Color.WHITE);
                batch.draw(armyCirlce, armyPosition.x - (32 * getScaleX()), armyPosition.y - (32 * getScaleX()),
                        64*getScaleX(), 64*getScaleX());
            }
            batch.setColor(armyColor);
            batch.draw(armyCirlce, armyPosition.x - (24 * getScaleX()), armyPosition.y - (24 * getScaleX()),
                    48*getScaleX(), 48*getScaleX());
            // center text in armyCircle (NOTE: These constants are dependent on the text and texture size)
            int xOffsetText = (int) (5 * getScaleX());
            if (armyCount > 9) {
                xOffsetText = (int) (12 * getScaleX());
            }
            font.getData().setScale(getScaleX());
            font.draw(batch, Integer.toString(armyCount), armyPosition.x - xOffsetText, armyPosition.y + (7*getScaleX()));
        }
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }
}
