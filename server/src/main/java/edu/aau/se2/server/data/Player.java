package edu.aau.se2.server.data;

import java.io.Serializable;

public class Player implements Serializable {
    private int uid;
    private String nickname;
    private int colorID;
    private boolean isReady;
    private int armyReserveCount;
    private Card[] tradableSet;
    private boolean askForCardExchange;
    private boolean exchangeCards;
    private boolean hasLost;

    public Player() {
    }

    public Player(int uid, String nickname) {
        this.uid = uid;
        this.nickname = nickname;
        reset();
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
     *
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

    public void reset() {
        this.armyReserveCount = 0;
        this.isReady = false;
        this.colorID = -1;
        this.askForCardExchange = false;
        this.exchangeCards = false;
        this.hasLost = false;
    }

    public Card[] getTradableSet() {
        return tradableSet;
    }

    public void setTradableSet(Card[] tradableSet) {
        this.tradableSet = tradableSet;
    }

    public boolean isAskForCardExchange() {
        return askForCardExchange;
    }

    public void setAskForCardExchange(boolean askForCardExchange) {
        this.askForCardExchange = askForCardExchange;
    }

    public boolean isExchangeCards() {
        return exchangeCards;
    }

    public void setExchangeCards(boolean exchangeCards) {
        this.exchangeCards = exchangeCards;
    }

    public boolean isHasLost() {
        return hasLost;
    }

    public void setHasLost(boolean hasLost) {
        this.hasLost = hasLost;
    }

}