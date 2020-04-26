package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class SelectCountDialog extends Dialog {
    private static final Skin uiSkin = new Skin(Gdx.files.internal("dialog/uiskin.json"));
    private OnResultListener listener;
    private int minCount;
    private int maxCount;
    private int currentCount;
    private Label lblCurrentCount;

    public SelectCountDialog(String title, int minCount, int maxCount, OnResultListener listener) {
        super(title, uiSkin);
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.currentCount = minCount + (maxCount-minCount)/2;

        setupUI();
        this.setMovable(false);
        this.listener = listener;
    }

    private void setupUI() {
        lblCurrentCount = new Label(Integer.toString(currentCount), uiSkin);
        this.text(lblCurrentCount).center();
        getContentTable().row().center();
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

        getContentTable().add(btnPlus).center();
        getContentTable().add(btnMinus).center();
        this.button("OK", true);
        this.button("Abbruch", false);
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

    public interface OnResultListener {
        /**
         * Called once the "OK" button is pressed
         * @param count The final count of the dialog
         */
        void result(int count);
    }
}
