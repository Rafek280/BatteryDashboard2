package de.frauas.informatik.batterydashboard.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.batterydashboard.R;

import java.io.IOException;
import java.util.ArrayList;

import de.frauas.informatik.batterydashboard.enums.GaugeMetric;
import de.frauas.informatik.batterydashboard.enums.GaugeType;

/**
 * This is the class for all gauges on the dashboard whatever their layout is.</br>
 * A gauge is an element on the dashboard that shows or visualizes a certain metric (e.g. driving amperage or cell voltages) by showing text and in some cases graphics.</br>
 * A gauge has a GaugeMetric and a GaugeType (text, big number or graphical) and it can hold the current value(s) for its metric.
 * If its type is "graphical" it also holds an object implementing IGraphicView, that can be e.g. a RoundGaugeGraphicView or BatteryGraphicView object depending on the metric.</br>
 * A gauge does also hold position coordinates and a context, which is needed to draw it on the screen. </br>
 * Gauge extends ConstraintLayout and is thus a ViewGroup and a visual element.
 * </br></br>
 * If a gauge needs to be saved for later re-instantiation (e.g. when saving a dashboard) you save a GaugeBlueprint object instead.
 * A GaugeBlueprint has a GaugeMetric and GaugeType and position coordinates, but does not have any current values nor any visual components like graphic views or layouts.</br>
 * A blueprint can be gotten from a gauge and a gauge can be made from a blueprint for a given context (done by GaugeManager).
 * </br></br>
 * A gauge inflates a layout depending on type and metric. There is one layout for the text-type, one layout for the big-number-type, but several layouts
 * for the graphical type. The right graphical layout to inflate has to be chosen in the inflateAndInit() method, depending on the metric.</br>
 * Notice that to get a certain View (e.g. the TextView for the value) from the layout, we have to use findViewWithTag() instead of findViewById()
 * because those layouts are used more than once. To learn more about which elements with which tags a gauge layout has to or can have, please read the pdf documentation!
 * </br></br>
 * >>> You can find more info on how the gauge class, IGraphicalView and its implementing classes and the according
 * layouts work together in the summer term 2020 pdf documentation on the BatteryDashboard – with pictures ;)
 * @see GaugeMetric
 * @see GaugeType
 * @see GaugeBlueprint
 * @author filzinge@stud.fra-uas.de
 */


public class Gauge extends ConstraintLayout {
    private static final String TAG = "Gauge class";
    GaugeMetric gaugeMetric;
    GaugeType gaugeType;
    private boolean isGraphical = false;
    private float dX, dY, posX, posY;
    private int lastAction;
    private TextView numberView;
    IGraphicView graphicView;
    private ViewGroup content;
    public boolean isHighlighted;
    private float initialBgAlpha = .5f;
    private View deleteIcon;
    private StatistiksGaugeManager state;
    private GaugeManager gg;
    private boolean statistik = false;

    public boolean isStatistik() {
        return statistik;
    }

    public void setStatistik(boolean statistik) {
        this.statistik = statistik;
    }



    public Gauge(Context context){
        super(context);
        gaugeMetric = GaugeMetric.CAPACITY;
        gaugeType = GaugeType.BIG_NUMBER;
    } // default "tools" constructor needed for some reason...
    /**
     * constructor to make a gauge with params defining context, metric and type.
     * @param context A context it can be drawn/inflated in (on UI thread).
     * @param m metric for gauge to create (e.g. voltage or charger_temp)
     * @param t type for gauge to create (text, big number, graphical)
     * @see GaugeMetric
     * @see GaugeType
     * @see GaugeBlueprint
     */
    public Gauge(Context context, GaugeMetric m, GaugeType t) {
        super(context);
        gaugeMetric = m;
        gaugeType = t;

        inflateAndInit(context);
    //if (statistik==false) {
       // setDraggable(this);
    //}
    }

