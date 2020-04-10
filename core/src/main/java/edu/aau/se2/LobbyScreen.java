package edu.aau.se2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

public class LobbyScreen implements Screen {

    private Texture background;
    private Texture lobbyText;
    private SpriteBatch batch;
    private BitmapFont font;
    private Lobby lobby;
    private ArrayList<String> userNames;

    public LobbyScreen(Lobby lobby) {
        this.lobby = lobby;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        background = new Texture(Gdx.files.internal("lobbyScreen.png"));
        lobbyText = new Texture(Gdx.files.internal("lobby2.png"));
        batch = new SpriteBatch();
        batch.begin();
        batch.draw(background, 0 , 0);
        batch.draw(lobbyText, 75,825);
        batch.end();
        renderUsers();
    }

    public void renderUsers(){

        this.userNames =  lobby.getUserNames();
        int xCord = 85;
        int yCord = 800;

        font = new BitmapFont();
        font.setColor(Color.RED);
        font.getData().scale(8);

        batch.begin();
        for (String s: userNames
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
    }
}
