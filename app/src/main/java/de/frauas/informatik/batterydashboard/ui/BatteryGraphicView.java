package de.frauas.informatik.batterydashboard.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.batterydashboard.R;

/**
 * This is a custom View that shows a graphical battery with a bar reacting to a passed float value.</br>
 * </b>This view is used inside the layout of a Gauge object, you cannot not use it without one!</b>
 *
 * @see Gauge
 * @see RoundGaugeGraphicView
 * @see FourBarsGraphicView (FourBarsGraphicView is not implemented yet, to do so you can use RoundGaugeGraphicView or BatteryGraphicView as examples :)
 * @author filzinge@stud.fra-uas.de
 */

public class BatteryGraphicView extends View implements IGraphicView {
    private int graphColor;
    private int warningColor;
    private float currentValue = 0.0f;
    private Paint blackPaint;
    private Paint greyPaint;
    private Paint gradientVPaint;
    private RectF bigRect;
    private RectF smallRect;
    private RectF innerRect;
    private float fullBarXCoordinate;
    private float maxValue;
    private float minValue;

    public BatteryGraphicView(Context context) {
        super(context);

        graphColor = context.getColor(R.color.colorAccent2);
        warningColor = context.getColor(R.color.redAccent);

        init();
    }

    /**
     * Constructor that uses attributes (defined as styleable in res/values/attrs.xml). </br>
     * Those attributes can be used in xml when using this view :) </br>
     * See res/layout/graphical_battery_gauge !
     * @param context
     * @param attrs attrs (defined in res/values/attrs.xml), can be passed when using view in xml
     */
    public BatteryGraphicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BatteryGraphicView, 0, 0);

        try {
            graphColor = a.getColor(R.styleable.BatteryGraphicView_color1, context.getColor(R.color.colorAccent2));
            warningColor = a.getColor(R.styleable.BatteryGraphicView_color2, context.getColor(R.color.warning));
        } finally {
            a.recycle();
        }

        init();
    }

    // getters and setters
    @Override
    public void setColors( int color1, int color2){
        this.graphColor = color1;
        this.warningColor = color2;
    }

    public int getGraphColor() {
        return graphColor;
    }
    public int getWarningColor() {
        return warningColor;
    }

    /**
     * initiates Paint objects for drawing the view
     */
    private void init() {
        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStyle(Paint.Style.FILL);

        greyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greyPaint.setColor(Color.GRAY);
        greyPaint.setStyle(Paint.Style.STROKE);
        greyPaint.setStrokeWidth(1f);

        gradientVPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gradientVPaint.setStyle(Paint.Style.FILL);
        //gradientVPaint.setShader(new LinearGradient(0, 0, 50, 0, Color.TRANSPARENT, graphColor, Shader.TileMode.CLAMP));
        gradientVPaint.setShader(new LinearGradient(0, 0, 30, 0, warningColor, graphColor, Shader.TileMode.CLAMP));
        gradientVPaint.setAlpha(200);
    }

    /**
     * overridden method from android View class. This method recalculates the space that can be used by the graphic if the View's size was changed.
     * @param w new width
     * @param h new height
     * @param oldw old width
     * @param oldh old height
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Account for padding
        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        float width = (float) w - xpad;
        float height = (float) h - ypad;
        bigRect = new RectF(1, 1, width - width /12, height -2);
        smallRect = new RectF(width - width /12, height /3, width -2, (height /3)*2 );
        innerRect = new RectF(1, 1, 0, height -2);;
        fullBarXCoordinate = bigRect.right;
    }

    /**
     * overridden method from android View class. This method draws the custom view using the given canvas.
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(smallRect, 1, 1, greyPaint); // small rect outline

        innerRect.right = (fullBarXCoordinate/100)*currentValue; // set right bound for bar rectangle
        canvas.drawRoundRect(innerRect, 2, 2, gradientVPaint); // current value bar

        canvas.drawRoundRect(bigRect, 5, 5, greyPaint); // big rect outline
    }

    /**
     * updates the graphic to reflect a changed value. Invalidate() needs to be called to tell the android UI thread to redraw the view.
     * @param currentValue the new value to be reflected in graphic
     * @see IGraphicView
     */
    @Override
    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
        this.invalidate();
    }

    /**
     * update method for graphical views with more than one value (e.g. cell voltages gauge). Needs to be implemented because of the IGraphicView interface.
     * @param currentValues float array of new values
     * @see IGraphicView
     */
    @Override
    public void setCurrentValue(float[] currentValues) {
        setCurrentValue(currentValues[0]);
    }

    /**
     * method to set min and max value (from gauge constructor).
     * The right min and max values for a metric can be found by calling gaugeMetric.[max/min]Value</br>
     * Method has to be implemented because of IGraphicView interface.
     * @param min minimum value
     * @param max maximum value
     * @see IGraphicView
     * @see frauas.batterydashboard.enums.GaugeMetric
     */
    @Override
    public void setValBounds(float min, float max){
        minValue = min;
        maxValue = max;
    }
}
