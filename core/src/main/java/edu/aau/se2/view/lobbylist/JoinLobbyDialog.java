package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import edu.aau.se2.RiskGame;

public class JoinLobbyDialog extends Dialog {
    private RiskGame game;

    public JoinLobbyDialog(String title, Skin skin) {
        super(title, skin);
        initComponents();
    }

    public JoinLobbyDialog(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);
        initComponents();
    }

    public JoinLobbyDialog(String title, WindowStyle windowStyle) {
        super(title, windowStyle);
        initComponents();
    }

    private void initComponents() {
        text("Wollen Sie wirklich beitreten?");
        button("Ja", true);
        button("Nein", false);
    }

    @Override
    protected void result(Object object) {
        if (object instanceof Boolean && (Boolean)object) {
            // TODO pass lobby
            // TODO game.setScreen(new LobbyScreen());
        }
    }

    public void setGame(RiskGame game) {
        this.game = game;
    }
}
