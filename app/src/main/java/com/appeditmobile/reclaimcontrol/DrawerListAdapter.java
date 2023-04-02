package com.appeditmobile.reclaimcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerListAdapter extends ArrayAdapter<String> {

    private final int[] navIcons;

    public DrawerListAdapter(Context context, String[] navOptions, int[] navIcons) {
        super(context, R.layout.nav_item, navOptions);
        this.navIcons = navIcons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View navView = inflater.inflate(R.layout.nav_item, parent, false);

        ImageView navIcon = navView.findViewById(R.id.nav_item_icon);
        TextView navText = navView.findViewById(R.id.nav_item_text);

        navIcon.setImageResource(navIcons[position]);
        navText.setText(getItem(position));

        return navView;
    }
}
