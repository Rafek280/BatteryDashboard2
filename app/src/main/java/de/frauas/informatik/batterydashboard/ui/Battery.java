package de.frauas.informatik.batterydashboard.ui;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import com.example.batterydashboard.R;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

// scale factors are used to convert the 3-digit integers in protocol to floating point numbers
import static de.frauas.informatik.batterydashboard.background.StatReceiver.AMPERAGE_SCALE;
import static de.frauas.informatik.batterydashboard.background.StatReceiver.TEMPERATURE_SCALE;
import static de.frauas.informatik.batterydashboard.background.StatReceiver.VOLTAGE_SCALE;

/** This class is a model for the hardware battery system.</br>
 * A battery object is created by the UiService when the app is started, and given to the DataService when bound.
 * The DataService writes data in the battery object, the UiService reads values from it to display them in the UI.
 * It contains n blocks (n = value BLOCK_COUNT from battery_specs.xml), usually 4.</br>
 * It also contains other info about the battery: currentDrivingAmperage and many calculated values like e.g. VoltageSum.
 * The battery object itself is not connected to the Receiver, but only handled by the services.
 * </br>
 * See also: documentation of summer term 2020 (contains class diagram and graphical representation of battery system).</br></br>
 * Call printBattery() on this component to visualize the battery setup and current values in logcat (Info).
 * or just set printInfo in BatteryDataService to true! :)
 *
 * @see de.frauas.informatik.batterydashboard.background.BatteryDataService BatteryDataService (set printInfo to true for visualization of battery data)
 * @author filzinge@stud.fra-uas.de
 * */

public class Battery {
    private final Resources r;
    private int MAXAGE_MILLIS; // values older than this will be ignored in UI
    private int CELL_COUNT_PER_BLOCK;
    private int BLOCK_COUNT;
    private int MAX_AMPERE_HOURS_PER_CELL;
    private int TEMPERATURE_SENSOR_PER_BLOCK;
    private int CELLS_PER_THERMOMETER;
    private float WARNING_THRESHOLD;
    private Date timestamp;
    private Block[] blocks;
    private float currentDrivingAmperage;
    private boolean isCharging;


