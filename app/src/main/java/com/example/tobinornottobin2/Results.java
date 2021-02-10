
package com.example.tobinornottobin2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class Results extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        //button methods to move onto next page
 ImageButton buttonLogin = (ImageButton) findViewById(R.id.btnLogin2);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
      ImageButton btnFacts = (ImageButton) findViewById(R.id.btnFunfacts2);
        btnFacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveFactsPage = new Intent(com.example.tobinornottobin2.Results.this, FunFacts.class);
                startActivity(moveFactsPage);
            }
        });

   ImageButton btnClimate= (ImageButton) findViewById(R.id.btnClimateChange2);
        btnClimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveClimatePage = new Intent(com.example.tobinornottobin2.Results.this, ClimateChange.class);
                startActivity(moveClimatePage);
            }
        });
    }
}