 package de.frauas.informatik.batterydashboard.ui;


import de.frauas.informatik.batterydashboard.enums.GaugeMetric;
import de.frauas.informatik.batterydashboard.enums.GaugeType;

/**
 * This class is needed to easily save Gauges in DashboardConfigs and recreate them later. </br>
 * GaugeBlueprints are the objects saved in a dashboard configuration. </br></br>
 * @see DashboardConfiguration
 * We cannot save Gauge objects for later instantiation because they need a context and they also implement
 * a lot of functionality set up in its constructor (like draggability) that we don't need for saving Dashboard Configurations.
 * As we only need the metric, type and position of a gauge to recreate it, we use this blueprint class. </br>
 * A blueprint without a position specified will be initiated as a gauge at x100/y100.
 *
 * @see GaugeManager GaugeManager and its instantiateConfigBlueprints() and addGaugeToCurrent() methods
 *
 * @author filzinge@stud.fra-uas.de
 */

class GaugeBlueprint {
    GaugeMetric gaugeMetric;
    GaugeType gaugeType;
    float posX=100;
    float posY=100;


    GaugeBlueprint(GaugeMetric gaugeMetric, GaugeType gaugeType){
        this.gaugeMetric = gaugeMetric;
        this.gaugeType = gaugeType;

    }


    GaugeBlueprint(GaugeMetric gaugeMetric, GaugeType gaugeType, float posX, float posY){
        this.gaugeMetric = gaugeMetric;
        this.gaugeType = gaugeType;
       // posX=200;
       // posY=300;

            if(posX!=0) {
                this.posX = posX;
            }
            if(posY!=0) {
                 this.posY = posY;
            }
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }


    @Override
    public String toString(){
        return gaugeMetric.label + " (" + gaugeType.description + ")" +" (" + posX + ")" +" (" + posY + ")";
    }
}
