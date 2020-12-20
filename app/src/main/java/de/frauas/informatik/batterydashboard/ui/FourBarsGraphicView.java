package de.frauas.informatik.batterydashboard.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.batterydashboard.R;

/**
 * Not fully implemented yet. >>> To do so you can use the other graphic views as examples and read a little more in their documentation – see below! :)
 * @see RoundGaugeGraphicView
 * @see BatteryGraphicView
 */
public class FourBarsGraphicView extends View implements IGraphicView {
    private float currentValues[];
    private int graphColor;
    private int warningColor;
    private float currentValue = 0.0f;
    private Paint greyPaint;
    private Paint gradientHPaint;
    private float maxValue;
    private float minValue;

    public FourBarsGraphicView(Context context) {
        super(context);

        graphColor = context.getColor(R.color.colorAccent2);
        warningColor = context.getColor(R.color.redAccent);

        init();
    }

    public FourBarsGraphicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BatteryGraphicView, 0, 0);

        try {
            graphColor = a.getColor(R.styleable.BatteryGraphicView_color1, context.getColor(R.color.colorAccent2));
            warningColor = a.getColor(R.styleable.BatteryGraphicView_color2, context.getColor(R.color.redAccent));
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        greyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greyPaint.setColor(Color.GRAY);
        greyPaint.setStyle(Paint.Style.STROKE);
        greyPaint.setStrokeWidth(1f);

        gradientHPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gradientHPaint.setStyle(Paint.Style.FILL);
        gradientHPaint.setShader(new LinearGradient(0, 0, 50, 0, Color.TRANSPARENT, graphColor, Shader.TileMode.CLAMP));
    }

    @Override
    public void setCurrentValue(float currentValue) {

    }

    @Override
    public void setCurrentValue(float[] currentValues) {
    }

    @Override
    public void setValBounds(float min, float max){
        minValue = min;
        maxValue = max;
    }

    @Override
    public void setColors( int color1, int color2){
        this.graphColor = color1;
        this.warningColor = color2;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Account for padding
        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        float width = (float) w - xpad;
        float height = (float) h - ypad;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("FOUR BARS GRAPHIC VIEW", "onDraw called");

    }
}
