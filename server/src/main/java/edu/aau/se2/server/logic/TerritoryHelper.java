package edu.aau.se2.server.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TerritoryHelper {
    public abstract static class ID {
        private ID() {
            // defeat instantiation
        }

        public static final int ARGENTINA = 0;
        public static final int BRAZIL = 1;
        public static final int PERU = 2;
        public static final int VENEZUELA = 3;
        public static final int ALASKA = 4;
        public static final int WESTERN_CANADA = 5;
        public static final int CENTRAL_AMERICA = 6;
        public static final int EASTERN_US = 7;
        public static final int GREENLAND = 8;
        public static final int NORTHWEST_TERRITORY = 9;
        public static final int CENTRAL_CANADA = 10;
        public static final int EASTERN_CANADA = 11;
        public static final int WESTERN_US = 12;
        public static final int CENTRAL_AFRICA = 13;
        public static final int EAST_AFRICA = 14;
        public static final int EGYPT = 15;
        public static final int MADAGASCAR = 16;
        public static final int NORTH_AFRICA = 17;
        public static final int SOUTH_AFRICA = 18;
        public static final int GREAT_BRITAIN = 19;
        public static final int ICELAND = 20;
        public static final int NORTHERN_EUROPE = 21;
        public static final int SCANDINAVIA = 22;
        public static final int SOUTHERN_EUROPE = 23;
        public static final int UKRAINE = 24;
        public static final int WESTERN_EUROPE = 25;
        public static final int EASTERN_AUSTRALIA = 26;
        public static final int INDONESIA = 27;
        public static final int NEW_GUINEA = 28;
        public static final int WESTERN_AUSTRALIA = 29;
        public static final int AFGHANISTAN = 30;
        public static final int CHINA = 31;
        public static final int INDIA = 32;
        public static final int IRKUTSK = 33;
        public static final int JAPAN = 34;
        public static final int KAMCHATKA = 35;
        public static final int MIDDLE_EAST = 36;
        public static final int MONGOLIA = 37;
        public static final int SOUTHEAST_ASIA = 38;
        public static final int SIBERIA = 39;
        public static final int URAL = 40;
        public static final int YAKUTSK = 41;
    }

    private static final Integer[][] neighbouringTerritories = new Integer[][] {
            {ID.BRAZIL, ID.PERU},   // Argentina
            {ID.PERU, ID.ARGENTINA, ID.VENEZUELA, ID.NORTH_AFRICA},  // Brazil
            {ID.BRAZIL, ID.ARGENTINA, ID.VENEZUELA},    // Peru
            {ID.PERU, ID.BRAZIL},   //Venezuela
            {ID.WESTERN_CANADA, ID.NORTHWEST_TERRITORY, ID.KAMCHATKA},  // Alaska
            {ID.ALASKA, ID.NORTHWEST_TERRITORY, ID.CENTRAL_CANADA, ID.WESTERN_US}, // WesternCanada
            {ID.WESTERN_US, ID.EASTERN_US, ID.VENEZUELA}, // CentralAmerica
            {ID.WESTERN_US, ID.CENTRAL_AMERICA, ID.CENTRAL_CANADA, ID.EASTERN_CANADA}, // EasternUS
            {ID.NORTHWEST_TERRITORY, ID.CENTRAL_CANADA, ID.EASTERN_CANADA, ID.ICELAND}, // Greenland
            {ID.GREENLAND, ID.ALASKA, ID.WESTERN_CANADA, ID.CENTRAL_CANADA}, // NorthwestTerritory
            {ID.GREENLAND, ID.NORTHWEST_TERRITORY, ID.WESTERN_CANADA, ID.EASTERN_CANADA, ID.WESTERN_US, ID.EASTERN_US}, // CentralCanada
            {ID.GREENLAND, ID.CENTRAL_CANADA, ID.EASTERN_US}, // EasternCanada
            {ID.EASTERN_US, ID.WESTERN_CANADA, ID.CENTRAL_CANADA, ID.CENTRAL_AMERICA}, // WesternUS
            {ID.NORTH_AFRICA, ID.EAST_AFRICA, ID.SOUTH_AFRICA}, // CentralAfrica
            {ID.NORTH_AFRICA, ID.SOUTH_AFRICA, ID.CENTRAL_AFRICA, ID.EGYPT, ID.MADAGASCAR, ID.MIDDLE_EAST}, // EastAfrica
            {ID.NORTH_AFRICA, ID.EAST_AFRICA, ID.MIDDLE_EAST, ID.SOUTHERN_EUROPE}, // Egypt
            {ID.EAST_AFRICA, ID.SOUTH_AFRICA}, // Madagascar
            {ID.EAST_AFRICA, ID.CENTRAL_AFRICA, ID.EGYPT, ID.SOUTHERN_EUROPE, ID.WESTERN_EUROPE, ID.BRAZIL}, // NorthAfrica
            {ID.EAST_AFRICA, ID.CENTRAL_AFRICA, ID.MADAGASCAR}, // SouthAfrica
            {ID.WESTERN_EUROPE, ID.NORTHERN_EUROPE, ID.SCANDINAVIA, ID.ICELAND}, // GreatBritain
            {ID.GREENLAND, ID.GREAT_BRITAIN, ID.SCANDINAVIA}, // Iceland
            {ID.WESTERN_EUROPE, ID.SOUTHERN_EUROPE, ID.UKRAINE, ID.SCANDINAVIA, ID.GREAT_BRITAIN}, // NorthernEurope
            {ID.NORTHERN_EUROPE, ID.UKRAINE, ID.GREAT_BRITAIN, ID.ICELAND}, // Scandinavia
            {ID.WESTERN_EUROPE, ID.NORTHERN_EUROPE, ID.UKRAINE, ID.MIDDLE_EAST, ID.EGYPT, ID.NORTH_AFRICA}, // SouthernEurope
            {ID.SCANDINAVIA, ID.NORTHERN_EUROPE, ID.SOUTHERN_EUROPE, ID.MIDDLE_EAST, ID.AFGHANISTAN, ID.URAL}, // Ukraine
            {ID.NORTH_AFRICA, ID.GREAT_BRITAIN, ID.NORTHERN_EUROPE, ID.SOUTHERN_EUROPE}, // WesternEurope
            {ID.WESTERN_AUSTRALIA, ID.NEW_GUINEA}, // EasternAustralia
            {ID.WESTERN_AUSTRALIA, ID.NEW_GUINEA, ID.SOUTHEAST_ASIA}, // Indonesia
            {ID.WESTERN_AUSTRALIA, ID.EASTERN_AUSTRALIA, ID.INDONESIA}, // NewGuinea
            {ID.NEW_GUINEA, ID.EASTERN_AUSTRALIA, ID.INDONESIA}, // WesternAustralia
            {ID.UKRAINE, ID.URAL, ID.MIDDLE_EAST, ID.INDIA, ID.CHINA}, // Afghanistan
            {ID.SOUTHEAST_ASIA, ID.INDIA, ID.AFGHANISTAN, ID.URAL, ID.SIBERIA, ID.MONGOLIA}, // China
            {ID.MIDDLE_EAST, ID.AFGHANISTAN, ID.CHINA, ID.SOUTHEAST_ASIA}, // India
            {ID.MONGOLIA, ID.SIBERIA, ID.YAKUTSK, ID.KAMCHATKA}, // Irkutsk
            {ID.KAMCHATKA, ID.MONGOLIA}, // Japan
            {ID.ALASKA, ID.JAPAN, ID.MONGOLIA, ID.IRKUTSK, ID.YAKUTSK}, // Kamchatka
            {ID.EAST_AFRICA, ID.EGYPT, ID.SOUTHERN_EUROPE, ID.UKRAINE, ID.AFGHANISTAN, ID.INDIA}, // MiddleEast
            {ID.CHINA, ID.SIBERIA, ID.IRKUTSK, ID.KAMCHATKA, ID.JAPAN}, // Mongolia
            {ID.INDONESIA, ID.INDIA, ID.CHINA}, // SoutheastAsia
            {ID.URAL, ID.CHINA, ID.MONGOLIA, ID.IRKUTSK, ID.YAKUTSK}, // Siberia
            {ID.UKRAINE, ID.AFGHANISTAN, ID.CHINA, ID.SIBERIA}, // Ural
            {ID.SIBERIA, ID.IRKUTSK, ID.KAMCHATKA}, // Yakutsk
    };

    public static List<Integer> getNeighbouringTerritories(int territoryID) {
        try {
            return Arrays.asList(neighbouringTerritories[territoryID]);
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("territory with id " + territoryID + " does not exist");
        }
    }

    public static boolean areNeighbouring(int territoryID1, int territoryID2) {
        return getNeighbouringTerritories(territoryID1).contains(territoryID2);
    }

    private TerritoryHelper() {
        // defeat instantiation
    }

    private static final Integer[][] continentTerritories = new Integer[][] {
            {ID.ARGENTINA, ID.BRAZIL, ID.PERU, ID.VENEZUELA},
            {ID.ALASKA, ID.WESTERN_CANADA, ID.CENTRAL_AMERICA, ID.EASTERN_US, ID.GREENLAND, ID.NORTHWEST_TERRITORY, ID.CENTRAL_CANADA, ID.EASTERN_CANADA, ID.WESTERN_US},
            {ID.CENTRAL_AFRICA, ID.EAST_AFRICA, ID.EGYPT, ID.MADAGASCAR, ID.NORTH_AFRICA, ID.SOUTH_AFRICA},
            {ID.GREAT_BRITAIN, ID.ICELAND, ID.NORTHERN_EUROPE, ID.SCANDINAVIA, ID.SOUTHERN_EUROPE, ID.UKRAINE, ID.WESTERN_EUROPE},
            {ID.EASTERN_AUSTRALIA, ID.INDONESIA, ID.NEW_GUINEA, ID.WESTERN_AUSTRALIA},
            {ID.AFGHANISTAN, ID.CHINA, ID.INDIA, ID.IRKUTSK, ID.JAPAN, ID.KAMCHATKA, ID.MIDDLE_EAST, ID.MONGOLIA, ID.SOUTHEAST_ASIA, ID.SIBERIA, ID.URAL, ID.YAKUTSK}
    };

    public static List<Integer> getTerritoriesOfContinent(int continentID) {
        try {
            return Arrays.asList(continentTerritories[continentID]);
        }
        catch (IndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("continent with id " + continentID + " does not exist");
        }
    }

    public static List<Integer> getFullyIncludedContinents(List<Integer> territoryIDs) {
        List<Integer> continentIDs = new ArrayList<>();
        for (int i=0; i<6; i++) {
            if (territoryIDs.containsAll(getTerritoriesOfContinent(i))) {
                continentIDs.add(i);
            }
        }
        return continentIDs;
    }

    public abstract class Continent {
        private Continent() {

        }

        public static final int SOUTH_AMERICA = 0;
        public static final int NORTH_AMERICA = 1;
        public static final int AFRICA = 2;
        public static final int EUROPE = 3;
        public static final int AUSTRALIA = 4;
        public static final int ASIA = 5;
    }
}
