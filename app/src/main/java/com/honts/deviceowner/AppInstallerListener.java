package com.honts.deviceowner;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.util.Log;

public class AppInstallerListener extends BroadcastReceiver {

    private static final String TAG = "AppInstallerListener";

    private Intent deleteIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: intent received");

        String packageName = "com.honts.recorder";

        /*
        deleteIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        deleteIntent.setData(Uri.parse("package:"+packageName));
        context.startActivity(deleteIntent);
        */

        deleteIntent = new Intent(context, context.getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        packageInstaller.uninstall(packageName,pendingIntent.getIntentSender());


        Log.d(TAG, "onReceive: appInstallerreceiver exit");

    }
}
