package de.frauas.informatik.batterydashboard.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.batterydashboard.R;

/**
 * This is a custom View that shows a graphical gauge (like a Tacho) with a hand (Zeiger) reacting to a passed float value.</br>
 * </b>This view is used inside the layout of a Gauge object, you cannot not use it without one!</b></br></br>
 *
 * The gauge can either show a range of values from 0 to a max value with a gradient colored "scale" or a
 * range from a negative min value to a positive max value. In the latter case the scale has a different color (color2)
 * below zero. If the current value is <0 the gauge gets a blue in the same color.</br>
 * This behavior is used to visualize recuperation in driving amperage and power gauge for now but could be adapted for warnings ;) </br>
 * (set color2 in when declaring a RoundGaugeGraphicView in an xml layout)
 *
 * @see Gauge
 * @see BatteryGraphicView
 * @see FourBarsGraphicView (FourBarsGraphicView is not implemented yet, to do so you can use RoundGaugeGraphicView or BatteryGraphicView as examples :)
 * @author filzinge@stud.fra-uas.de
 */

public class RoundGaugeGraphicView extends View implements IGraphicView {
    private float currentValues[];
    private float currentValue = 0.0f;
    private float maxValue;
    private float minValue = 0f;
    private float minValueInDeg;
    private float maxValueInDeg;
    private int needleColor;
    private int glowColor;
    private int color1;
    private int color2;
    private float width;
    private float height;
    private float gaugeSideLength;
    float smallSide;
    private Paint thickStrokePaint1;
    private Paint thickStrokePaint2;
    private Paint strokePaint;
    private Paint blackPaint;
    private Paint gradientPaint;
    private Paint gradientPaint2;
    private Paint gradientPaintCharging;
    private Paint needlePaint;
    private RectF square;
    private RectF clipRect;

    /*public RoundGaugeGraphicView(Context context) {
        super(context);

        glowColor = context.getColor(R.color.colorAccent);
        warningColor = context.getColor(R.color.redAccent);

        init();
    }*/

    public RoundGaugeGraphicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RoundGaugeGraphicView, 0, 0);

        try {
            color1 = a.getColor(R.styleable.RoundGaugeGraphicView_color1, context.getColor(R.color.colorPrimaryBright));
            color2 = a.getColor(R.styleable.RoundGaugeGraphicView_color2, context.getColor(R.color.chargingColor));
            needleColor = a.getColor(R.styleable.RoundGaugeGraphicView_needleColor, context.getColor(R.color.textColor));
        } finally {
            a.recycle();
        }

        glowColor = context.getColor(R.color.colorAccent);
        init();
    }

    private void init() {
        thickStrokePaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        thickStrokePaint1.setColor(Color.parseColor("#4db6ac"));
        thickStrokePaint1.setColor(color1);
        thickStrokePaint1.setAlpha(180);
        thickStrokePaint1.setStrokeWidth(5);
        thickStrokePaint1.setStyle(Paint.Style.STROKE);

        thickStrokePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        thickStrokePaint2.setStrokeWidth(5);
        thickStrokePaint2.setStyle(Paint.Style.STROKE);
        thickStrokePaint2.setColor(color2);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(Color.DKGRAY);
        strokePaint.setStrokeWidth(1);
        strokePaint.setStyle(Paint.Style.STROKE);

        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStyle(Paint.Style.FILL);

        gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gradientPaint.setStyle(Paint.Style.FILL);

        gradientPaintCharging = new Paint(Paint.ANTI_ALIAS_FLAG);
        gradientPaintCharging.setStyle(Paint.Style.FILL);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setColor(needleColor);
        needlePaint.setStrokeWidth(3);
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public void setValBounds(float min, float max){
        minValue = min;
        maxValue = max;
        minValueInDeg = minValue*(280/(maxValue-minValue));
        maxValueInDeg = maxValue*(280/(maxValue-minValue));
    }

    @Override
    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
        this.invalidate();
    }

    @Override
    public void setColors( int color1, int color2){
        this.color1 = color1;
        this.color2 = color2;
    }

    @Override
    public void setCurrentValue(float[] currentValues) {
        setCurrentValue(currentValues[0]);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = (float) w;
        height = (float) h;
        if(width <= height){smallSide = width;}else{smallSide = height;}
        float padding = 4;
        gaugeSideLength = smallSide - padding *2;
        gradientPaint.setShader(new RadialGradient(smallSide/2, smallSide/2, smallSide/2.8f, glowColor, Color.TRANSPARENT, Shader.TileMode.CLAMP));
        gradientPaintCharging.setShader(new RadialGradient(smallSide/2, smallSide/2, smallSide/2f, Color.parseColor("#186aff"), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        thickStrokePaint1.setShader(new SweepGradient(smallSide/2, smallSide/2, glowColor, Color.parseColor("#99b44000")));
        square = new RectF(padding, padding, padding + gaugeSideLength, padding + gaugeSideLength);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw outline
        canvas.save();
        if(minValue < 0){
            canvas.rotate(130-minValueInDeg,smallSide/2, smallSide/2);
            canvas.drawArc(square, 0, minValueInDeg,false, thickStrokePaint2);
            canvas.drawArc(square, 0, maxValueInDeg,false, thickStrokePaint1);
        }else{
            canvas.rotate(130,smallSide/2, smallSide/2);
            canvas.drawArc(square, 0, 280,false, thickStrokePaint1);
        }
        canvas.restore();

        // draw big circle with gradient fill, blue when recuperating.
        if(currentValue < 0){
            canvas.drawCircle(smallSide/2, smallSide/2, gaugeSideLength /2-4, gradientPaintCharging); // big circle with gradient fill
        }else{
            canvas.drawCircle(smallSide/2, smallSide/2, gaugeSideLength /2-4, gradientPaint); // big circle with gradient fill
        }

        // drawing the needle
        canvas.save();
        if(minValue < 0) {
            canvas.rotate(40-minValueInDeg + (currentValue*(280/(maxValue-minValue))), smallSide/2, smallSide/2); // 315 = maxDegrees
            canvas.drawLine(smallSide/2, smallSide/2, smallSide/2, smallSide-5, needlePaint); // draw needle
        }else{
            canvas.rotate(40+(maxValueInDeg/100)*currentValue, smallSide/2, smallSide/2); // 40deg is where the minimum should be drawn
            canvas.drawLine(smallSide/2, smallSide/2, smallSide/2, smallSide-5, needlePaint); // draw needle
        }
        canvas.restore();

        // draw inner circle
        canvas.drawCircle(smallSide/2, smallSide/2, gaugeSideLength /3.5f, blackPaint); // inner circle fill
        canvas.drawCircle(smallSide/2, smallSide/2, gaugeSideLength /3.5f, strokePaint); // inner circle outline
    }
}
