package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ConfirmDialog extends Dialog {
    private OnClickListener listener;

    public ConfirmDialog(Skin uiSkin, String title, String textMessage, String textYes, String textNo, OnClickListener listener) {
        super(title, uiSkin);
        this.text(textMessage);
        this.button(textYes, true); //send "true" as the result
        this.button(textNo, false); //send "false" as the result
        this.setMovable(false);
        this.listener = listener;
    }

    @Override
    protected void result(Object object) {
        super.result(object);
        if (listener != null) {
            listener.clicked(((boolean)object));
        }
        this.hide();
        this.remove();
    }

    public interface OnClickListener {
        /**
         * Called once a dialog button is pressed
         * @param result True (yes) or false (no)
         */
        void clicked(boolean result);
    }
}
