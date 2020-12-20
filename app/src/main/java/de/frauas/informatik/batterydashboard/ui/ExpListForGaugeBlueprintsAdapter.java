package de.frauas.informatik.batterydashboard.ui;

import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.batterydashboard.R;

/**
 * This is a custom ExpandableListAdapter to make the list of Gauge(Blueprints) in the add window. </br>
 * Every child list element contains the string gotten by gaugeBlueprint.toString() and a button
 * that calls the gaugeManager to create a new gauge from the passed blueprint. </br>
 * The ExpListGaugeDataProvider provides the data needed. </br>
 * >> You will most likely not have to change this class, but rather the provider or the layout files. </br>
 * For layout see the xml files list_item and list_group in res/layout.
 * @see <a href="https://developer.android.com/reference/android/widget/ExpandableListAdapter"> android documentation </a>
 * @see ExpListGaugeDataProvider
 * @see <a href="https://www.journaldev.com/9942/android-expandablelistview-example-tutorial">Tutorial I used :)</a>
 * @author filzinge@stud.fra-uas.de
 */

public class ExpListForGaugeBlueprintsAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> expandableListTitle;
    private HashMap<String, List<GaugeBlueprint>> expandableListDetail;
    private GaugeManager gaugeManager = GaugeManager.getInstance();

    public ExpListForGaugeBlueprintsAdapter(Context context, List<String> expandableListTitle, HashMap<String, List<GaugeBlueprint>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = getChild(listPosition, expandedListPosition).toString();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, parent, false);
        }
        TextView expandedListTextView = (TextView) convertView.findViewById(R.id.expandedListItem);
        expandedListTextView.setText(expandedListText);
        ImageButton expandedListAddButton = (ImageButton) convertView.findViewById(R.id.expandedListAddButton);
        expandedListAddButton.setOnClickListener( v -> {
            GaugeBlueprint gaugeBlueprint = (GaugeBlueprint) getChild(listPosition, expandedListPosition);
            gaugeManager.addGaugeToCurrent(context, gaugeBlueprint);
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, parent, false);
        }
        TextView listTitleTextView = convertView.findViewById(R.id.listTitle);
        listTitleTextView.setText(listTitle);
        convertView.findViewById(R.id.ivGroupIndicator).setSelected(isExpanded);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
