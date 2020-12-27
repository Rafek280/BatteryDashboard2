package de.frauas.informatik.batterydashboard.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.frauas.informatik.batterydashboard.enums.GaugeMetric;
import de.frauas.informatik.batterydashboard.enums.GaugeType;

/**
 * This class provides the data needed to fill the expandable list in the add-gauge-window (config window).</br>
 * These lists have to reflect the available gauges so that the user is able to add them to the dashboard.
 * >>>> If you have implemented new gauges or added new gauge metric or types you will have to make changes here as well! See comments in code :) </br>
 * @author filzinge@stud.fra-uas.de
 *
 * @see <a href="https://www.journaldev.com/9942/android-expandablelistview-example-tutorial">Tutorial I used :)</a>
 */

class ExpListGaugeDataProvider {
    static HashMap<String, List<GaugeBlueprint>> getAvailableGaugesData() {
        HashMap<String, List<GaugeBlueprint>> expandableListDetail = new HashMap<>();

        List<GaugeBlueprint> voltage = new ArrayList<>();
        voltage.add(new GaugeBlueprint(GaugeMetric.VOLTAGE, GaugeType.TEXT_ONLY));
        voltage.add(new GaugeBlueprint(GaugeMetric.VOLTAGE, GaugeType.BIG_NUMBER));
        voltage.add(new GaugeBlueprint(GaugeMetric.VOLTAGE, GaugeType.GRAPHICAL));

        List<GaugeBlueprint> geschwindigkeit = new ArrayList<>();
        geschwindigkeit.add(new GaugeBlueprint(GaugeMetric.GESCHWINDIGKEIT, GaugeType.TEXT_ONLY));
        geschwindigkeit.add(new GaugeBlueprint(GaugeMetric.GESCHWINDIGKEIT, GaugeType.BIG_NUMBER));
        geschwindigkeit.add(new GaugeBlueprint(GaugeMetric.GESCHWINDIGKEIT, GaugeType.GRAPHICAL));

        List<GaugeBlueprint> fahrtenStatisten = new ArrayList<>();
        fahrtenStatisten.add(new GaugeBlueprint(GaugeMetric.DURCHSCHNITTSGESCHWINDIGKEIT, GaugeType.BIG_NUMBER));
        fahrtenStatisten.add(new GaugeBlueprint(GaugeMetric.DURCHSCHNITTSGESCHWINDIGKEIT, GaugeType.GRAPHICAL));
        fahrtenStatisten.add(new GaugeBlueprint(GaugeMetric.DURCHSCHNITTSVERBRAUCH, GaugeType.BIG_NUMBER));
        fahrtenStatisten.add(new GaugeBlueprint(GaugeMetric.DURCHSCHNITTSVERBRAUCH  , GaugeType.GRAPHICAL));

        List<GaugeBlueprint> drivingAmp = new ArrayList<>();
        drivingAmp.add(new GaugeBlueprint(GaugeMetric.DRIVING_AMP, GaugeType.TEXT_ONLY));
        drivingAmp.add(new GaugeBlueprint(GaugeMetric.DRIVING_AMP, GaugeType.BIG_NUMBER));
        drivingAmp.add(new GaugeBlueprint(GaugeMetric.DRIVING_AMP, GaugeType.GRAPHICAL));

        List<GaugeBlueprint> tagesKilometerzaehler= new ArrayList<>();
        tagesKilometerzaehler.add(new GaugeBlueprint(GaugeMetric.TAGES_KILOMETER_ZAEHLER, GaugeType.TEXT_ONLY));
        tagesKilometerzaehler.add(new GaugeBlueprint(GaugeMetric.TAGES_KILOMETER_ZAEHLER, GaugeType.BIG_NUMBER));


        List<GaugeBlueprint> aktuellerVerbrauch= new ArrayList<>();
        aktuellerVerbrauch.add(new GaugeBlueprint(GaugeMetric.CONSUMPTION, GaugeType.BIG_NUMBER));


        List<GaugeBlueprint> power = new ArrayList<>();
        power.add(new GaugeBlueprint(GaugeMetric.POWER, GaugeType.TEXT_ONLY));
        power.add(new GaugeBlueprint(GaugeMetric.POWER, GaugeType.BIG_NUMBER));
        power.add(new GaugeBlueprint(GaugeMetric.POWER, GaugeType.GRAPHICAL));

        List<GaugeBlueprint> accuTemp = new ArrayList<>();
        accuTemp.add(new GaugeBlueprint(GaugeMetric.CHARGER_TEMP, GaugeType.TEXT_ONLY));
        accuTemp.add(new GaugeBlueprint(GaugeMetric.CHARGER_TEMP, GaugeType.BIG_NUMBER));

        List<GaugeBlueprint> capacity = new ArrayList<>();
        capacity.add(new GaugeBlueprint(GaugeMetric.CAPACITY, GaugeType.TEXT_ONLY));
        capacity.add(new GaugeBlueprint(GaugeMetric.CAPACITY, GaugeType.BIG_NUMBER));
        capacity.add(new GaugeBlueprint(GaugeMetric.CAPACITY, GaugeType.GRAPHICAL));

        List<GaugeBlueprint> cellvoltages = new ArrayList<>();
        cellvoltages.add(new GaugeBlueprint(GaugeMetric.CELL_VOLTAGES, GaugeType.TEXT_ONLY));

        List<GaugeBlueprint> celltemps = new ArrayList<>();
        celltemps.add(new GaugeBlueprint(GaugeMetric.CELL_TEMPS, GaugeType.TEXT_ONLY));

        /* the values for the following gauges are not yet implemented
            TODO do it! :)
             step 1
                - implement calculation of range, odometer and consumption in Battery (there are notes!)
             step 1 b (nice-to-have graphical gauges!)
                - for graphical gauges create a layout (xml), a class and a graphicView class implementing IGraphicView
                    (take a look at one of the existing graphical gauges!)
                - check the inflateAndInit method of the Gauge class and add code to create your graphical Gauge in the switch/case statement
             step 2
             - uncomment the appropriate section here or follow the example of the Lists above.
             - if you created a graphical gauge add this option to the section :)
             - put the list you created to expandableListDetail with appropriate title -> see end of this class ;)
-*/
        List<GaugeBlueprint> range = new ArrayList<>(); // = reichweite
        range.add(new GaugeBlueprint(GaugeMetric.RANGE, GaugeType.TEXT_ONLY));
        range.add(new GaugeBlueprint(GaugeMetric.RANGE, GaugeType.BIG_NUMBER));

        List<GaugeBlueprint> odometer = new ArrayList<>(); // = tages-km-Zähler
        odometer.add(new GaugeBlueprint(GaugeMetric.ODOMETER, GaugeType.TEXT_ONLY));
        odometer.add(new GaugeBlueprint(GaugeMetric.ODOMETER, GaugeType.BIG_NUMBER));

        expandableListDetail.put("Fahrten Statistiken", fahrtenStatisten);
        expandableListDetail.put("Spannung", voltage);
        expandableListDetail.put("Geschwindigkeit", geschwindigkeit);
        expandableListDetail.put("Driving Amperage", drivingAmp);
        expandableListDetail.put("Verbrauch", aktuellerVerbrauch);
        expandableListDetail.put("Leistung", power);
        expandableListDetail.put("Charger-Temperatur", accuTemp);
        expandableListDetail.put("Kapazität", capacity);
        expandableListDetail.put("Zellspannungen", cellvoltages);
        expandableListDetail.put("Tages kilometer zaehler", tagesKilometerzaehler);
        expandableListDetail.put(celltemps.get(0).gaugeMetric.label, celltemps);

        return expandableListDetail;
    }
}
