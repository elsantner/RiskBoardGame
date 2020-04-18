package edu.aau.se2;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import edu.aau.se2.view.lobbylist.LobbyListScreen;


public class RiskGame extends Game {

    @Override
    public void create() {
        Gdx.app.log(RiskGame.class.getName(), "Starting game");
        LobbyListScreen lobbyListScreen = new LobbyListScreen(this);
        setScreen(lobbyListScreen);
    }

    @Override
    public void setScreen(Screen screen) {
        // making sure no memory leaks are happening
        Screen oldScreen = getScreen();
        if (oldScreen != null)
            oldScreen.dispose();
        super.setScreen(screen);
    }

    public Lobby createLobby() {
        Lobby lobby = new Lobby();
        lobby.createLobby();
        return lobby;
    }
}
