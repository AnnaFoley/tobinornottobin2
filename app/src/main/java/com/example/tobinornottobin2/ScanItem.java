package com.example.tobinornottobin2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import com.example.tobinornottobin2.ObjectDetection.ObjectDetection.CameraActivity;
import com.example.tobinornottobin2.ObjectDetection.ObjectDetection.Detector;
import com.example.tobinornottobin2.ObjectDetection.ObjectDetection.DetectorActivity;
import com.example.tobinornottobin2.ObjectDetection.ObjectDetection.ScanActivity;


public  class ScanItem extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanitem);

        // Write a message to the database
        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        //DatabaseReference myRef = database.getReference("message");
        //myRef.setValue("Hello, World!");
        // Read from the database
        //myRef.addValueEventListener(new ValueEventListener() {
         //   @Override
           // public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
            //    String value = dataSnapshot.getValue(String.class);
             //   Log.d(TAG , "Value is: " + value);
            //}

            //@Override
            //public void onCancelled(DatabaseError error) {
                // Failed to read value
              //  Log.w(TAG , "Failed to read value.", error.toException());
            //}
        //});


        //variables
        long id;
        ArrayList<String> material = new ArrayList<>(3);
        material.add("carboard");
        material.add("plastic");
        material.add("paper");
        material.add("can");

        boolean clean;
        // when the user presses the scan button the object detection starts by inisting the scan service
        Button btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent StartScan = new Intent(com.example.tobinornottobin2.ScanItem.this, com.example.tobinornottobin2.ObjectDetection.ObjectDetection.DetectorActivity.class);
                startActivity(StartScan);
            }
        });

      ImageButton btnClimate = (ImageButton) findViewById(R.id.btnClimateChange);
        btnClimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             Intent moveClimatePage = new Intent(com.example.tobinornottobin2.ScanItem.this, ClimateChange.class);
             startActivity(moveClimatePage);
            }
        });


        ImageButton btnFacts = (ImageButton) findViewById(R.id.btnFunfacts1);
        btnFacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveFactPage = new Intent(com.example.tobinornottobin2.ScanItem.this, FunFacts.class);
                startActivity(moveFactPage);
            }
        });





    }
}