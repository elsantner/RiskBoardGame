package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class SelectCountDialog extends Dialog {
    private OnResultListener listener;
    private int minCount;
    private int maxCount;
    private int currentCount;
    private Label lblCurrentCount;
    private String text;
    private Skin uiSkin;
    private boolean abortAllowed = true;

    public SelectCountDialog(Skin uiSkin, String title, String text, int minCount, int maxCount, OnResultListener listener) {
        super(title, uiSkin);
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.currentCount = minCount + (maxCount-minCount)/2;
        this.text = text;
        this.uiSkin = uiSkin;
        this.listener = listener;
    }

    @Override
    public Dialog show(Stage stage) {
        setupUI(uiSkin, text);
        this.setMovable(false);
        return super.show(stage);
    }

    private void setupUI(Skin uiSkin, String text) {
        this.pad(Gdx.graphics.getHeight()/50f);
        if (text != null) {
            getContentTable().add(new Label(text, uiSkin)).center().colspan(2).row();
        }

        getContentTable().row().center().colspan(2);
        lblCurrentCount = new Label(Integer.toString(currentCount), uiSkin);
        TextButton btnPlus = new TextButton ("+", uiSkin);
        TextButton btnMinus = new TextButton ("-", uiSkin);
        btnPlus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (currentCount+1 <= maxCount) {
                    currentCount++;
                }
                lblCurrentCount.setText(currentCount);
            }
        });

        btnMinus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (currentCount-1 >= minCount) {
                    currentCount--;
                }
                lblCurrentCount.setText(currentCount);
            }
        });

        getContentTable().add(lblCurrentCount).center().colspan(2).row();
        getContentTable().add(btnPlus).center().minWidth(Gdx.graphics.getWidth()/15f);
        getContentTable().add(btnMinus).center().minWidth(Gdx.graphics.getWidth()/15f);
        this.button("OK", true);
        if (abortAllowed) {
            this.button("Abbruch", false);
        }
    }

    @Override
    protected void result(Object object) {
        if (listener != null) {
            if ((boolean)object) {
                listener.result(currentCount);
            }
            else {
                listener.result(0);
            }
        }
        this.hide();
        this.remove();
    }

    public void setAbortAllowed(boolean abortAllowed) {
        this.abortAllowed = abortAllowed;
    }

    public interface OnResultListener {
        /**
         * Called once the "OK" button is pressed
         * @param count The final count of the dialog
         */
        void result(int count);
    }
}
