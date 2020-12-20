package de.frauas.informatik.batterydashboard.enums;

/**
 * This enum contains the different types of gauges and a description to show in GUI (in add window).
 *
 * @author filzinge@stud.fra-uas.de
 */

public enum GaugeType {
    TEXT_ONLY ("Text"),
    BIG_NUMBER ("große Zahl"),
    GRAPHICAL ("grafisch");

    public final String description;

    GaugeType(String description) {
        this.description = description;
    }
}
