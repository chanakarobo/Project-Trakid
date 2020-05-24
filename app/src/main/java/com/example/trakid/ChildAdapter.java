package com.example.trakid;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ChildAdapter extends ArrayAdapter<ChildObject> {

    private Activity context;
    private List<ChildObject>childList;

    public ChildAdapter(Activity context, List<ChildObject> childList) {
        super(context, R.layout.child_list, childList);
        this.context = context;
        this.childList = childList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=context.getLayoutInflater();

       View listViewItems=inflater.inflate(R.layout.child_list,null,true);

        TextView textName=listViewItems.findViewById(R.id.parent_name);

        ChildObject childObject=childList.get(position);

        textName.setText(childObject.getChildName());

        return listViewItems;
    }
}





