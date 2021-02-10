package com.example.tobinornottobin2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ClimateChange extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_climate_change);

        //button methods to move onto next page
         ImageButton btnLogin = (ImageButton) findViewById(R.id.btnLogin4);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
       ImageButton btnScan = (ImageButton) findViewById(R.id.btnBackScan2);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveClimatePage = new Intent(com.example.tobinornottobin2.ClimateChange.this, ScanItem.class);
                startActivity(moveClimatePage);
            }
        });

       ImageButton btnFacts= (ImageButton) findViewById(R.id.btnFunfacts3);
        btnFacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveFactPage = new Intent(com.example.tobinornottobin2.ClimateChange.this, FunFacts.class);
                startActivity(moveFactPage);
            }
        });

    }
}