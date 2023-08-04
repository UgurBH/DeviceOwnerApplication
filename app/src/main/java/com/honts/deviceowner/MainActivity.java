package com.honts.deviceowner;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DO-MainActivity";

    private static final String FIRST_KIOSK_PACKAGE = "com.honts.recorder";
    private static final String SECOND_KIOSK_PACKAGE = "com.google.android.calculator";
    private static final String[] APP_PACKAGES = {FIRST_KIOSK_PACKAGE, SECOND_KIOSK_PACKAGE, Settings.ACTION_BLUETOOTH_SETTINGS};

    private DevicePolicyManager devicePolicyManager;
    private AppInstallerListener appInstallerListener;
    private IntentFilter registerAppIntentFilter;


    Button wipeButton;
    Button setRestrictions;
    Button setDeviceOwnerButton;
    private Button setPinButton;
    private Button certInstallerBtn;

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

        setPinButton = findViewById(R.id.setPin);
        setPinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //unistallApps("com.honts.recorder");
                setPinCode();
            }
        });

        certInstallerBtn = findViewById(R.id.certInstaller);
        certInstallerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    installCerts();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                }
            }
        });

        registerAppInstaller();

    }

    public void installCerts() throws CertificateException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        File file = new File("/sdcard/myCA.cer");
        //File filePfx = new File("/sdcard/myCA.pfx");
        //String file = "/sdcard/myCA.pem";
        devicePolicyManager.setGlobalSetting(DeviceAdminRcvr.getComponentName(this), Settings.Global.ADB_ENABLED, "1");

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();


        FileInputStream certificateInputStream = new FileInputStream(file);
        CertificateFactory certificateFactory = null;
        certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate caCertificate = (X509Certificate) certificateFactory.generateCertificate(certificateInputStream);



        byte[] certificateData = caCertificate.getEncoded();


        //devicePolicyManager.installCaCert(DeviceAdminRcvr.getComponentName(this), certificateData);

        devicePolicyManager.installKeyPair(DeviceAdminRcvr.getComponentName(this), privateKey, caCertificate, "testing");


    }


    public void registerAppInstaller() {
        appInstallerListener = new AppInstallerListener();
        registerAppIntentFilter = new IntentFilter();
        registerAppIntentFilter.addAction("Honts_DeviceOwner_deleteApp");
        registerReceiver(appInstallerListener, registerAppIntentFilter);


    }

    //below method uninstall the apps
    public void unistallApps(String appname) {

        //the code below uninstalls the app with pop-up message box on Android 11
        /*
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + appname));
        startActivity(intent);
        */

        //below code uninstalls the app silently on Android 11

        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        PackageInstaller packageInstaller = this.getPackageManager().getPackageInstaller();
        packageInstaller.uninstall(appname, pendingIntent.getIntentSender());


    }

    // below method sets the Pin code
    public void setPinCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            devicePolicyManager.setGlobalSetting(DeviceAdminRcvr.getComponentName(this), Settings.Global.ADB_ENABLED, "1");
            byte byteValue[] = new byte[123];
            devicePolicyManager.setResetPasswordToken(DeviceAdminRcvr.getComponentName(this), byteValue);
            Log.d(TAG, "setPinCode: is active " + devicePolicyManager.isResetPasswordTokenActive(DeviceAdminRcvr.getComponentName(this)));
            devicePolicyManager.resetPasswordWithToken(DeviceAdminRcvr.getComponentName(this), "1234", byteValue, 1);
        }
    }

    //below method shows how to perform factory reset with DevicePolicyManager
    public void wipeDevice() {
        //DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyManager.wipeData(0);

    }


    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //do something or nothing in your case
            Log.d(TAG, "onKeyLongPress: back key pressed !!!! ***");
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    //below method shows how to set restrictions.
    public void setRestrictions() {
        Log.i(TAG, "setting restrictions");

        //Intent startAppRestrictions = new Intent(this, ApplyRestrictions.class);
        //startActivity(startAppRestrictions);

        //DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        //devicePolicyManager.addUserRestriction(DeviceAdminRcvr.getComponentName(this), UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT);
        //devicePolicyManager.setKeyguardDisabled(DeviceAdminRcvr.getComponentName(this), true);
        //devicePolicyManager.setStatusBarDisabled(DeviceAdminRcvr.getComponentName(this), true);
        devicePolicyManager.setGlobalSetting(DeviceAdminRcvr.getComponentName(this), Settings.Global.ADB_ENABLED, "1");


        ActivityOptions options = ActivityOptions.makeBasic();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            options.setLockTaskEnabled(true);
            Log.d(TAG, "setRestrictions: entered keyguard if statement");
            //devicePolicyManager.setKeyguardDisabled(DeviceAdminRcvr.getComponentName(this), true); //make this false to solve scan engine issue
            //devicePolicyManager.setLockTaskFeatures(DeviceAdminRcvr.getComponentName(this),DevicePolicyManager.LOCK_TASK_FEATURE_HOME);
            //devicePolicyManager.setLockTaskFeatures(DeviceAdminRcvr.getComponentName(this), DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS);
            devicePolicyManager.setLockTaskFeatures(DeviceAdminRcvr.getComponentName(this), DevicePolicyManager.LOCK_TASK_FEATURE_HOME | DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS | DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS);

        }

        //kiosk mode method starting chrome and setting it as kiosk app

        devicePolicyManager.setLockTaskPackages(DeviceAdminRcvr.getComponentName(this), APP_PACKAGES);
        PackageManager packageManager = MainActivity.this.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(FIRST_KIOSK_PACKAGE);
        if (intent != null) {
            MainActivity.this.startActivity(intent, options.toBundle());
        }

    }
//Notes
    //<uses-permission android:name="android.permission.REBOOT"/> add to manifest
}