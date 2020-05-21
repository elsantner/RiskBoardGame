package edu.aau.se2.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.io.IOException;
import java.util.logging.Level;

import edu.aau.se2.model.Database;
import edu.aau.se2.utils.LoggerConfigurator;

public class ReconnectDialog extends Dialog {
    private OnResultListener listener;
    private String text;
    private Skin uiSkin;
    private Label lblText;

    public ReconnectDialog(Skin uiSkin, String title, String text, OnResultListener listener) {
        super(title, uiSkin);
        this.uiSkin = uiSkin;
        this.listener = listener;
        this.text = text;
    }

    @Override
    public Dialog show(Stage stage) {
        setupUI(uiSkin, text);
        this.setMovable(false);
        return super.show(stage);
    }

    private void setupUI(Skin uiSkin, String text) {
        this.pad(Gdx.graphics.getHeight()/50f);
        getContentTable().padTop(Gdx.graphics.getHeight()/25f);

        this.lblText = new Label(text, uiSkin);
        getContentTable().add(lblText).center().row();

        getContentTable().row().center();
        TextButton btnReconnect = new TextButton ("Erneut verbinden", uiSkin);
        btnReconnect.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    lblText.setText("Verbinde ...");
                    Database.getInstance().connectIfNotConnected();
                } catch (IOException e) {
                    LoggerConfigurator.getConfiguredLogger("ReconnectDialog", Level.INFO).info("Reconnect failed");
                }
                result(Database.getInstance().isConnected());
            }
        });

        getContentTable().add(btnReconnect).center().row();
    }

    @Override
    protected void result(Object object) {
        if (listener != null) {
            listener.result((boolean)object);
        }
        this.hide();
        this.remove();
    }

    public interface OnResultListener {
        /**
         * Called once the reconnect reports status.
         * @param success Whether the reconnect was successful or not
         */
        void result(boolean success);
    }
}