    public Battery(Resources resources){
        this.r = resources;
        MAXAGE_MILLIS                = resources.getInteger(R.integer.max_data_age); // values older than this will be ignored in UI
        CELL_COUNT_PER_BLOCK         = resources.getInteger(R.integer.cellsPerBlock);
        BLOCK_COUNT                  = resources.getInteger(R.integer.blocks);
        MAX_AMPERE_HOURS_PER_CELL    = resources.getInteger(R.integer.maxAHPerCell);
        TEMPERATURE_SENSOR_PER_BLOCK = resources.getInteger(R.integer.thermometer_per_block);
        CELLS_PER_THERMOMETER        = resources.getInteger(R.integer.cellsPerThermometer);
        WARNING_THRESHOLD = (resources.getInteger(R.integer.warningThreshold));

        // make blocks
        blocks = new Block[BLOCK_COUNT];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(i, CELL_COUNT_PER_BLOCK, TEMPERATURE_SENSOR_PER_BLOCK);
        }
    }

    /**
     * models one of the battery's [four] blocks with a number of cells specified by cellcount in battery and so on. </br>
     * The Block class contains methods for setting the values of its components (charger, cells, block itself),
     * those are called by the battery's setValues method.
     */
    class Block {
        float[] cellVoltages;
        int[] cellTemps;
        float chargingAmperage;
        // boolean chargerTempWarning;
        int chargerTemp;
        int capacity;
        private final int blockId;

        Block(int id, int cellCountPerBlock, int tempSensorPerBlock) {
            this.blockId = id;
            cellTemps = new int[tempSensorPerBlock];
            cellVoltages = new float[cellCountPerBlock];
        }

        void setCellTemps(String[] cellTempString) {
            int j = 0;
            for(int i = blockId*TEMPERATURE_SENSOR_PER_BLOCK; i < blockId*TEMPERATURE_SENSOR_PER_BLOCK+TEMPERATURE_SENSOR_PER_BLOCK; i++){
                cellTemps[j] = Integer.parseInt(cellTempString[i]) / TEMPERATURE_SCALE;
                j++;
            }
        }

        void setCellVoltages(String[] cellVoltageString) {
            int j = 0;
            for(int i = blockId*CELL_COUNT_PER_BLOCK; i < blockId*CELL_COUNT_PER_BLOCK+CELL_COUNT_PER_BLOCK; i++){
                cellVoltages[j] = Float.parseFloat(cellVoltageString[i]) / VOLTAGE_SCALE;
                j++;
            }
        }

        void setChargingAmperage(String[] chargingAmperageString){
            chargingAmperage = Float.parseFloat(chargingAmperageString[blockId]) / AMPERAGE_SCALE;
        }

        void setChargerTemp(String[] chargerTempString) {
            chargerTemp = Integer.parseInt(chargerTempString[blockId]);
        }

        void setCapacity(String[] capacityString){
            capacity = Integer.parseInt(capacityString[blockId]);
        }
    }

    private int totalCellThermometerCount() {
        return BLOCK_COUNT * CELL_COUNT_PER_BLOCK / CELLS_PER_THERMOMETER;
    }

    private int totalCellCount() {
        return BLOCK_COUNT * CELL_COUNT_PER_BLOCK;
    }


    /**
     * parses a stringBuilder object and sets the values in the battery model
     * @param msg a StringBuilder object containing the data from the receiver (from the hardware), following the communication protocol format
     *            (find file Kommunikation_App_BMS_Charger.txt)
     */

    public void setValues(StringBuilder msg) {
        String[] cellVoltage =
                msg.substring(msg.indexOf("CellVoltage:")+12, msg.indexOf("CellTemp:")).split(";");
        String[] cellTemp =
                msg.substring(msg.indexOf("CellTemp:")+9, msg.indexOf("DrivingAmperage:")).split(";");
        String drivingAmperage =
                msg.substring(msg.indexOf("DrivingAmperage:")+16, msg.indexOf("ChargingAmperage:"));
        String[] chargingAmperage =
                msg.substring(msg.indexOf("ChargingAmperage:")+17, msg.indexOf("ChargerTemp:")).split(";");
        String[] chargerTemp =
                msg.substring(msg.indexOf("ChargerTemp:")+12,msg.indexOf("Capacity:")).split(";");
        String[] capacity =
                msg.substring(msg.indexOf("Capacity:")+9).split(";");

        timestamp = Calendar.getInstance().getTime();
        int i = 0;

        // check if battery is charging (it's charging if any chargingAmperage value is bigger than 0)
        int chargingAmpSum = 0;
        for(i = 0; i < chargingAmperage.length; i++){
            chargingAmpSum += Integer.parseInt(chargingAmperage[i]);
        }
        if(chargingAmpSum > 0){
            isCharging = true;
        }

        // set value(s) in battery
        currentDrivingAmperage = Integer.parseInt(drivingAmperage)/10f;

        // set values in blocks
        for (Block block : blocks) {
            block.setCellTemps(cellTemp);
            block.setCellVoltages(cellVoltage);
            block.setChargerTemp(chargerTemp);
            block.setChargingAmperage(chargingAmperage);
            block.setCapacity(capacity);
        }

    }

    /*public boolean isCharging() {
        return isCharging;
    }*/

    public float drivingAmperage(){
        return currentDrivingAmperage;
    }

    public int voltageSum(){
        float sum = 0;
        for (Block block : blocks) {
            for (float cV : block.cellVoltages) {
                sum += cV;
            }
        }
        return Math.round(sum);
    }
    public int durchscnitt(){
        float sum = 3;
        for (Block block : blocks) {
            for (float cV : block.cellVoltages) {
                sum += cV;
            }
        }
        return Math.round(sum);
    }

    public float power(){
        // Leistung = Spannung * Stromstärke
        // 1W = a V * A => 1kW = (V*A)/1000
        return Math.round(voltageSum()*drivingAmperage()/100) / 10f;
    }

    public float[] getChargingAmperages(){
        float[] result = new float[BLOCK_COUNT];
        for (int i = 0; i < result.length; i++) {
            result[i] = blocks[i].chargingAmperage;
        }
        return result;
    }

    public float[] getChargerTemperatures() {
        float[] result = new float[BLOCK_COUNT];
        for (int i = 0; i < result.length; i++) {
            result[i] = blocks[i].chargerTemp;
        }
        return result;
    }

    public float getAvrgChargerTemp(){
        float[] temps = getChargerTemperatures();
        float sum = 0;
        for (float temp : temps) {
            sum += temp;
        }
        return sum / BLOCK_COUNT;
    }

    private float minCellVoltage(){
        float min = blocks[0].cellVoltages[0];
        for (Block block : blocks) {
            for (float cV : block.cellVoltages) {
                if(cV < min) { min = cV; }
            }
        }
        return min;
    }

    private float maxCellVoltage(){
        float max = blocks[0].cellVoltages[0];
        for (Block block : blocks) {
            for (float cV : block.cellVoltages) {
                if(cV > max) { max = cV; }
            }
        }
        return max;
    }

    private float avrgCellVoltage(){
        float sum = 0;
        for (Block block : blocks) {
            for (float cV : block.cellVoltages) {
                sum += cV;
            }
        }
        return Math.round(100*(sum/totalCellCount()))/100f;
    }

    private float minCellTemp(){
        float min = blocks[0].cellTemps[0];
        for (Block block : blocks) {
            for (float cT : block.cellTemps) {
                if(cT < min) { min = cT; }
            }
        }
        return min;
    }

    private float maxCellTemp(){
        float max = blocks[0].cellTemps[0];
        for (Block block : blocks) {
            for (float cT : block.cellTemps) {
                if(cT > max) { max = cT; }
            }
        }
        return max;
    }

    private float avrgCellTemp(){
        int sum = 0;
        for (Block block : blocks) {
            for (float cT : block.cellTemps) {
                sum += cT;
            }
        }
        return Math.round(10*((float)sum)/(totalCellThermometerCount()))/10f; // auf eine Nachkommastelle "abgeschnitten"
    }

    public float[] getCellVoltages(){
        float[] voltages = new float[3];
        voltages[0] = minCellVoltage();
        voltages[1] = maxCellVoltage();
        voltages[2] = avrgCellVoltage();
        return voltages;
    }

    public float[] getCellTemps(){
        float[] temps = new float[3];
        temps[0] = minCellTemp();
        temps[1] = maxCellTemp();
        temps[2] = avrgCellTemp();
        return temps;
    }

    public float capacity(){
        int min = blocks[0].capacity;
        for (Block block : blocks) {
            if(block.capacity < min) {
                min = block.capacity;
            }
        }
        return min;
    }

    /**
     * the overwritten toString method creates a string describing the setup and current values of the battery
     * @return string describing the setup and current values of the battery
     */
    @Override
    public String toString(){
        StringBuilder s = new StringBuilder(" ");
        s.append(System.lineSeparator()).append("--- B A T T E R Y  S T A T E ---").append(System.lineSeparator());
        s.append(super.toString()).append(" @ ").append(timestamp).append(System.lineSeparator());
        s.append("Overall capacity (min capa of all blocks): ").append(capacity()).append(System.lineSeparator());
        s.append("Voltage sum (voltages of all cells): ").append(voltageSum()).append(System.lineSeparator());
        s.append("Power (Leistung in kW): ").append(power()).append(System.lineSeparator());
        for (Block block : blocks) {
            s.append("BLOCK ").append(block.blockId).append(System.lineSeparator());
            s.append("CellVoltages in V ").append(Arrays.toString((block.cellVoltages))).append(System.lineSeparator());
            s.append("CellTemp Sensors in °C ").append(Arrays.toString((block.cellTemps))).append(System.lineSeparator());
            s.append("Charger Temperature in °C: ").append(block.chargerTemp).append(System.lineSeparator());
            s.append("Charging Amperage in A: ").append(block.chargingAmperage).append(System.lineSeparator());
            s.append("Capacity in Ah (or %): ").append(block.capacity).append(System.lineSeparator());
        }
        s.append(System.lineSeparator());
        return s.toString();
    }

    /**
     * prints the current battery state to console (logcat -> info)
     */
    public void printBattery(){
        Log.i("Battery", toString());
    }
}
