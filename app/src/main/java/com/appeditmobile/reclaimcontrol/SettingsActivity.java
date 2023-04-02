package com.appeditmobile.reclaimcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    // elements of app bar
    private ImageView optionIv;
    private ImageView abIv;
    private TextView abTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // seting the action bar actions
        optionIv = findViewById(R.id.action_bar_image);
        abIv = findViewById(R.id.action_bar_main_ic);
        abTv = findViewById(R.id.action_bar_tv);
        abIv.setImageResource(R.drawable.ic_outline_settings_32);
        abTv.setText("Settings");
        optionIv.setImageResource(R.drawable.ic_round_arrow_back_ios_30);
        optionIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingsActivity.super.onBackPressed();
            }
        });

    }
}