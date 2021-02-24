package com.example.tobinornottobin2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public  class FunFacts extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fun_facts);


        ImageButton btnLogin = (ImageButton) findViewById(R.id.btnLogin3);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //button methods to move onto next page

 ImageButton btnScan = (ImageButton) findViewById(R.id.btnBackScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveScanPage = new Intent(com.example.tobinornottobin2.FunFacts.this, ScanItem.class);
                startActivity(moveScanPage);
            }
        });

        @SuppressLint("WrongViewCast") Button buttonFacts= (Button) findViewById(R.id.btnClimateChange3);
        buttonFacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveFactPage = new Intent(com.example.tobinornottobin2.FunFacts.this, ClimateChange.class);
                startActivity(moveFactPage);
            }
        });
    }
}