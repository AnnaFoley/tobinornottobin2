
package com.example.tobinornottobin2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tobinornottobin2.ObjectDetection.ObjectDetection.Detector;

public abstract class Results extends AppCompatActivity  {

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



    }
}