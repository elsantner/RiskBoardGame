package edu.aau.se2;

import com.badlogic.gdx.Game;
import edu.aau.se2.view.game.GameScreen;

public class RiskGame extends Game {
	private GameScreen gameScreen;
  private LobbyScreen lobbyScreen;
	
	@Override
	public void create () {
		gameScreen = new GameScreen();
    lobbyScreen = new LobbyScreen(createLobby());
		setScreen(gameScreen);
	}

	@Override
	public void dispose () {
		super.dispose();
		gameScreen.dispose();
	}
  
  public Lobby createLobby() {
        Lobby lobby = new Lobby();
        lobby.createLobby();
        return lobby;
    }
}
