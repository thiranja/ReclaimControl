package com.appeditmobile.reclaimcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InfoActivity extends AppCompatActivity {

    private TextView infoTv;

    // elements of app bar
    private ImageView optionIv;
    private ImageView abIv;
    private TextView abTv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        infoTv = findViewById(R.id.info_tv);

        // seting the action bar actions
        optionIv = findViewById(R.id.action_bar_image);
        abIv = findViewById(R.id.action_bar_main_ic);
        abTv = findViewById(R.id.action_bar_tv);

        optionIv.setImageResource(R.drawable.ic_round_arrow_back_ios_30);
        optionIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InfoActivity.super.onBackPressed();
            }
        });

        Intent intent = getIntent();
        String infoType = intent.getStringExtra("info_type");
        if (infoType.equals("help")) {
            abTv.setText("Help");
            abIv.setImageResource(R.drawable.ic_outline_help_outline_32);
            infoTv.setText(Html.fromHtml(readHtmlFile(true)));
        }else{
            abTv.setText("About");
            abIv.setImageResource(R.drawable.ic_outline_about_32);
            infoTv.setText(Html.fromHtml(readHtmlFile(false)));
        }
    }

    private String readHtmlFile(boolean isHelp) {
        StringBuilder buf = new StringBuilder();
        try {
            InputStream html;
            if (isHelp) {
                html = getResources().openRawResource(R.raw.help);
            }else{
                html = getResources().openRawResource(R.raw.about);
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(html, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
        } catch (Exception e) {
            Log.e("HtmlFile", "Error reading HTML file", e);
        }
        return buf.toString();
    }
}