    /**
     * Constructor to make a gauge from a GaugeBlueprint object
     * @param context A context the gauge can be drawn/inflated in (on UI thread).
     * @param blueprint A GaugeBlueprint object defining metric, type and position.
     */
    public Gauge(Context context, GaugeBlueprint blueprint) {
        super(context);
        gaugeMetric = blueprint.gaugeMetric;
        gaugeType = blueprint.gaugeType;

        inflateAndInit(context);

        // set to blueprint's position

       // do{
        if (statistik==false) {

            setDraggable(this);

            setToPosition(blueprint);
        }
      //  }while(gg.getActiveGauges());
    }

    /**
     * This method inflates the appropriate layout for the gaugeMetric and gaugeType set in the constructor
     * and also adds the draggable-functionality to the gauge.</br>
     * When you create a new graphical gauge you have to add code for inflating it here in the switch/case statement :)</br>
     * >> important: isGraphical has to be set to true for graphical gauges only or their IGraphicView element will not be updated.
     *
     * @param context the context to inflate the layout in
     */
    private void inflateAndInit(Context context) {
        // inflate layout depending on metric and type
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        if (gaugeMetric == GaugeMetric.CELL_VOLTAGES || gaugeMetric == GaugeMetric.CELL_TEMPS) {
            content = (ViewGroup) inflater.inflate(R.layout.cellvoltages_gauge, this);
        } else if (gaugeType == GaugeType.BIG_NUMBER) {
            content = (ViewGroup) inflater.inflate(R.layout.big_number_gauge, this);
        } else if (gaugeType == GaugeType.TEXT_ONLY) {
            content = (ViewGroup) inflater.inflate(R.layout.text_only_gauge, this);
        } else {
            isGraphical = true;
            // TODO implement and add code for more graphical gauges...
            switch (gaugeMetric) {
                case CAPACITY:
                case CONSUMPTION:
                    // use battery gauge
                    content = (ViewGroup) inflater.inflate(R.layout.graphical_battery_gauge, this);
                    graphicView = content.findViewWithTag("graphicView");
                    break;
                case POWER:
                case VOLTAGE:
                case GESCHWINDIGKEIT:
                case DURCHSCHNITTSGESCHWINDIGKEIT:
                case  TAGES_KILOMETER_ZAEHLER:
                case DRIVING_AMP:
                    // use round gauge
                    content = (ViewGroup) inflater.inflate(R.layout.graphical_round_gauge, this);
                    graphicView = content.findViewWithTag("graphicView");
                    graphicView.setColors(Color.parseColor("#6cff6c"), context.getColor(R.color.colorPrimaryBright));
                    break;
                default:
                    // use big number gauge as fallback when there is no graphical implementation for metric
                    // (should be prevented by GUI, but who likes NullPointerExceptions?^^)
                    content = (ViewGroup) inflater.inflate(R.layout.big_number_gauge, this);
                    isGraphical = false;
                    break;
            }
            if (isGraphical) graphicView.setValBounds(gaugeMetric.minValue, gaugeMetric.maxValue);
        }

        // set variables and static values
        TextView label = content.findViewWithTag("label");
        label.setText(gaugeMetric.label());
        TextView unit = content.findViewWithTag("unit");
        // set unit for all views tagged with "unit"
        ArrayList<View> unitViews = getViewsByTag(content, "unit");
        for(View v : unitViews){
            ((TextView) v).setText(gaugeMetric.unit());
        }
        // find the first view with "value" tag even if tag is "value1"
        try{
            numberView = content.findViewWithTag("value");
        }
        catch(NullPointerException e){
            numberView = content.findViewWithTag("value1");
        }

        prepareDeleteIcon();
    }

    private void prepareDeleteIcon(){
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        deleteIcon = inflater.inflate(R.layout.delete_icon_view, null);
    }

