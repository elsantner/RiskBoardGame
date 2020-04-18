package edu.aau.se2.view.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


import java.util.ArrayList;
import java.util.List;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnPlayersChangedListener;
import edu.aau.se2.server.data.Player;

public class LobbyScreen implements Screen, OnPlayersChangedListener {

    private static final String TAG = "LobbyScreen";
    private Texture background;
    private Texture lobbyText;
    private Texture lobbyOverlay;
    private Texture line;
    private SpriteBatch batch;
    private BitmapFont font;

    private Database db;
    private List<Player> users;

    public LobbyScreen() {
        db = Database.getInstance();
        db.setPlayersChangedListener(this);
        assets();
        users = new ArrayList<>();
    }

    @Override
    public void show() {
        // show
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

    private void renderUsers() {

        int xCord = 85;
        int yCord = 775;

        batch.begin();
        for (Player us : users
        ) {
            String name = us.getNickname();
            boolean ready = us.isReady();
            font.setColor(new Color(0.6f, 0, 0, 1));
            font.draw(batch, name, xCord, yCord);
            if (ready) {
                font.setColor(new Color(0, 0.8f, 0, 1));
                font.draw(batch, "ready", (xCord + 1100), yCord);
            } else {
                font.setColor(new Color(0.8f, 0, 0, 1));
                font.draw(batch, "!ready", (xCord + 1100), yCord);
            }
            yCord -= 150;
        }
        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        //resize
    }

    @Override
    public void pause() {
        //pause
    }

    @Override
    public void resume() {
        //resume
    }

    @Override
    public void hide() {
        //hide
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
        Gdx.app.log(TAG, "Loading assets");
        font = new BitmapFont(Gdx.files.internal("font/lobbyFontv2.fnt"));
        font.getData().scale(0.05f);
        font.setColor(new Color(0.6f, 0, 0, 1));
        background = new Texture(Gdx.files.internal("lobby/lobbyScreen.png"));
        lobbyText = new Texture(Gdx.files.internal("lobby/lobby2.png"));
        lobbyOverlay = new Texture(Gdx.files.internal("lobby/lobbyMenuOverlay.png"));
        line = new Texture(Gdx.files.internal("lobby/line.png"));
    }

    public List<Player> getUsers() {
        return users;
    }

    public void setUsers(List<Player> users) {
        this.users = users;
    }

    @Override
    public void playersChanged(List<Player> newPlayers) {
        this.users = newPlayers;
    }
}
