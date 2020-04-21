package edu.aau.se2.server.data;

public interface PlayerLostConnectionListener {
    /**
     * Called if a player disconnects from the server.
     * @param player Disconnected player.
     * @param playerLobby Lobby the player is still part of, or null if no such lobby exists.
     */
    void playerLostConnection(Player player, Lobby playerLobby);
}
