package de.frauas.informatik.batterydashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

import de.frauas.informatik.batterydashboard.background.BatteryDataService;
import de.frauas.informatik.batterydashboard.ui.UiService;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

/**
 * This activity is only used to manage permissions and start the service. The Service will be
 * responsible for listening to the Serial port and update the overlay.
 * </br>
 * The app might get stuck or crash until you have given overlay permission.
 * You might have to do so several times. Please keep trying and maybe find a better solution! :)
 * </br>
 * For app's closing behavior see setExitButton() method in UiService.
 * @see UiService UiService (see setExitButton method for app's closing behavior)
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 2;
    public static final int FLAG_NONBLOCKING_OVERLAY = FLAG_NOT_TOUCH_MODAL | FLAG_NOT_FOCUSABLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if you are running this on a new device, just keep allowing and restart the app until it works.
        if(!Settings.canDrawOverlays(this)){
            // ask for setting
            Intent intent = new Intent  (Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        }

        // start the OverlayService that controls UI
        Intent dataService = new Intent(this, BatteryDataService.class);
        Intent uiService = new Intent(this, UiService.class);
        //startService(dataService);
        startService(uiService);

        finish();
    }
}
