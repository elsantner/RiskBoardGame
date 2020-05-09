package edu.aau.se2;

import android.widget.Toast;
import com.badlogic.gdx.backends.android.AndroidApplication;
import edu.aau.se2.view.PopupMessageDisplay;

public class ToastMessageDisplay implements PopupMessageDisplay {

    private AndroidApplication appl;

    public ToastMessageDisplay(AndroidApplication appl) {
        this.appl = appl;
    }

    @Override
    public void showMessage(String message) {
        appl.runOnUiThread(() -> Toast.makeText(appl, message, Toast.LENGTH_LONG).show());
    }
}
