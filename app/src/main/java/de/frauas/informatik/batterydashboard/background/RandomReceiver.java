package de.frauas.informatik.batterydashboard.background;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/** This class extends StatReceiver and mocks a connection to the BMS (Battery Management System) by creating random values.</br>
 * Values seem to become unrealistic after some time (e.g. running overnight)...
 * </br>
 * @author Lehmann, Deegener, (Julia)
 */

public class RandomReceiver extends StatReceiver {
    private static final String TAG = "RandomReceiver";
    private final Random rand = new Random(System.currentTimeMillis()); //
    private int[] cells={0,3, 0, -2, 1, 4, -3,0,-2,-1,0,2,-5,-3,-4,
            1,-3,2,4,-3,-5,2,4,0,0,3,-2,4,-2, -5,
            -2,0,0,2,1,-1,0,3,0,-1,0,-1, 2, -1, -2,
            1,2,3,-2,-3,4,-1,0,0,-2,-2,0,2,0,-2};

    private int amp=200, volt=20000, capa=80;
    private boolean increase;
    @Override
    public void startReadingFromFile(File source, int updatesPerTenSeconds, Context context){
        assert updatesPerTenSeconds != 0.0;
        Timer statsTimer = new Timer();
        statsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    long tstart = System.currentTimeMillis();
                    StringBuilder randomString = genRandString();
                    listener.onNewData(randomString);
                    long tend = System.currentTimeMillis();
                    //Log.i(TAG, String.format("processing statselement in %dms", tend - tstart));
                } catch (Exception e) {
                    //TODO make toast
                    Log.d(TAG, "BMS-Informationen sind falsch formatiert!");
                    Log.e(TAG, "BMS-Informationen sind falsch formatiert!");
                    e.printStackTrace();
                }
            }
        }, 500, (10000 / updatesPerTenSeconds));
    }

    private StringBuilder genRandString() {
        StringBuilder sb = new StringBuilder(400);
        if (increase) {
            amp += 30;
            volt -= 90;
            if (amp > 150) {
                amp = 150;
                increase = false;
            }
        }
        else {
            amp -= 10;
            volt += 30;

            if (amp < -50) {
                amp = -50;
                increase = true;
            }
        }

        sb.append("CellVoltage:");
        addCellValues(sb, 60, 100);
        sb.append("CellTemp:");
        addCellTempValues(sb, 20, 100);
        sb.append("DrivingAmperage:");
        sb.append(amp);
        sb.append("ChargingAmperage:");
        addChargingAmperageValues(sb, 4, 100);
        sb.append("ChargerTemp:");
        addChargerTempValues(sb, 4, 100);
        sb.append("Capacity:");
        addCapacityValues(sb, 4, 100);

        return sb;
    }

    private void addRandomValues(StringBuilder sb, int count, int bound){
        for (int i = 0; i < count; i++) {
            sb.append(rand.nextInt(bound));
            sb.append(';');
        }
        sb.deleteCharAt(sb.length() - 1);
    }
    private void addCellValues(StringBuilder sb, int count, int bound){
        int cellv=volt/count;
        for (int i = 0; i < count; i++) {
            sb.append(cells[i]+cellv);
            sb.append(';');
        }
        sb.deleteCharAt(sb.length() - 1);
    }
    private void addChargingAmperageValues(StringBuilder sb, int count, int bound){
        for (int i = 0; i < count; i++) {
            sb.append(0);
            sb.append(';');
        }
        sb.deleteCharAt(sb.length() - 1);
    }
    private void addChargerTempValues(StringBuilder sb, int count, int bound){
        for (int i = 0; i < count; i++) {
            sb.append(25+rand.nextInt(5));
            sb.append(';');
        }
        sb.deleteCharAt(sb.length() - 1);
    }
    private void addCapacityValues(StringBuilder sb, int count, int bound){
        for (int i = 0; i < count; i++) {
            sb.append(capa+rand.nextInt(15)); // etwas zwischen capa (80) und 95
            sb.append(';');
        }
        sb.deleteCharAt(sb.length() - 1);
    }
    private void addCellTempValues(StringBuilder sb, int count, int bound){
        for (int i = 0; i < count; i++) {
            sb.append(20+rand.nextInt(10));
            sb.append(';');
        }
        sb.deleteCharAt(sb.length() - 1);
    }


}
