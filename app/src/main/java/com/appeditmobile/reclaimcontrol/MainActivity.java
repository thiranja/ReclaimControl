package com.appeditmobile.reclaimcontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.appeditmobile.reclaimcontrol.database.BlockedApp;
import com.appeditmobile.reclaimcontrol.database.BlockedAppDatabaseHelper;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_USAGE_ACCESS = 1;

    PackageManager packageManager;
    AppListMaker maker;
    private int REQUEST_CODE = 1;

    private BlockedAppDatabaseHelper databaseHelper;
    private GridView blockedAppGV;
    private BlockedIconAdapter baIconAdapter;
    private GridView addAppGV;
    private IconAdapter addIconAdapter;
//    This is the list that hold apps on the blocked app database
    private ArrayList<ApplicationInfo> appInfos;
    private ArrayList<Integer> blockTypes;
//    this holds all other apps
    private ArrayList<ApplicationInfo> appList;
    private EditText searchET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new BlockedAppDatabaseHelper(this);
        blockedAppGV = findViewById(R.id.blokedAppGV);
        addAppGV =findViewById(R.id.addAppGV);
        searchET =findViewById(R.id.search_et);

        Intent appLaunchMonitor = new Intent(this, AppLaunchService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(appLaunchMonitor);
        }else{
            startService(appLaunchMonitor);
        }


        // Register LockReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_LAUNCHER);

        packageManager = getPackageManager();

        findViewById(R.id.launchBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(MainActivity.this, "Launch Clicked", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(MainActivity.this, WarningActivity.class);
//                startActivity(intent);
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                intent.putExtra("unique_id", System.currentTimeMillis());
//                sendBroadcast(intent);
//                getDisplayOverOtherAppPermission();

            }
        });

        LinearLayout permissionLayout = findViewById(R.id.permission_layout);
        if (isPermissionGranted()){
            permissionLayout.setVisibility(View.GONE);
        }else{
            permissionLayout.setVisibility(View.VISIBLE);
        }

        Button permissionBtn = findViewById(R.id.grant_permission_btn);
        permissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grantPermission();
            }
        });


        maker = new AppListMaker();
        maker.execute();

        // Define the OnItemClickListener for the GridView
        blockedAppGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle the click on the item in the GridView
                String packageName = appInfos.get(position).packageName;

                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.blocked_app_popup, popupMenu.getMenu());


                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.blo_release:
                                // Handle menu item 1 click
                                databaseHelper.deleteBlockedApp(packageName);
                                appList.add(appInfos.get(position));
                                addIconAdapter.addToBackup(appInfos.get(position));
                                appInfos.remove(position);
                                baIconAdapter.notifyDataSetChanged();
                                addIconAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.blo_check_usage:
                                // Handle menu item 2 click
                                // check for permission
                                AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                                int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
                                boolean granted = mode == AppOpsManager.MODE_ALLOWED;

                                if (granted){
                                    Toast.makeText(MainActivity.this, "Have permission", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(MainActivity.this, "No permission", Toast.LENGTH_SHORT).show();
                                    showUsageAccessPopup();
                                }

//                                Intent intent = new Intent();
//                                intent.setClassName("com.google.android.apps.wellbeing", "com.google.android.apps.wellbeing.dashboard.DashboardActivity");
//                                intent.putExtra("packageName", "com.google.android.youtube");
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
                                return true;
                            case R.id.blo_launch:
                                // Handle menu item 3 click
                                Intent lauchIntent = packageManager.getLaunchIntentForPackage(packageName);
                                try{
                                    startActivity(lauchIntent);
                                }catch (Exception e){
                                    Toast.makeText(MainActivity.this, "Sorry can't launch this app", Toast.LENGTH_SHORT).show();
                                }

                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });

        addAppGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String packageName = appList.get(position).packageName;

                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.add_app_popup, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();// Handle menu item 1 click

                        if (itemId == R.id.add_warn) {
                            // Handle menu item 1 click
                            BlockedApp ba = databaseHelper.getBlockedApp(packageName);
                            if (ba != null) {
                                return false;
                            }
                            // Get the current time in milliseconds
                            long currentTimeMillis = System.currentTimeMillis();
                            java.util.Date currentDate = new java.util.Date(currentTimeMillis);
                            Date sqlDate = new Date(currentDate.getTime());

                            ba = new BlockedApp(packageName, 0, sqlDate, BlockedApp.BLOCK_TYPE_WARN, 0);
                            databaseHelper.insertBlockedApp(ba);
                            appInfos.add(appList.get(position));
                            addIconAdapter.removeFromBackup(appList.get(position));
                            appList.remove(appList.get(position));
                            blockTypes.add(BlockedApp.BLOCK_TYPE_WARN);
                            baIconAdapter.notifyDataSetChanged();
                            addIconAdapter.notifyDataSetChanged();
                            return true;
                        } else if (itemId == R.id.add_block) {
                            // Handle menu item 1 click
                            BlockedApp ba = databaseHelper.getBlockedApp(packageName);
                            if (ba != null) {
                                return false;
                            }
                            // Get the current time in milliseconds
                            long currentTimeMillis = System.currentTimeMillis();
                            java.util.Date currentDate = new java.util.Date(currentTimeMillis);
                            Date sqlDate = new Date(currentDate.getTime());

                            ba = new BlockedApp(packageName, 0, sqlDate, BlockedApp.BLOCK_TYPE_BLOCK, 0);
                            databaseHelper.insertBlockedApp(ba);
                            appInfos.add(appList.get(position));
                            addIconAdapter.removeFromBackup(appList.get(position));
                            appList.remove(appList.get(position));
                            blockTypes.add(BlockedApp.BLOCK_TYPE_BLOCK);
                            baIconAdapter.notifyDataSetChanged();
                            addIconAdapter.notifyDataSetChanged();
                            return true;
                        } else if (itemId == R.id.add_lock) {
                            // Handle menu item 1 click
                            BlockedApp ba = databaseHelper.getBlockedApp(packageName);
                            if (ba != null) {
                                return false;
                            }
                            // Get the current time in milliseconds
                            long currentTimeMillis = System.currentTimeMillis();
                            java.util.Date currentDate = new java.util.Date(currentTimeMillis);
                            Date sqlDate = new Date(currentDate.getTime());

                            ba = new BlockedApp(packageName, 0, sqlDate, BlockedApp.BLOCK_TYPE_LOCK, 0);
                            databaseHelper.insertBlockedApp(ba);
                            appInfos.add(appList.get(position));
                            addIconAdapter.removeFromBackup(appList.get(position));
                            appList.remove(appList.get(position));
                            blockTypes.add(BlockedApp.BLOCK_TYPE_LOCK);
                            baIconAdapter.notifyDataSetChanged();
                            addIconAdapter.notifyDataSetChanged();
                            return true;
                        } else if (itemId == R.id.add_usage) {
                            // check for permission
                            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
                            boolean granted = mode == AppOpsManager.MODE_ALLOWED;

                            if (granted) {
                                Toast.makeText(MainActivity.this, "Have permission", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "No permission", Toast.LENGTH_SHORT).show();
                                showUsageAccessPopup();
                            }

//                                Intent intent = new Intent();
//                                intent.setClassName("com.google.android.apps.wellbeing", "com.google.android.apps.wellbeing.dashboard.DashboardActivity");
//                                intent.putExtra("packageName", "com.google.android.youtube");
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
                            return true;
                        } else if (itemId == R.id.add_launch) {// Handle menu item 3 click
                            Intent lauchIntent = packageManager.getLaunchIntentForPackage(packageName);
                            try {
                                startActivity(lauchIntent);
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Sorry can't launch this app", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });
    }

    private void getDisplayOverOtherAppPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            // You already have permission
        }
    }

    private void showUsageAccessPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_usage_access, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);


        Button grantButton = popupView.findViewById(R.id.grant_usage_btn);
        grantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivityForResult(intent, REQUEST_USAGE_ACCESS);
                popupWindow.dismiss();
            }
        });

        Button closeButton = popupView.findViewById(R.id.close_usage_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private boolean isPermissionGranted(){
        String[] permissions = {Manifest.permission.GET_TASKS, Manifest.permission.REORDER_TASKS};
        if (ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void grantPermission(){
        String[] permissions = {Manifest.permission.GET_TASKS, Manifest.permission.REORDER_TASKS};

        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission was denied
                Toast.makeText(this, "denined", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AppListMaker extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<ApplicationInfo> apps = (ArrayList<ApplicationInfo>) packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            appList = new ArrayList<>();

            BlockedApp[] blockedApps = databaseHelper.getAllBlockedApps();
            Log.d("GRIDE", "size of blocked app array is " + Integer.toString(blockedApps.length));
            appInfos = new ArrayList<>();
            blockTypes = new ArrayList<Integer>();

            for (ApplicationInfo app : apps){
                if (packageManager.getLaunchIntentForPackage(app.packageName) != null ){
                    appList.add(app);

                    String packageName = app.packageName;
                    Log.d("GRID", "appInfo package name is " + packageName);
                    for (BlockedApp ba : blockedApps){
                        String pn = ba.getPackageName();
                        //Log.d("GRID", "BlockedApp package name is " + pn);
                        if (pn.trim().equals(packageName.trim())){
                            Log.d("GRIDX", "If reached");
                            appInfos.add(app);
//                            remove the app from application list
                            appList.remove(app);
                            blockTypes.add(ba.getBlockType());

                        }
                    }

                }
            }
            Log.d("GRID", "grid size is "+ Integer.toString(appInfos.size()));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            addIconAdapter = new IconAdapter(MainActivity.this, appList,packageManager);
            baIconAdapter = new BlockedIconAdapter(MainActivity.this,appInfos, blockTypes,packageManager);
            addAppGV.setAdapter(addIconAdapter);
            //adapter.notifyDataSetChanged();

            blockedAppGV.setAdapter(baIconAdapter);
            //baIconAdapter.notifyDataSetChanged();

//            setMFAData();
//            allAppsSv.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
//
//
//                @Override
//                public boolean onQueryTextSubmit(String query) {
//                    return false;
//                }
//
//                @Override
//                public boolean onQueryTextChange(String newText) {
////                    adapter.filter(newText);
//                    return true;
//                }
//
//
//            });

            searchET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String searchPhrase = charSequence.toString();
                    Log.d("SEARCH",searchPhrase);
                    addIconAdapter.filter(searchPhrase);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }
}