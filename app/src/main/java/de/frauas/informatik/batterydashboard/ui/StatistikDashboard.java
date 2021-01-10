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
 * This class is the parent view of the dashboard frame, which is an AbsoluteLayout (as long as
 * it's possible to use this deprecated class, that fits our requirements with draggable views best ;) )
 * A dashboard object belongs to the UiService class and is used to retrieve the dashboard frame
 * to then manipulate its child views.
 * This class implements Overlay interface. See responsibilities to learn why ;)
 * Only UiService should be able to manipulate dashboard. To e.g. trigger UI updates from other classes
 * request an update by broadcasting, as implemented in DashboardManager:
 * @see GaugeManager
 * or learn how to do that here: https://www.websmithing.com/2011/02/01/how-to-update-the-ui-in-an-android-activity-using-data-from-a-background-service/
 *
 * Responsibilities:
 * - inflate dashboard layout (with background image, buttons etc.)
 * - get elements from the content – This is what you would do by *findViewById* if you had an Activity. It's way more
 *   complicated with this service architecture. Why we still need to do it this way is explained in the UiService class:
 *   @see UiService
 *   @see IOverlay for HOW TO FIND VIEWS BY ID!! ;)
 *
 * @author Julia
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
        return content.findViewById(R.id.dashboardFrame);
    }

    Button getExitButton(){
        return content.findViewById(R.id.ExitButtonStatistiks_ID);
    }

}
