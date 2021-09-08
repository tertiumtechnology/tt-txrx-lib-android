# tt-txrx-lib-android

Base module used inside Android apps in order to interact with Tertium Ble devices 

## Requirements for version 1.4 and later
Migration to api level 30 (Android 11) require the replacement of original support library APIs with the new androidx
 APIs (see [here](https://developer.android.com/jetpack/androidx/migrate) for more instructions) and the ACCESS_FINE_LOCATION permission

The `supportLibrayVersion` variable, defined as ext in the root project build.gradle file, must be replaced with the
 new `androidxAnnotationVersion` variable (current value is 1.2.0)