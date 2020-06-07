package edu.aau.se2.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import edu.aau.se2.RiskGame;
import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnNicknameChangeListener;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.DefaultNameProvider;
import edu.aau.se2.view.PopupMessageDisplay;
import edu.aau.se2.view.asset.AssetName;

public class MainMenu extends AbstractScreen implements OnNicknameChangeListener {
    private Skin mySkin;
    private Stage stage;
    private Viewport gamePort;
    private Button create;
    private Button join;
    private Button exit;
    private Texture backgroundTxt;
    private Table table;
    private SpriteBatch batch;
    private boolean showDisconnectedDialog;
    private Button settings;
    private DefaultNameProvider defaultNameProvider;
    private String nickname;
    private boolean showChangeNameUI;
    private Preferences prefs = Gdx.app.getPreferences("profile");
    private PopupMessageDisplay popupMessageDisplay;
    private String nickNameTxt;


    public MainMenu(RiskGame riskGame, DefaultNameProvider defaultNameProvider, PopupMessageDisplay popupMessageDisplay){
        this(riskGame, false, defaultNameProvider, popupMessageDisplay);
    }

    public MainMenu(RiskGame riskGame, boolean showDisconnectedDialog, DefaultNameProvider defaultNameProvider, PopupMessageDisplay popupMessageDisplay) {
        super(riskGame);
        this.showDisconnectedDialog = showDisconnectedDialog;

        mySkin = getGame().getAssetManager().get(AssetName.UI_SKIN_2);
        gamePort = new ScreenViewport();
        stage = new Stage(gamePort);
        batch = new SpriteBatch();
        this.defaultNameProvider = defaultNameProvider;
        this.popupMessageDisplay = popupMessageDisplay;
        this.nickNameTxt = "Nickname";

        backgroundTxt = getGame().getAssetManager().get(AssetName.TEX_LOBBY_SCREEN);

        table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);
        showChangeNameUI = false;

        if (prefs.getString("name").equals("New Player")) {
            prefs.remove("name");
            Gdx.input.getTextInput(new Input.TextInputListener() {

                @Override
                public void input(String text) {
                    if (text.length() > 0) {
                        Database.getInstance().setPlayerNickname(text);
                        prefs.putString("name", text);
                        popupMessageDisplay.showMessage(nickNameTxt + " : " + text);
                    } else {
                        Database.getInstance().setPlayerNickname(defaultNameProvider.getDeviceName());
                        prefs.putString("name", defaultNameProvider.getDeviceName());
                        popupMessageDisplay.showMessage("Keine Eingabe. Nickname blieb: " + defaultNameProvider.getDeviceName());
                    }
                    prefs.flush();
                }

                @Override
                public void canceled() {
                    Database.getInstance().setPlayerNickname(defaultNameProvider.getDeviceName());
                    prefs.putString("name", defaultNameProvider.getDeviceName());
                    prefs.flush();
                    popupMessageDisplay.showMessage(nickNameTxt + " : " + defaultNameProvider.getDeviceName());
                }
            }, "Nickname eingeben", defaultNameProvider.getDeviceName(), "");

        }

        Gdx.input.setInputProcessor(stage);
    }

    private void displayDisconnectedDialog() {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        ReconnectDialog dialog = new ReconnectDialog(uiSkin, "Keine Verbindung",
                "Moechten Sie es erneut versuchen?", success -> {
                    if (!success) {
                        displayDisconnectedDialog();
                    }
                    else {
                        setButtonsEnabled(true);
                    }
                });
        showDialog(dialog, stage, 3);
    }

    private void setButtonsEnabled(boolean enabled) {
        create.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
        join.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
        exit.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
    }

    public void setupButtons(){

        create = new TextButton("Spiel erstellen", mySkin);
        join = new TextButton("Spiel beitreten", mySkin);
        exit = new TextButton("Spiel verlassen", mySkin);
        settings = new TextButton("Nickname festlegen", mySkin);

        table.add(create).width(gamePort.getWorldWidth() * 0.35f).height(gamePort.getWorldHeight() * 0.1f).padBottom(gamePort.getWorldHeight() * 0.03f);
        table.row();
        table.add(join).width(gamePort.getWorldWidth() * 0.35f).height(gamePort.getWorldHeight() * 0.1f).padBottom(gamePort.getWorldHeight() * 0.03f);
        table.row();
        table.add(settings).width(gamePort.getWorldWidth() * 0.35f).height(gamePort.getWorldHeight() * 0.1f).padBottom(gamePort.getWorldHeight() * 0.03f);
        table.row();
        table.add(exit).width(gamePort.getWorldWidth() * 0.35f).height(gamePort.getWorldHeight() * 0.1f);

    }

    public void setupLogo(){
        Texture logoTxt = getGame().getAssetManager().get(AssetName.TEX_LOGO);
        Image logoImage = new Image(logoTxt);
        logoImage.setScale(gamePort.getWorldWidth() / (logoImage.getWidth() * 2));
        logoImage.setOrigin(Align.center);

        table.add(logoImage).padBottom(gamePort.getWorldHeight() * 0.1f).row();
    }

    public void onClickButtons(){
        create.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Database.getInstance().hostLobby();
            }
        });


        join.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getGame().openLobbyListScreen();
            }
        });


        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        settings.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(prefs.getString("name") != null && !prefs.getString("name").equals("")){
                    nickname = prefs.getString("name");
                } else {
                    nickname = defaultNameProvider.getDeviceName();
                }
                Gdx.input.getTextInput(new Input.TextInputListener() {

                    @Override
                    public void input(String text) {
                        if(text.length() > 0){
                            Database.getInstance().setPlayerNickname(text);
                            prefs.remove("name");
                            prefs.putString("name", text);
                            prefs.flush();
                            popupMessageDisplay.showMessage(nickNameTxt + " : " + text);
                        } else {
                            popupMessageDisplay.showMessage(nickNameTxt + " darf nicht leer sein.");
                        }
                    }

                    @Override
                    public void canceled() {
                        prefs.remove("name");
                        if(nickname == null){
                            Database.getInstance().setPlayerNickname(defaultNameProvider.getDeviceName());
                            prefs.putString("name", defaultNameProvider.getDeviceName());
                        } else {
                            Database.getInstance().setPlayerNickname(nickname);
                            prefs.putString("name", nickname);
                        }
                        popupMessageDisplay.showMessage(nickNameTxt + " : " + nickname);
                        prefs.flush();
                    }
                }, "Nickname eingeben", nickname, "");
            }
        });
    }

    @Override
    public void show() {
        setupLogo();
        setupButtons();
        onClickButtons();

        addInputProcessor(stage);
        if (showDisconnectedDialog) {
            setButtonsEnabled(false);
            displayDisconnectedDialog();
        }
    }

    @Override
    public void handleBackButton() {
        Gdx.app.exit();
    }

    @Override
    public void render(float delta) {
        stage.act(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClearColor(1, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(backgroundTxt,0,0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        //currently unused
    }

    @Override
    public void pause() {
        //currently unused
    }

    @Override
    public void resume() {
        //currently unused
    }

    @Override
    public void hide() {
        //currently unused
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
    }

    @Override
    public void nicknameChanged(String nickname) {
        this.nickname = nickname;
    }

    public boolean getShowChangeNameUI() {
        return this.showChangeNameUI;
    }

    public void setShowChangeNameUI(boolean showChangeNameUI) {
        this.showChangeNameUI = showChangeNameUI;
    }
}
