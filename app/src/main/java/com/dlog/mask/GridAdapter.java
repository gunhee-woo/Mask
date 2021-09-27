package com.dlog.mask;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {
    ArrayList<MarkerData> markerData = new ArrayList<MarkerData>();
    Context context;
    ImageView imgMarker;
    TextView textMarker;
    LayoutInflater inf;
    int layout;

    public GridAdapter(Context context, int layout) {
        this.context = context;
        this.layout = layout;
        inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return markerData.size();
    }

    @Override
    public Object getItem(int position) {
        return markerData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addMarkerData(MarkerData data) {
        markerData.add(data);
    }

    public void addMarkerData(String markerText, int markerImage) {
        MarkerData data = new MarkerData(markerText, markerImage);
        markerData.add(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null)
            convertView = inf.inflate(layout, null);
        imgMarker = (ImageView)convertView.findViewById(R.id.imgMarker);
        textMarker = (TextView)convertView.findViewById(R.id.countMarker);
        imgMarker.setImageResource(markerData.get(position).getMarkerImage());
        textMarker.setText(markerData.get(position).getMarkerText());
        return convertView;
    }
}
