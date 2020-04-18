package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import edu.aau.se2.model.Database;

public class JoinLobbyDialog extends Dialog {
    private int lobbyID;

    public JoinLobbyDialog(String title, Skin skin, int lobbyID) {
        super(title, skin);
        initComponents();
        this.lobbyID = lobbyID;
    }

    public JoinLobbyDialog(String title, Skin skin, String windowStyleName, int lobbyID) {
        super(title, skin, windowStyleName);
        initComponents();
        this.lobbyID = lobbyID;
    }

    public JoinLobbyDialog(String title, WindowStyle windowStyle, int lobbyID) {
        super(title, windowStyle);
        initComponents();
        this.lobbyID = lobbyID;
    }

    private void initComponents() {
        text("Wollen Sie wirklich beitreten?");
        button("Ja", true);
        button("Nein", false);
    }

    @Override
    protected void result(Object object) {
        if (object instanceof Boolean && (Boolean)object) {
            Database.getInstance().joinLobby(lobbyID);
        }
    }
}
