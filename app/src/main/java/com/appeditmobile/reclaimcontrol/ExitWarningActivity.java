package com.appeditmobile.reclaimcontrol;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.codemybrainsout.ratingdialog.RatingDialog;


public class ExitWarningActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit_warning);


    }

    @Override
    protected void onResume() {
        super.onResume();

//        RatingDialog ratingDialog = new RatingDialog.Builder(this)
//                .icon(R.mipmap.ic_launcher)
//                .threshold(3)
//                .playstoreUrl("YOUR_URL")
//                .build();
//
//        ratingDialog.show();
        finishActivityAndNavigateHome();
    }

    private void finishActivityAndNavigateHome(){
        // navigate to home
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}