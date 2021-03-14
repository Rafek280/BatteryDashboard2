package de.frauas.informatik.batterydashboard.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.batterydashboard.R;

/**
 * SAME FUNCTIONALITY AS DASHBOARD, displays second frame for statistics
 *
 * Functions the same as the normal dashboard.
 *
 * More Detail on the functionality of this class are found in Dashboard.java
 */

public class StatistikDashboard extends ConstraintLayout implements IOverlay {
    private ViewGroup content;

    public StatistikDashboard(Context context) {
        super(context);
        initializeViews(context);
    }
    public StatistikDashboard(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initializeViews(context);
    }
    public StatistikDashboard(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        content = (ViewGroup) inflater.inflate(R.layout.statdash, this);
    }

    @Override
    public ViewGroup getContent() {
        return content;
    }

    public AbsoluteLayout getDashboardFrame(){
        return content.findViewById(R.id.dashboardFrameStatistiks);
    }

    Button getExitButton(){
        return content.findViewById(R.id.ExitButtonStatistiks_ID);
    }

}
