# DeviceOwnerApplication
Device Owner Application shows the usage of DevicePolicyManager

Instructions : 

1 - After installing the application send below adb command to device to active device admin.
adb shell dpm set-device-owner --user 0 com.honts.deviceowner/com.honts.deviceowner.DeviceAdminRcvr

2 - Set instrctions button will take you to ApplyRestrictions activity where you can define the app policies. (Example is disallow airplane mode)

