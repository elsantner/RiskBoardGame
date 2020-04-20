package edu.aau.se2.server.data;

import java.io.Serializable;

public class Player implements Serializable {
    private int uid;
    private String nickname;
    private int colorID;
    private boolean isReady;
    private transient int armyReserveCount;

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
     * @param colorID ID of the color to set. Must be between 0 and 5.
     */
    public void setColorID(int colorID) {
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