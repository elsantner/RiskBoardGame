package edu.aau.se2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class RiskGame extends Game {
	private LobbyScreen lobbyScreen;
	
	@Override
	public void create () {

		lobbyScreen = new LobbyScreen(createLobby());
		setScreen(lobbyScreen);

	}

	@Override
	public void dispose () {
		super.dispose();
	}

	public Lobby createLobby(){
		Lobby lobby = new Lobby();
		lobby.startLobby();
		return lobby;
	}
}
