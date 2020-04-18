package edu.aau.se2.view.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*;


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
    private int height;
    private int width;

    private Database db;
    private List<Player> users;

    public LobbyScreen() {
        db = Database.getInstance();
        db.setPlayersChangedListener(this);

        users = new ArrayList<>();

        height = Gdx.graphics.getHeight();
        width = Gdx.graphics.getWidth();
        // if for some reason phone screen is flipped
        if (height > width) {
            int tmp = height;
            height = width;
            width = tmp;
        }
        batch = new SpriteBatch();

        assets();
    }

    @Override
    public void show() {
        // show
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        batch.draw(background, 0, 0);
        batch.draw(lobbyOverlay, 0, 0);
        batch.draw(line, 0, 0);
        batch.draw(lobbyText, 0, 0);


        batch.end();
        renderUsers();
    }

    private void renderUsers() {

        int xCord = (int)((width * 55) / 1080);
        int yCord = (int)((height  * 760) / 1080);

        batch.begin();
        for (Player us : users
        ) {
            String name = us.getNickname();
            boolean ready = us.isReady();
            font.setColor(new Color(0.6f, 0, 0, 1));
            font.draw(batch, name, xCord, yCord);
            if (ready) {
                font.setColor(new Color(0, 0.8f, 0, 1));
                font.draw(batch, "ready", (xCord + (int)((width * 1200)/ 1920)), yCord);
            } else {
                font.setColor(new Color(0.8f, 0, 0, 1));
                font.draw(batch, "!ready", (xCord +(int)((width * 1200)/ 1920)), yCord);
            }
            yCord -= (int)((height * 150) / 1080);
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
        Gdx.app.log(TAG, "Loading assets" + Gdx.graphics.getDensity() + "  "+ height);

        background = scaleToScreen("lobby/lobbyScreen.png");
        lobbyText = scaleToScreen("lobby/lobby2.png");
        line = scaleToScreen("lobby/line.png");
        lobbyOverlay = scaleToScreen("lobby/lobbyMenuOverlay.png");

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/CenturyGothic.ttf"));
        FreeTypeFontGenerator.setMaxTextureSize(5000);
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = (height * 150) / 1080;
        parameter.borderColor = Color.BLACK;
        parameter.borderWidth = (height * 4) / 1080f;
        font = generator.generateFont(parameter);
        font.setColor(new Color(0.6f, 0, 0, 1));
        generator.dispose();

    }

    private Texture scaleToScreen(String path) {
        Pixmap pixmap = new Pixmap(Gdx.files.internal(path));

        Pixmap pixmap1 = new Pixmap(width, height, pixmap.getFormat());
        pixmap1.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, pixmap1.getWidth(), pixmap1.getHeight());
        return new Texture(pixmap1);
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
