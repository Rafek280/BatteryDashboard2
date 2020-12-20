package de.frauas.informatik.batterydashboard.ui;

import java.util.ArrayList;

public class DashboardConfiguration {
    boolean isPreset;
    String name;
    ArrayList<GaugeBlueprint> gauges;

    DashboardConfiguration(ArrayList<GaugeBlueprint> gauges, String name, boolean isPreset){
        this.gauges = gauges;
        this.name = name;
        this.isPreset = isPreset;
    }

    DashboardConfiguration(ArrayList<GaugeBlueprint> gauges){
        this.gauges = gauges;
        name = "Unbenannt";
    }

    DashboardConfiguration(ArrayList<GaugeBlueprint> gauges, String name){
        this.gauges = gauges;
        this.name = name;
    }

    void updatePositions(){
        for (GaugeBlueprint g : gauges) {

        }
    }

    void setName(String n){
        this.name = n;
    }
}
