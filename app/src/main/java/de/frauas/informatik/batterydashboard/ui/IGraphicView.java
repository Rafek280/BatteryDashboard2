package de.frauas.informatik.batterydashboard.ui;

/**
 * Interface implemented by the different graphic views (see classes below).
 * @see BatteryGraphicView
 * @see RoundGaugeGraphicView
 * @see FourBarsGraphicView (FourBarsGraphicView is not fully implemented yet)
 * @author filzinge@stud.fra-uas.de
 */

public interface IGraphicView {
    void setCurrentValue(float currentValue);
    void setCurrentValue(float[] currentValues);
    void setValBounds(float min, float max);
    void setColors(int color1, int color2);
}
