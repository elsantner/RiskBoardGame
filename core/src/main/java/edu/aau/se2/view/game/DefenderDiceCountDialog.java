package edu.aau.se2.view.game;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class DefenderDiceCountDialog extends SelectCountDialog {

    public DefenderDiceCountDialog(Skin uiSkin, int minCount, int maxCount, OnResultListener listener) {
        super(uiSkin, "Verteidigen", "Wuerfelanzahl waehlen", minCount, maxCount, listener);

    }

    @Override
    public Dialog show(Stage stage) {
        Dialog d = super.show(stage);
        d.button("Beschuldigen", false);
        return d;
    }

    @Override
    protected void result(Object object) {
        if (listener != null) {
            if ((boolean)object) {
                listener.result(currentCount);
            }
            else {
                listener.result(-1);
            }
        }
        this.hide();
        this.remove();
    }
}
