package com.honts.deviceowner;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

public class DeviceAdminRcvr extends DeviceAdminReceiver {
    
    private static String TAG = "testing";

    @Override
    public void onProfileProvisioningComplete(@NonNull Context context, @NonNull Intent intent) {
        Log.d(TAG, "onProfileProvisioningComplete: entered");

        DevicePolicyManager manager =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = getComponentName(context);
        //manager.setProfileName(componentName, context.getString(R.string.profile_name));
        manager.setProfileName(componentName, "");
        manager.setProfileEnabled(componentName);


        Intent launch = new Intent(context, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch);

        //super.onProfileProvisioningComplete(context, intent);

        //adb command to set the device owner is below
        //adb shell dpm set-device-owner --user 0 com.honts.deviceowner/com.honts.deviceowner.DeviceAdminRcvr


    }

    public static ComponentName getComponentName(Context context) {
       return new ComponentName(context.getApplicationContext(), DeviceAdminRcvr.class);
    }
}
