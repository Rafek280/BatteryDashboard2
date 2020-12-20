package de.frauas.informatik.batterydashboard.background;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.WorkerThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/** This class is a Singleton. It reads values from a file provided by the BMS.
 * </br></br>
 * This class *might* have to be fixed before it works with a real device!
 * Things it depends on might have changed and due to Covid-19 I wasn't able to work with the real radio device. – J
 * 
 * @author Lehmann
 */

public class StatReceiver {
    private static final String TAG = "StatReceiver";
    private static final int DEFAULT_UPDATE_FREQUENCY = 1;
    private static StatReceiver instance;
    private Timer statsTimer = new Timer();

    public static final int VOLTAGE_SCALE = 100;
    public static final int TEMPERATURE_SCALE = 1;
    public static final int AMPERAGE_SCALE = 10;
    public static final int CAPACITY_SCALE = 1;

    OnNewDataListener listener;

    // Singleton implementation
    static StatReceiver getInstance() {
        if (instance == null)
            instance = new StatReceiver();
        return instance;
    }

    public void startReadingFromFile(File source, Context context) throws FileNotFoundException {
        startReadingFromFile(source, DEFAULT_UPDATE_FREQUENCY, context);
    }

    /**
     * This method *might* have to be fixed before it works with a real device!
     *  Things it depends on might have changed and due to Covid-19 I wasn't able to work with the real radio device. – J
     * @param source source file
     * @param updatesPerTenSeconds update frequency per 10 seconds – to see the difference in GUI set the UI update frequency to a similar value in UiService!
     * @param context
     * @throws FileNotFoundException
     * @see de.frauas.informatik.batterydashboard.ui.UiService UiService (set update frequency here too to see all updates in GUI!)
     */
    public void startReadingFromFile(File source, int updatesPerTenSeconds, Context context)
            throws FileNotFoundException {
        final BufferedReader br = new BufferedReader(new FileReader(source));
        statsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                StringBuilder msg = new StringBuilder();
                try {
                    while (br.ready()) {
                        msg.append(br.readLine());
                    }
                    if (!msg.equals("")) {
                        long tstart = System.currentTimeMillis();
                        listener.onNewData(msg);
                        long tend = System.currentTimeMillis();
                        Log.i(TAG, String.format("processing statselement in %dms", tend - tstart));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "IO Error. Check console output.", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context, "Wrong format in stream", Toast.LENGTH_LONG).show();
                    System.out.print(String.format("failed to parse message: \n%s", msg));
                    e.printStackTrace();
                }
            }
        }, 500, 1000 / updatesPerTenSeconds);
    }



    private int[] toIntArr(String[] input) {
        return Arrays.stream(input)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    public void stop(){
        statsTimer.cancel();
        statsTimer.purge();
    }

    public void setOnNewDataListener(OnNewDataListener listener) {
        this.listener = listener;
    }

    public interface OnNewDataListener{
        @WorkerThread
        void onNewData(StringBuilder packetAsString);
    }
}
