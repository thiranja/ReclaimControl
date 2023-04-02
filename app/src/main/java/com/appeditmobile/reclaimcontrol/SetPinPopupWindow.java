package com.appeditmobile.reclaimcontrol;

import static android.content.Context.WINDOW_SERVICE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.appeditmobile.reclaimcontrol.database.SharedPrefManager;
import com.hanks.passcodeview.PasscodeView;
import com.takwolf.android.lock9.Lock9View;

import java.util.List;

public class SetPinPopupWindow {

    private static int REENTER_PASSWORD_PARSE = 0;
    private static int ENTER_NEW_PASSWORD_PARSE = 1;
    private static int CONFIRM_PASSWORD_PARSE = 2;

    private View popupView;
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int windowParse;
    private String lockPassword;
    private String enteredPassword;
    private String confirmPassword;

    public SetPinPopupWindow(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.set_up_pin, null);

        wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
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
        Button exit = popupView.findViewById(R.id.exit_button);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wm.removeView(popupView);

            }
        });
//        Lock9View patternLock = popupView.findViewById(R.id.lock9_view);
//        patternLock.setWillNotDraw(true);
//        PatternLockView mPatternLockView = (PatternLockView) popupView.findViewById(R.id.pattern_lock_view);

        TextView indicatorText = popupView.findViewById(R.id.setpin_indicator_text);

        sharedPreferences = context.getSharedPreferences(SharedPrefManager.FILE_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        boolean isLockEnabled = sharedPreferences.getBoolean(SharedPrefManager.IS_LOCK_ENABLE_BOOLEAN, false);
        lockPassword = "1234";
        if (isLockEnabled){
            lockPassword = sharedPreferences.getString(SharedPrefManager.LOCK_PASSWORD_STRING,"1234");
            indicatorText.setText("Enter the previous Pattern");
            windowParse = REENTER_PASSWORD_PARSE;
        }else{
            windowParse = ENTER_NEW_PASSWORD_PARSE;
            indicatorText.setText("Enter new Pattern");
        }

//        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
//            @Override
//            public void onStarted() {
//
//            }
//
//            @Override
//            public void onProgress(List<PatternLockView.Dot> progressPattern) {
//
//            }
//
//            @Override
//            public void onComplete(List<PatternLockView.Dot> dotPattern) {
//
//                String pattern = PatternLockUtils.patternToString(mPatternLockView, dotPattern);
//                Log.d("PATTERNLOCK", "the drawn pattern is " + pattern);
//                if (windowParse == REENTER_PASSWORD_PARSE){
//                    if (pattern.equals(lockPassword)){
//                        // pasaword entered correctly
//                        indicatorText.setText("Enter new Pattern");
//                        windowParse = ENTER_NEW_PASSWORD_PARSE;
//                    }else {
//                        indicatorText.setText("Wrong! Try again!");
//                    }
//                }else if (windowParse == ENTER_NEW_PASSWORD_PARSE){
//                    enteredPassword = pattern;
//                    indicatorText.setText("Confirm new Pattern!");
//                    windowParse = CONFIRM_PASSWORD_PARSE;
//                }else if ( windowParse == CONFIRM_PASSWORD_PARSE){
//                    confirmPassword = pattern;
//                    if (enteredPassword.equals(confirmPassword)){
//                        // successfully changed the password
//                        editor.putString(SharedPrefManager.LOCK_PASSWORD_STRING,confirmPassword);
//                        editor.putBoolean(SharedPrefManager.IS_LOCK_ENABLE_BOOLEAN, true);
//                        editor.apply();
//                        Toast.makeText(context, "Pattern Changed", Toast.LENGTH_SHORT).show();
//                        wm.removeView(popupView);
//                    }else {
//                        indicatorText.setText("Pattern mismatch! Try again!");
//                        windowParse = ENTER_NEW_PASSWORD_PARSE;
//                    }
//                }
//            }
//
//            @Override
//            public void onCleared() {
//
//            }
//        });

//        patternLock.setGestureCallback(new Lock9View.GestureCallback() {
//            @Override
//            public void onNodeConnected(int[] numbers) {
//
//            }
//
//            @Override
//            public void onGestureFinished(int[] numbers) {
//                StringBuilder sb = new StringBuilder();
//                for (int number : numbers){
//                    sb.append(number);
//                }
//                String pattern = sb.toString();
//
//                if (windowParse == REENTER_PASSWORD_PARSE){
//                    if (pattern.equals(lockPassword)){
//                        // pasaword entered correctly
//                        indicatorText.setText("Enter new Pattern");
//                        windowParse = ENTER_NEW_PASSWORD_PARSE;
//                    }else {
//                        indicatorText.setText("Wrong! Try again!");
//                    }
//                }else if (windowParse == ENTER_NEW_PASSWORD_PARSE){
//                    enteredPassword = pattern;
//                    indicatorText.setText("Confirm new Pattern!");
//                    windowParse = CONFIRM_PASSWORD_PARSE;
//                }else if ( windowParse == CONFIRM_PASSWORD_PARSE){
//                    confirmPassword = pattern;
//                    if (enteredPassword.equals(confirmPassword)){
//                        // successfully changed the password
//                        editor.putString(SharedPrefManager.LOCK_PASSWORD_STRING,confirmPassword);
//                        editor.putBoolean(SharedPrefManager.IS_LOCK_ENABLE_BOOLEAN, true);
//                        editor.apply();
//                        Toast.makeText(context, "Pattern Changed", Toast.LENGTH_SHORT).show();
//                        wm.removeView(popupView);
//                    }else {
//                        indicatorText.setText("Pattern mismatch! Try again!");
//                        windowParse = ENTER_NEW_PASSWORD_PARSE;
//                    }
//                }
//            }
//        });

//        PasscodeView passcodeView = popupView.findViewById(R.id.passcode_view);
//        passcodeView.setLocalPasscode("1234");
//        passcodeView.setListener(new PasscodeView.PasscodeViewListener() {
//            @Override
//            public void onFail() {
//
//            }
//
//            @Override
//            public void onSuccess(String number) {
//
//            }
//        });
//        passcodeView.setPasscodeEntryListener(new PasscodeView.PasscodeViewListener() {
//            @Override
//            public void onFail() {
//                // handle incorrect passcode entry
//            }
//
//            @Override
//            public void onSuccess(String passcode) {
//                // handle correct passcode entry
//            }
//        });
    }


    public void showWindow(){
        wm.addView(popupView,params);
    }
}
