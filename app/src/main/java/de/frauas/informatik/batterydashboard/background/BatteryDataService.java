package de.frauas.informatik.batterydashboard.background;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.WorkerThread;
import de.frauas.informatik.batterydashboard.ui.Battery;

import java.io.File;

/**
 * >>>> set printInfo to true to have battery data printed readably to console on every update! <<<<<<</br>
 * This is the DataService that is started by the UiService when the app starts.
 * </br></br>
 * This service implements the OnNewData listener of a StatReceiver (can be RandomReceiver for mocking a connection),
 * through which it gets the data from the hardware battery system (or random data in case of RandomReceiver) as a StringBuilder object.
 *</br></br>
 * This service receives a battery object from UiService after being bound. When DataService receives new data
 * (set frequency in the appropriate Receiver class), it sets the new values in the shared battery object.
 *
 * @see de.frauas.informatik.batterydashboard.ui.UiService
 * @see RandomReceiver RandomReceiver (extends StatReceiver)
 * @see StatReceiver StatReceiver
 * @see Battery
 *
 * @author Lehmann (receiver connection etc.)
 * @author filzinge@stud.fra-uas.de
 *
 */

public class BatteryDataService extends Service implements StatReceiver.OnNewDataListener {
    private static final String TAG = "BatteryDataService";
    private Battery battery;
    private final IBinder binder = new LocalBinder(); // Binder given to clients
    /**
     * set to true to have battery data printed readably to console (Logcat -> info) on every update!
     */
    private boolean printInfo = false;

    /**
     * Binder given to clients
     */
    public class LocalBinder extends Binder {
        public BatteryDataService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BatteryDataService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // set the data source – either connection to real arduino device or random simulator instead
        try {
            File source = new File("/dev/ttyACM0");
            StatReceiver receiver;
            if (source.exists())
                receiver = StatReceiver.getInstance();
            else
                receiver = new RandomReceiver();  // simulates random data stream

            receiver.startReadingFromFile(source, 10, this); // will update data every 10 seconds/frequency
            receiver.setOnNewDataListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * React to new Datapacket from the Serial port.
     * </br>
     * set printInfo to true to see the data formatted in logcat (info)
     * @param dataPacket a StringBuilder object containing all values from the StatReceiver
     */
    @WorkerThread
    @Override
    public void onNewData(StringBuilder dataPacket) {
        // TODO find out when battery is charging and set dashboard to charger config – in UiService?!

        // set values in battery
        battery.setValues(dataPacket);
        if(printInfo) {
            Log.i(TAG, "onNewData: " + dataPacket.toString());
            battery.printBattery();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
       // return Service.START_REDELIVER_INTENT; // service startet sich immer wieder
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "...is being destroyed");
        StatReceiver.getInstance().stop();
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }
}
