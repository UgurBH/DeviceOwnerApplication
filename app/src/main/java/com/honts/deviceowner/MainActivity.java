package com.honts.deviceowner;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RecoverySystem;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button wipeButton;
    Button setRestrictions;
    Button setDeviceOwnerButton;

    private static final String TAG = "DO-MainActivity";

    private static final String FIRST_KIOSK_PACKAGE = "com.android.chrome";
    private static final String SECOND_KIOSK_PACKAGE = "com.google.android.calculator";
    private static final String[] APP_PACKAGES = {FIRST_KIOSK_PACKAGE, SECOND_KIOSK_PACKAGE};
    private DevicePolicyManager devicePolicyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: activity starts");

        devicePolicyManager = (DevicePolicyManager) MainActivity.this.getSystemService(Context.DEVICE_POLICY_SERVICE);

        wipeButton = findViewById(R.id.wipebutton);
        wipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wipeDevice();
            }
        });

        setRestrictions = findViewById(R.id.setRestrictions);
        setRestrictions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRestrictions();
            }
        });

    }

    //below method shows how to perform factory reset with DevicePolicyManager
    public void wipeDevice() {
        //DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyManager.wipeData(0);

    }

    //below method shows how to set restrictions.
    public void setRestrictions() {
        Log.i(TAG, "setting restrictions");
        //DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyManager.addUserRestriction(DeviceAdminRcvr.getComponentName(this), UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT);
        //devicePolicyManager.setKeyguardDisabled(DeviceAdminRcvr.getComponentName(this), true);
        devicePolicyManager.setStatusBarDisabled(DeviceAdminRcvr.getComponentName(this), true);
        devicePolicyManager.setGlobalSetting(DeviceAdminRcvr.getComponentName(this), Settings.Global.ADB_ENABLED, "1");


        ActivityOptions options = ActivityOptions.makeBasic();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            options.setLockTaskEnabled(true);
            Log.d(TAG, "setRestrictions: entered keyguard if statement");
            //devicePolicyManager.setLockTaskFeatures(DeviceAdminRcvr.getComponentName(this),DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD);


        }

        //kiosk mode method starting chrome and setting it as kiosk app

        //devicePolicyManager.setLockTaskPackages(DeviceAdminRcvr.getComponentName(this),APP_PACKAGES);
        /*PackageManager packageManager = MainActivity.this.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(FIRST_KIOSK_PACKAGE);
        if(intent != null){
            MainActivity.this.startActivity(intent, options.toBundle());
        }*/


    }
//Notes
    //<uses-permission android:name="android.permission.REBOOT"/> add to manifest
}