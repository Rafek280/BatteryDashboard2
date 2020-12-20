package de.frauas.informatik.batterydashboard.enums;

/**
 * This enum contains the different metrics that a gauge can display
 * as well as the according label and unit as strings and the min and max value. </br>
 * You should add realistic min and max values when implementing graphical gauges for more metrics as default values are 0 and 100 for round gauges for example.
 *
 * @author filzinge@stud.fra-uas.de
 */

public enum GaugeMetric {
    VOLTAGE ("Spannung","V", 0, 250),
    POWER("Leistung","kW", -10, 30),
    DRIVING_AMP("Driving Amperage","A", -50,150),
    CHARGER_TEMP("Charger-Temp","°C"),
    CONSUMPTION("Verbrauch","unit"),
    RANGE("Reichweite","km"),
    CELL_VOLTAGES("Zellspannungen","V"),
    ODOMETER("Gefahrene Kilometer","km"),
    CAPACITY("Kapazität","%", 0, 100),
    CELL_TEMPS("Zelltemperaturen", "°C");

    public final String label;
    public final String unit;
    public float maxValue = 100;
    public float minValue = 0;
    public String unit(){ return unit; }
    public String label(){ return label; }
    public float getMaxValue(){ return maxValue; }
    public float getMinValue(){ return minValue; }

    GaugeMetric(String label, String unit) {
        this.label = label;
        this.unit = unit;
    }

    GaugeMetric(String label, String unit, float minValue, float maxValue) {
        this.label = label;
        this.unit = unit;
        this.maxValue = maxValue;
        this.minValue = minValue;
    }
}
