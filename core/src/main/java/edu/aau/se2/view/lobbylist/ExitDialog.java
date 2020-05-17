package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import edu.aau.se2.model.Database;

public class ExitDialog extends Dialog {

    public ExitDialog(String title, Skin skin) {
        super(title, skin);
        initComponents();
    }

    public ExitDialog(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);
        initComponents();
    }

    public ExitDialog(String title, WindowStyle windowStyle) {
        super(title, windowStyle);
        initComponents();
    }

    private void initComponents() {
        text("Wirklich verlassen?");
        button("Ja", true);
        button("Nein", false);
    }

    @Override
    protected void result(Object object) {
        if ((boolean)object) {
            Database.getInstance().leaveLobby();
        }
    }
}
