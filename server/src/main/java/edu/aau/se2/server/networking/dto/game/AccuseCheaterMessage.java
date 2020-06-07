package edu.aau.se2.server.networking.dto.game;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class AccuseCheaterMessage extends InLobbyMessage {

    public AccuseCheaterMessage(){

    }

    public AccuseCheaterMessage(int lobbyID, int playerID ){
      super(lobbyID, playerID);
    }
}
