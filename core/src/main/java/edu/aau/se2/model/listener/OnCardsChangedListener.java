package edu.aau.se2.model.listener;

public interface OnCardsChangedListener {
    void singleNewCard(String cardName);
    void refreshCards(String[] cardNames);
}
