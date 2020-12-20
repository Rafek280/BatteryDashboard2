package de.frauas.informatik.batterydashboard.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.batterydashboard.R;

import java.util.HashMap;
import java.util.List;

class ConfigWindow extends ConstraintLayout implements IOverlay {
    private ViewGroup content;

    // constructors
    public ConfigWindow(Context context) {
        super(context);
        initializeViews(context);
    }
    public ConfigWindow(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initializeViews(context);
    }
    public ConfigWindow(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        content = (ViewGroup) inflater.inflate(R.layout.config_window_layout, this);
    }

    void setTestText(String txt){
        /*TextView tv = content.findViewById(R.id.config_window_test_textview);
        tv.setText(txt);*/
    }

    @Override
    public ViewGroup getContent() {
        return content;
    }

    ImageButton getCloseBtn(){
        return content.findViewById(R.id.config_window_close_btn);
    }
}
