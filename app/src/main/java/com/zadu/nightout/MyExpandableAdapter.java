package com.zadu.nightout;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by kasmus on 4/21/15.
 */
public class MyExpandableAdapter extends BaseExpandableListAdapter {
    // TODO: REPLACE ME WITH A SIMPLECURSOR ADAPTER, use a textview to toggle collapse
    private Activity activity;
    private ArrayList<Object> childtems;
    private LayoutInflater inflater;
    private ArrayList<String> parentItems, child;

    public MyExpandableAdapter(ArrayList<String> parents, ArrayList<Object> childern) {
        this.parentItems = parents;
        this.childtems = childern;
    }

    public void setInflater(LayoutInflater inflater, Activity activity) {
        this.inflater = inflater;
        this.activity = activity;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        child = (ArrayList<String>) childtems.get(groupPosition);

        CheckedTextView checkedTextView = null;
        TextView textView = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_contact, null);
        }

        checkedTextView = (CheckedTextView) convertView.findViewById(R.id.contactCheckedTextView);
        checkedTextView.setText(child.get(childPosition));
        textView = (TextView) convertView.findViewById(R.id.contactDescriptionTextView);
        textView.setText("(555)-555-5555");

        checkedTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CheckedTextView check = (CheckedTextView) view.findViewById(R.id.contactCheckedTextView);
                Toast.makeText(activity, child.get(childPosition),
                        Toast.LENGTH_SHORT).show();
                check.setChecked(!check.isChecked());
            }
        });

        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
        }

        ((TextView) convertView).setText(parentItems.get(groupPosition));

        return convertView;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return ((ArrayList<String>) childtems.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public int getGroupCount() {
        return parentItems.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}
