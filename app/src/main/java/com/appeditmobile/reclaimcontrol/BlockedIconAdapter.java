package com.appeditmobile.reclaimcontrol;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appeditmobile.reclaimcontrol.database.BlockedApp;

import java.util.ArrayList;
import java.util.List;

public class BlockedIconAdapter extends BaseAdapter {

    private Context mContext;
    private List<ApplicationInfo> mAppInfos;
    private List<ApplicationInfo> mFilteredData;
    private PackageManager mPackageManager;
    private List<Integer> mBlockType;

    public BlockedIconAdapter(Context context, List<ApplicationInfo> iconItems, List<Integer> blockType, PackageManager packageManager) {
        mContext = context;
        mFilteredData = iconItems;
        mAppInfos = new ArrayList<>(iconItems);
        mPackageManager = packageManager;
        mBlockType = blockType;
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
            holder.blockTypeIcon = convertView.findViewById(R.id.block_type_iconGI);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        ApplicationInfo item = mFilteredData.get(position);
        holder.icon.setImageDrawable(item.loadIcon(mPackageManager));
        holder.name.setText(item.loadLabel(mPackageManager));
        holder.blockTypeIcon.setVisibility(View.VISIBLE);
        if (mBlockType.get(position) == BlockedApp.BLOCK_TYPE_WARN){
            holder.blockTypeIcon.setImageResource(R.drawable.ic_baseline_warning_24);
        }else if (mBlockType.get(position) == BlockedApp.BLOCK_TYPE_BLOCK){
            holder.blockTypeIcon.setImageResource(R.drawable.ic_baseline_block_24);
        }else if (mBlockType.get(position) == BlockedApp.BLOCK_TYPE_LOCK){
            holder.blockTypeIcon.setImageResource(R.drawable.ic_baseline_lock_24);
        }else{
            holder.blockTypeIcon.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView icon;
        ImageView blockTypeIcon;
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

}
