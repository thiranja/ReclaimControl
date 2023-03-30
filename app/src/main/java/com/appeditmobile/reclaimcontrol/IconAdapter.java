package com.appeditmobile.reclaimcontrol;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class IconAdapter extends BaseAdapter {

    private Context mContext;
    private List<ApplicationInfo> mAppInfos;
    private List<ApplicationInfo> mFilteredData;
    private PackageManager mPackageManager;

    public IconAdapter(Context context, List<ApplicationInfo> iconItems, PackageManager packageManager) {
        mContext = context;
        mFilteredData = iconItems;
        mAppInfos = new ArrayList<>(iconItems);
        mPackageManager = packageManager;
    }

    @Override
    public int getCount() {
        return mFilteredData.size();
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return mFilteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.iconGI);
            holder.name = convertView.findViewById(R.id.labelGI);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        ApplicationInfo item = mFilteredData.get(position);
        holder.icon.setImageDrawable(item.loadIcon(mPackageManager));
        holder.name.setText(item.loadLabel(mPackageManager));

        return convertView;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView name;
    }

    public void filter(String searchText) {
        mFilteredData.clear();
        for (ApplicationInfo item : mAppInfos) {
            if (item.loadLabel(mPackageManager).toString().toLowerCase().contains(searchText.toLowerCase())) {
                mFilteredData.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public void removeFromBackup(ApplicationInfo app){
        mAppInfos.remove(app);
    }

    public void addToBackup(ApplicationInfo app){
        mAppInfos.add(app);
    }

}