    void setDraggable(View view){



            view.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override

                public boolean onTouch(View view, MotionEvent event) {

                    int width = view.getLayoutParams().width;
                    int height = view.getLayoutParams().height;
                    if(isStatistik() == true) {
                        try {
                            state.draggableBlocker();
                        }
                        catch (Exception exception){
                            System.out.println("Stastik Gaugen sind nicht dragable");
                        }
                    }else
                    switch (event.getAction()) {


                            case MotionEvent.ACTION_MOVE:
                                lastAction = MotionEvent.ACTION_MOVE;


                                if (width == getMaxWidth() && height == getMaxHeight()) {
                                } else {
                                    view.animate()
                                            .x(event.getRawX() + dX)
                                            .y(event.getRawY() + dY)
                                            .setDuration(0)
                                            .start();

                                    if (event.getRawX() + dX < 0) {
                                        view.animate()
                                                .x(0)
                                                .setDuration(0)
                                                .start();
                                    }
                                    if (event.getRawX() + dX > 195) {
                                        view.animate()
                                                .x(195)
                                                .setDuration(0)
                                                .start();
                                    }
                                    if (event.getRawY() + dY + height > getMaxHeight()) {
                                        view.animate()
                                                .y(getMinHeight() - height)
                                                .setDuration(0)
                                                .start();
                                    }
                                    if (event.getRawY() + dY < 0) {
                                        view.animate()
                                                .y(0)
                                                .setDuration(0)
                                                .start();
                                    }
                                    if (event.getRawY() + dY > 460) {
                                        view.animate()
                                                .y(460)
                                                .setDuration(0)
                                                .start();
                                    }

                                }

                                break;


                        case MotionEvent.ACTION_DOWN:
                            lastAction = MotionEvent.ACTION_DOWN;
                            activateHighlight();
                            view.bringToFront();
                            dX = view.getX() - event.getRawX();
                            dY = view.getY() - event.getRawY();
                            break;

                        case MotionEvent.ACTION_UP:
                            if (lastAction == MotionEvent.ACTION_MOVE) {
                                // on-click action:
                                toggleHighlight();
                            }
                            deactivateHighlight();
                            break;

                        default:
                            return false;
                    }
                    return true;
                }

            });

    }

    void activateHighlight(){
        isHighlighted = true;
        View bg = content.findViewWithTag("background");
        initialBgAlpha = bg.getAlpha();
        bg.setAlpha(1);
    }
    void deactivateHighlight(){
        isHighlighted = false;
        View bg = content.findViewWithTag("background");
        bg.setAlpha(initialBgAlpha);
    }
    void toggleHighlight(){
        if(isHighlighted){
            deactivateHighlight();
        }
        else{
            activateHighlight();
        }
    }

    void activateDeleteMode(){
        deleteIcon.setOnClickListener((l) -> {
            //TESTING
            GaugeManager.getInstance().deleteGauge(getContext(), this);
        });

        content.addView(deleteIcon);
    }

    void deactivateDeleteMode(){
        content.removeView(findViewWithTag("gaugeDeleteIcon"));
    }

    void update(float value){
        numberView.setText(String.valueOf(value));
        if(isGraphical){
            graphicView.setCurrentValue(value);
        }
    }
    void update(int value){
        numberView.setText(String.valueOf(value));
        if(isGraphical){
            graphicView.setCurrentValue((float)value);
        }
    }
    void update(float[] values){
        // iterate over numberView array and set values, e.g. for cell voltages gauge
        for(int i = 0; i < values.length; i++){
            numberView = findViewWithTag("value"+(i+1));
            numberView.setText(String.valueOf(values[i]));
        }
    }
    void update(int[] values){
        // iterate over numberView array and set values, e.g. for accu temp graphic gauge
    }

    void setToPosition(GaugeBlueprint blueprint){
        content.setX(blueprint.posX);
        content.setY(blueprint.posY);
    }

    GaugeBlueprint getBlueprint(){
        return new GaugeBlueprint(this.gaugeMetric, this.gaugeType, this.posX, this.posY);
    }

    private static ArrayList<View> getViewsByTag(ViewGroup viewGroup, String tag){
        ArrayList<View> views = new ArrayList<>();
        final int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }
            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }
        }
        return views;
    }
}