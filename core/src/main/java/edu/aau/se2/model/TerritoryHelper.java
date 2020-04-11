package edu.aau.se2.model;

import java.util.Arrays;

public abstract class TerritoryHelper {
    public abstract static class ID {
        public final static int Argentina = 1;
        public final static int Brazil = 2;
        public final static int Peru = 3;
        public final static int Venezuela = 4;
        public final static int Alaska = 5;
        public final static int WesternCanada = 6;
        public final static int CentralAmerica = 7;
        public final static int EasternUS = 8;
        public final static int Greenland = 9;
        public final static int NorthwestTerritory = 10;
        public final static int CentralCanada = 11;
        public final static int EasternCanada = 12;
        public final static int WesternUS = 13;
        public final static int CentralAfrica = 14;
        public final static int EastAfrica = 15;
        public final static int Egypt = 16;
        public final static int Madagascar = 17;
        public final static int NorthAfrica = 18;
        public final static int SouthAfrica = 19;
        public final static int GreatBritain = 20;
        public final static int Iceland = 21;
        public final static int NorthernEurope = 22;
        public final static int Scandinavia = 23;
        public final static int SouthernEurope = 24;
        public final static int Ukraine = 25;
        public final static int WesternEurope = 26;
        public final static int EasternAustralia = 27;
        public final static int Indonesia = 28;
        public final static int NewGuinea = 29;
        public final static int WesternAustralia = 30;
        public final static int Afghanistan = 31;
        public final static int China = 32;
        public final static int India = 33;
        public final static int Irkutsk = 34;
        public final static int Japan = 35;
        public final static int Kamchatka = 36;
        public final static int MiddleEast = 37;
        public final static int Mongolia = 38;
        public final static int SoutheastAsia = 39;
        public final static int Siberia = 40;
        public final static int Ural = 41;
        public final static int Yakutsk = 42;
    }

    private final static Integer[][] neighbouringTerritories = new Integer[][] {
            {ID.Brazil, ID.Peru},   // Argentina
            {ID.Peru, ID.Argentina, ID.Venezuela, ID.NorthAfrica},  // Brazil
            {ID.Brazil, ID.Argentina, ID.Venezuela},    // Peru
            {ID.Peru, ID.Brazil},   //Venezuela
            {ID.WesternCanada, ID.NorthwestTerritory, ID.Kamchatka},  // Alaska
            {ID.Alaska, ID.NorthwestTerritory, ID.CentralCanada, ID.WesternUS}, // WesternCanada
            {ID.WesternUS, ID.EasternUS, ID.Venezuela}, // CentralAmerica
            {ID.WesternUS, ID.CentralAmerica, ID.CentralCanada, ID.EasternCanada}, // EasternUS
            {ID.NorthwestTerritory, ID.CentralCanada, ID.EasternCanada, ID.Iceland}, // Greenland
            {ID.Greenland, ID.Alaska, ID.WesternCanada, ID.CentralCanada}, // NorthwestTerritory
            {ID.Greenland, ID.NorthwestTerritory, ID.WesternCanada, ID.EasternCanada, ID.WesternUS, ID.EasternUS}, // CentralCanada
            {ID.Greenland, ID.CentralCanada, ID.EasternUS}, // EasternCanada
            {ID.EasternUS, ID.WesternCanada, ID.CentralCanada, ID.CentralAmerica}, // WesternUS
            {ID.NorthAfrica, ID.EastAfrica, ID.SouthAfrica}, // CentralAfrica
            {ID.NorthAfrica, ID.SouthAfrica, ID.CentralAfrica, ID.Egypt, ID.Madagascar, ID.MiddleEast}, // EastAfrica
            {ID.NorthAfrica, ID.EastAfrica, ID.MiddleEast, ID.SouthernEurope}, // Egypt
            {ID.EastAfrica, ID.SouthAfrica}, // Madagascar
            {ID.EastAfrica, ID.CentralAfrica, ID.Egypt, ID.SouthernEurope, ID.WesternEurope, ID.Brazil}, // NorthAfrica
            {ID.EastAfrica, ID.CentralAfrica, ID.Madagascar}, // SouthAfrica
            {ID.WesternEurope, ID.NorthernEurope, ID.Scandinavia, ID.Iceland}, // GreatBritain
            {ID.Greenland, ID.GreatBritain, ID.Scandinavia}, // Iceland
            {ID.WesternEurope, ID.SouthernEurope, ID.Ukraine, ID.Scandinavia, ID.GreatBritain}, // NorthernEurope
            {ID.NorthernEurope, ID.Ukraine, ID.GreatBritain, ID.Iceland}, // Scandinavia
            {ID.WesternEurope, ID.NorthernEurope, ID.Ukraine, ID.MiddleEast, ID.Egypt, ID.NorthAfrica}, // SouthernEurope
            {ID.Scandinavia, ID.NorthernEurope, ID.SouthernEurope, ID.MiddleEast, ID.Afghanistan, ID.Ural}, // Ukraine
            {ID.NorthAfrica, ID.GreatBritain, ID.NorthernEurope, ID.SouthernEurope}, // WesternEurope
            {ID.WesternAustralia, ID.NewGuinea}, // EasternAustralia
            {ID.WesternAustralia, ID.NewGuinea, ID.SoutheastAsia}, // Indonesia
            {ID.WesternAustralia, ID.EasternAustralia, ID.Indonesia}, // NewGuinea
            {ID.NewGuinea, ID.EasternAustralia, ID.Indonesia}, // WesternAustralia
            {ID.Ukraine, ID.Ural, ID.MiddleEast, ID.India, ID.China}, // Afghanistan
            {ID.SoutheastAsia, ID.India, ID.Afghanistan, ID.Ural, ID.Siberia, ID.Mongolia}, // China
            {ID.MiddleEast, ID.Afghanistan, ID.China, ID.India}, // India
            {ID.Mongolia, ID.Siberia, ID.Yakutsk, ID.Kamchatka}, // Irkutsk
            {ID.Alaska, ID.Japan, ID.Mongolia, ID.Irkutsk, ID.Yakutsk}, // Kamchatka
            {ID.Kamchatka, ID.Mongolia}, // Japan
            {ID.EastAfrica, ID.Egypt, ID.SouthernEurope, ID.Ukraine, ID.Afghanistan, ID.India}, // MiddleEast
            {ID.China, ID.Siberia, ID.Irkutsk, ID.Kamchatka, ID.Japan}, // Mongolia
            {ID.Indonesia, ID.India, ID.China}, // SoutheastAsia
            {ID.Ural, ID.China, ID.Mongolia, ID.Irkutsk, ID.Yakutsk}, // Siberia
            {ID.Ukraine, ID.Afghanistan, ID.China, ID.Siberia}, // Ural
            {ID.Siberia, ID.Irkutsk, ID.Kamchatka}, // Yakutsk
    };

    public static Integer[] getNeighbouringTerritories(int territoryID) {
        try {
            return neighbouringTerritories[territoryID-1];
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("territory with id " + territoryID + " does not exist");
        }
    }

    public static boolean areNeighbouring(int territoryID1, int territoryID2) {
        return Arrays.asList(getNeighbouringTerritories(territoryID1)).contains(territoryID2);
    }
}
