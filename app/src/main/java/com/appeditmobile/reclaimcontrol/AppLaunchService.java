package com.appeditmobile.reclaimcontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.appeditmobile.reclaimcontrol.database.BlockedApp;
import com.appeditmobile.reclaimcontrol.database.BlockedAppDatabaseHelper;

import java.util.List;

public class AppLaunchService extends Service {

    private static final long INTERVAL = 1000; // 1 second
    private static final long MAX_IDLE_TIME = 5 * 60 * 1000; // 5 minutes

    private UsageStatsManager mUsageStatsManager;
    private Handler mHandler;
    private Handler mMainThreadHandler;
    private boolean mIsMonitoring;
    private BlockedApp[] apps;
    private String finallyOpenedPackageName;

    public AppLaunchService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        }
        mHandler = new Handler();
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("app_channel", "App Service Channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BlockedAppDatabaseHelper dbHelper = new BlockedAppDatabaseHelper(getApplicationContext());
        apps = dbHelper.getAllBlockedApps();

        Notification notification = new NotificationCompat.Builder(this, "app_channel")
                .setContentTitle("Reclaim Control")
                .setContentText("Monitoring app usage!")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        startForeground(1, notification);

        if (!mIsMonitoring) {
            mIsMonitoring = true;
            mHandler.post(mMonitoringRunnable);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsMonitoring = false;
        mHandler.removeCallbacks(mMonitoringRunnable);
    }

    private Runnable mMonitoringRunnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            List<UsageStats> usageStatsList = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, currentTime - MAX_IDLE_TIME, currentTime);
            if (usageStatsList != null && !usageStatsList.isEmpty()) {
                UsageStats latestUsageStats = null;
                for (UsageStats usageStats : usageStatsList) {
                    if (latestUsageStats == null || usageStats.getLastTimeUsed() > latestUsageStats.getLastTimeUsed()) {
                        latestUsageStats = usageStats;
                    }
                }
                if (latestUsageStats != null) {
                    String packageName = latestUsageStats.getPackageName();
                    if (!(packageName.equals(finallyOpenedPackageName)) ) {
                        Log.d("AppLaunch",packageName);
                        finallyOpenedPackageName = packageName;
                        for (BlockedApp ba : apps) {
                            if (packageName.equals(ba.getPackageName())) {
                                // Launch app lock screen
                                startWarningActivity(packageName);

                            }
                        }
                    }
                }
            }
            mHandler.postDelayed(this, INTERVAL);
        }
    };

    private void startWarningActivity(String packageName){
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("AppLaunch", "Matching packages found");
//                Intent lockIntent = new Intent(getApplicationContext(), WarningActivity.class);
//                lockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                startActivity(lockIntent);
                WarningWindow window = null;
                window = new WarningWindow(getApplicationContext(),packageName);
                window.showWindow();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}