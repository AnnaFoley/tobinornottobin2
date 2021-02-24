package com.example.tobinornottobin2.ObjectDetection.ObjectDetection;

import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;

import static android.content.ContentValues.TAG;

public final class AndroidJUnit4 extends Runner implements Filterable, Sortable {
    @Override
    public Filter getFilter() {
        return null;
    }

    @Override
    public Description getDescription() {
        return null;
    }

    @Override
    public void run(RunNotifier notifier) {

    }

    @Override
    public void sort(Sorter sorter) {

    }




}
