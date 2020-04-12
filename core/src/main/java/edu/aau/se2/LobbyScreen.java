package edu.aau.se2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


import java.util.ArrayList;

public class LobbyScreen implements Screen {

    private final String LOG = "LobbyScreen";
    private Texture background;
    private Texture lobbyText;
    private Texture lobbyOverlay;
    private Texture line;
    private SpriteBatch batch;
    private BitmapFont font;
    private Lobby lobby;
    private ArrayList<String> userNames;

    public LobbyScreen() {
        assets();

    }

    public LobbyScreen(Lobby lobby) {
        this.lobby = lobby;
        assets();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch = new SpriteBatch();
        batch.begin();

        batch.draw(background, 0, 0);
        batch.draw(lobbyOverlay, 0, 0);
        batch.draw(lobbyText, 75, 800);
        batch.draw(line, 50, 785);

        batch.end();
        renderUsers();
    }

    public void renderUsers() {


        this.userNames = lobby.getUserNames();
        int xCord = 85;
        int yCord = 775;

        batch.begin();
        for (String s : userNames
        ) {
            font.draw(batch, s, xCord, yCord);
            yCord -= 150;
        }
        batch.end();

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        background.dispose();
        lobbyOverlay.dispose();
        line.dispose();
        font.dispose();
        lobbyText.dispose();
    }

    private void assets() {
        Gdx.app.log(LOG, "Loading assets");
        font = new BitmapFont(Gdx.files.internal("font/lobbyFontv2.fnt"));
        font.getData().scale(0.05f);
        font.setColor(new Color(0.6f, 0, 0, 1));
        background = new Texture(Gdx.files.internal("lobby/lobbyScreen.png"));
        lobbyText = new Texture(Gdx.files.internal("lobby/lobby2.png"));
        lobbyOverlay = new Texture(Gdx.files.internal("lobby/lobbyMenuOverlay.png"));
        line = new Texture(Gdx.files.internal("lobby/line.png"));
    }
}
