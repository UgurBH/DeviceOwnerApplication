package com.honts.deviceowner;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    private static final String TAG = "DO-BroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: LOCKED_BOOT received");

        //perform your actions here
        //Context context1 = context.createDeviceProtectedStorageContext();
        //context1.getSharedPreferences("DeviceProtectedStorage", Context.MODE_PRIVATE);

        try {
            Log.i(TAG, "onReceive: sleep 5 seconds");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Log.e(TAG, "onReceive: exception occured" );
            e.printStackTrace();
        }
        Log.d(TAG, "onReceive: try - catch end");

        //below code starts main activity after boot locked completed intent
//        Intent i = new Intent(context,MainActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(i);

    }
}
