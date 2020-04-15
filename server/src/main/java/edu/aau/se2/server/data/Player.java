package edu.aau.se2.server.data;

public class Player {
    private int uid;
    private String nickname;
    private int colorID;
    private transient int armyReserveCount;
    private transient boolean isReady;

    public Player() {
    }

    public Player(int uid, String nickname) {
        this.uid = uid;
        this.nickname = nickname;
        this.armyReserveCount = 0;
        this.isReady = false;
        this.colorID = -1;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getColorID() {
        return colorID;
    }

    /**
     * Sets the player color.
     * @param colorID ID of the color to set. Must be between 0 and 5
     * @throws IllegalArgumentException If colorID is not between 0 and 5
     */
    public void setColorID(int colorID) throws IllegalArgumentException {
        if (colorID < 0 || colorID > 5) {
            throw new IllegalArgumentException("colorID must be between 0 and 5");
        }
        this.colorID = colorID;
    }

    public int getArmyReserveCount() {
        return armyReserveCount;
    }

    public void setArmyReserveCount(int armyReserveCount) {
        this.armyReserveCount = armyReserveCount;
    }

    public void addToArmyReserveCount(int armyReserveCountSummand) {
        this.armyReserveCount += armyReserveCountSummand;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }
}