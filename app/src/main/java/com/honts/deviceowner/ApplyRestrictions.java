package com.honts.deviceowner;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.UserManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ApplyRestrictions extends AppCompatActivity {

    private static final String TAG = "DO-ApplyRestrictions";
    private DevicePolicyManager devicePolicyManager;

    private Switch airplaneToggleSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_restrictions);

        airplaneToggleSwitch  = findViewById(R.id.airplaneSwitch);

        devicePolicyManager = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        
        airplaneToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean enabled) {
                if(enabled){
                    disallowAirplaneModeChange();
                }
                else {
                    allowAirplaneModeChange();
                }
            }
        });


    }

    private void disallowAirplaneModeChange(){
        devicePolicyManager.addUserRestriction(DeviceAdminRcvr.getComponentName(this), UserManager.DISALLOW_AIRPLANE_MODE);

    }

    private void allowAirplaneModeChange(){
        devicePolicyManager.clearUserRestriction(DeviceAdminRcvr.getComponentName(this), UserManager.DISALLOW_AIRPLANE_MODE);

    }
}