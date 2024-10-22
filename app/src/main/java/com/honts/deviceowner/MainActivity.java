package com.honts.deviceowner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.security.KeyChain;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;

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
                    //installCerts();
                    installWifiCert(MainActivity.this);
                    installCerts();
                    //installWifiCertNew(MainActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        registerAppInstaller();
        checkPermissions();

    }

    public void checkPermissions() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "checkPermissions: permission required");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            Log.d(TAG, "checkPermissions: permission granted");
        }

    }

    public void installCerts() throws CertificateException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        File file = new File("/sdcard/myCA.pem");
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
        X509Certificate[] caCertificateArray = new X509Certificate[]{caCertificate};



        byte[] certificateData = caCertificate.getEncoded();


        //devicePolicyManager.installCaCert(DeviceAdminRcvr.getComponentName(this), certificateData);

        devicePolicyManager.installKeyPair(DeviceAdminRcvr.getComponentName(this), privateKey, caCertificate, "testing");
        //devicePolicyManager.installKeyPair(DeviceAdminRcvr.getComponentName(this), privateKey, caCertificateArray, "testing", true);


    }

    public void installWifiCert(Context context) throws Exception {
        // Load the client certificate (PEM or P12 file)
        FileInputStream fis = new FileInputStream("/sdcard/myCA.pfx");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(fis, "1234".toCharArray());

        // Extract the private key and client certificate from the keystore
        PrivateKey privateKey = (PrivateKey) keyStore.getKey("alias", "1234".toCharArray());
        Certificate[] certificateChain = keyStore.getCertificateChain("alias");

        // Load the CA certificate (PEM file)
        FileInputStream caInput = new FileInputStream("/sdcard/myCA.pem");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate caCert = cf.generateCertificate(caInput);

        // Close file streams
        fis.close();
        caInput.close();

        // Get WifiManager and create a WifiConfiguration object
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wifiConfig = new WifiConfiguration();

        // Set your Wi-Fi SSID (network name)
        wifiConfig.SSID = "\"" + "burak" + "\"";

        // Use WPA2-Enterprise (EAP-TLS) for security
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);

        // Configure the Enterprise settings
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);

        // Set client certificate and private key
        //enterpriseConfig.setClientKeyEntry(privateKey, (X509Certificate) certificateChain[0]);

        // Set the CA certificate
        enterpriseConfig.setCaCertificate((X509Certificate) caCert);

        // Apply the enterprise config to the Wi-Fi configuration
        wifiConfig.enterpriseConfig = enterpriseConfig;

        // Add the configured Wi-Fi network
        int networkId = wifiManager.addNetwork(wifiConfig);
        if (networkId != -1) {
            wifiManager.enableNetwork(networkId, true);
        }
    }

    public void installWifiCertNew(Context context) throws Exception {
        // Load the client certificate (e.g., from PKCS12 file)
        FileInputStream fis = new FileInputStream("/sdcard/myCA.pfx");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(fis, "1234".toCharArray());

        // Extract the private key and client certificate from the keystore
        PrivateKey privateKey = (PrivateKey) keyStore.getKey("alias", "1234".toCharArray());
        Certificate[] certificateChain = keyStore.getCertificateChain("alias");

        // Load the CA certificate (from a PEM file or other format)
        FileInputStream caInput = new FileInputStream("/sdcard/myCA.pem");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(caInput);

        // Close file streams
        fis.close();
        caInput.close();

        // Configure the Wi-Fi network suggestion
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);

        // Set the client certificate and private key
        //enterpriseConfig.setClientKeyEntry(privateKey, (X509Certificate) certificateChain[0]);

        // Set the CA certificate for server validation
        enterpriseConfig.setCaCertificate(caCert);
        enterpriseConfig.setAltSubjectMatch("burak");


        // Create a Wi-Fi network suggestion for WPA2-Enterprise (EAP-TLS)
        WifiNetworkSuggestion suggestion = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            suggestion = new WifiNetworkSuggestion.Builder()
                    .setSsid("Your_Wifi_SSID")  // Set your Wi-Fi SSID
                    .setWpa2EnterpriseConfig(enterpriseConfig)  // Apply the enterprise configuration
                    .build();
        }

        // Get WifiManager and add the Wi-Fi network suggestion
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Add the Wi-Fi network suggestion to the system
        int status = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            status = wifiManager.addNetworkSuggestions(Collections.singletonList(suggestion));
        }

        if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            // Successfully added the suggestion
            System.out.println("Wi-Fi suggestion added successfully");
        } else {
            // Failed to add the suggestion
            System.out.println("Failed to add Wi-Fi suggestion");
        }
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