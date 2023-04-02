package com.appeditmobile.reclaimcontrol;

import static android.content.Context.WINDOW_SERVICE;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appeditmobile.reclaimcontrol.database.BlockedApp;
import com.appeditmobile.reclaimcontrol.database.BlockedAppDatabaseHelper;
import com.appeditmobile.reclaimcontrol.database.SharedPrefManager;
import com.takwolf.android.lock9.Lock9View;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Random;

public class WarningWindow {

    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private View myView;
    private String packageName;
    private Context mContext;
    private PackageManager pm;
    private BlockedAppDatabaseHelper dbHelper;
    private BlockedApp ba;
    private java.sql.Date sqlDate;
    private String correctPattern;
    boolean dateUpdateNeeded;

    WarningWindow(Context context, String packagename){
        mContext = context;
        packageName = packagename;
        pm = mContext.getPackageManager();
        dbHelper = new BlockedAppDatabaseHelper(mContext);
        ba = dbHelper.getBlockedApp(packageName);
        Date lastOpenDate = ba.getLastAccessedDate();

        // Get the current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();
        java.util.Date currentDate = new java.util.Date(currentTimeMillis);
        sqlDate = new java.sql.Date(currentDate.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString1 = sdf.format(lastOpenDate);
        String dateString2 = sdf.format(sqlDate);

        if (dateString1.equals(dateString2)) {
            // date1 and date2 represent the same day
            dateUpdateNeeded = true;
            ba.incrementAttemptCount();
        } else {
            // date1 and date2 represent different days
            ba.setLaunchCount(0);
            ba.setAttemptCount(1);
            dateUpdateNeeded = true;
        }

        wm = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.OPAQUE);
        }else{
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.OPAQUE);
        }

        myView = LayoutInflater.from(mContext).inflate(R.layout.activity_warning, null);


        // SET TOP ICONS
        ImageView appIcon = myView.findViewById(R.id.warning_app_icon);
        try {
            appIcon.setImageDrawable(pm.getApplicationIcon(packageName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Button exit = myView.findViewById(R.id.exit_button);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToHome();
                // informing the service that app is not launched
                Intent appLaunchMonitor = new Intent(mContext, AppLaunchService.class);
                appLaunchMonitor.putExtra("is_app_open",false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(appLaunchMonitor);
                }else{
                    mContext.startService(appLaunchMonitor);
                }
                // start exit warning activity as then app will no longer launch without purpose
                // it will enter the package name of our app so it will prevent recurent launch of block
                Intent exitWarningActivity = new Intent(mContext, ExitWarningActivity.class);
                exitWarningActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mContext.startActivity(exitWarningActivity);
                updateDatabase(false);
                wm.removeView(myView);

            }
        });

        ImageView blockTypeIcon = myView.findViewById(R.id.block_type_icon);
        TextView appName = myView.findViewById(R.id.warning_popup_app_name_tv);
        TextView appStatus = myView.findViewById(R.id.warning_popup_app_status);
        try {
            appName.setText( pm.getApplicationLabel(pm.getApplicationInfo(ba.getPackageName(),0)));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
//        get the block type
        if (ba.getBlockType() == BlockedApp.BLOCK_TYPE_WARN){
            blockTypeIcon.setImageResource(R.drawable.ic_baseline_warning_24);
            appStatus.setText(" is Addictive!");
            setUpWarningLayout();
        }else if(ba.getBlockType() == BlockedApp.BLOCK_TYPE_BLOCK ){
            blockTypeIcon.setImageResource(R.drawable.ic_baseline_block_24);
            appStatus.setText(" is Blocked!");
            setUpBlockLayout();
        }else {
            blockTypeIcon.setImageResource(R.drawable.ic_baseline_lock_24);
            appStatus.setText(" is Locked");
            setUpLockLayout();
        }





    }

    private void updateDatabase(boolean isOpening){
        if (isOpening){
            ba.incrementLaunchCount();
            if (dateUpdateNeeded){
                ba.setLastAccessedDate(sqlDate);
            }
        }else{
            if (dateUpdateNeeded) {
                ba.setLaunchCount(0);
            }
        }
        ba.incrementAttemptCount();
        dbHelper.updateBlockedApp(ba);
    }

    public void showWindow(){
        wm.addView(myView,params);
    }

    private void launchApp(){
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void navigateToHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void setUpWarningLayout(){
        TextView usageText = myView.findViewById(R.id.warning_usage_text);
        usageText.setText("You have opened this app " + Integer.toString( ba.getLaunchCount()) + " times today");

        TextView warningText = myView.findViewById(R.id.warnig_text);
        String[] warningMessages = {
                "Let's be intentional: did you intentionally open this app, or was it accidental?",
                "Pause and reflect: are you opening this app out of habit or by mistake?",
                "Just a friendly reminder: did you really mean to open this app?",
                "Breaking a habit starts with awareness. Did you intend to open this app?",
                "Did you open this app out of habit or by accident?"
        };

        warningText.setText(warningMessages[new Random().nextInt(5)]);

        Button open = myView.findViewById(R.id.proceed_button);
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //launchApp();
                // informing the service that app is launched
                Intent appLaunchMonitor = new Intent(mContext, AppLaunchService.class);
                appLaunchMonitor.putExtra("is_app_open",true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(appLaunchMonitor);
                }else{
                    mContext.startService(appLaunchMonitor);
                }

                updateDatabase(true);
                wm.removeView(myView);
            }
        });
    }

    private void setUpBlockLayout(){
        TextView usageText = myView.findViewById(R.id.warning_usage_text);
        usageText.setText("You have attempted to launch this app " + Integer.toString( ba.getAttemptCount()) + " times today");
        // increase padding of the warning view
        myView.findViewById(R.id.warning_popup_warning_ly).setPadding(0,30,0,250);

        TextView warningText = myView.findViewById(R.id.warnig_text);
        String[] warningMessages = {
                "Let's be intentional: did you intentionally open this app, or was it accidental?",
                "Pause and reflect: are you opening this app out of habit or by mistake?",
                "Just a friendly reminder: did you really mean to open this app?",
                "Breaking a habit starts with awareness. Did you intend to open this app?",
                "Did you open this app out of habit or by accident?"
        };

        warningText.setText(warningMessages[new Random().nextInt(5)]);

        Button open = myView.findViewById(R.id.proceed_button);
        open.setVisibility(View.GONE);
    }

    private void setUpLockLayout(){
        // first remove the warning and block layout
        myView.findViewById(R.id.warning_popup_warning_ly).setVisibility(View.GONE);
        // enable the lock view
        myView.findViewById(R.id.warning_popup_lock_ly).setVisibility(View.VISIBLE);
//        disabling the open button
        Button open = myView.findViewById(R.id.proceed_button);
        open.setVisibility(View.GONE);

        SharedPreferences sp = mContext.getSharedPreferences(SharedPrefManager.FILE_NAME, Context.MODE_PRIVATE);

        correctPattern = sp.getString(SharedPrefManager.LOCK_PASSWORD_STRING, "1234");

//        Lock9View patternLock = myView.findViewById(R.id.lock9_view);
//        patternLock.setWillNotDraw(true);
//        patternLock.setGestureCallback(new Lock9View.GestureCallback() {
//            @Override
//            public void onNodeConnected(@androidx.annotation.NonNull int[] numbers) {
//
//            }
//
//            @Override
//            public void onGestureFinished(@androidx.annotation.NonNull int[] numbers) {
//                StringBuilder sb = new StringBuilder();
//                for (int number : numbers){
//                    sb.append(number);
//                }
//
//                Log.d("PATTERNLOCK","node connected password is " + sb.toString());
//                if (correctPattern.equals(sb.toString())){
//                    Log.d("PATTERNLOCK","pin correct opening app");
//                    //launchApp();
//                    updateDatabase(true);
//                    wm.removeView(myView);
//                }else{
//                    Log.d("PATTERNLOCK","pin incorrect");
//                }
//            }
//        });



//        KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
//        Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(null, null);
//        if (intent != null) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            mContext.startActivity(intent);
//        }
//
//        wm.removeView(myView);

    }
}
