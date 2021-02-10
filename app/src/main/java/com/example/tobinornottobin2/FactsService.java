package com.example.tobinornottobin2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FactsService extends Service {
    public FactsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}