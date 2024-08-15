// IArctopSdk.aidl
package com.arctop;
import com.arctop.IArctopSdkListener;

interface IArctopSdk {

    int initializeArctop(in String apiKey);
    /**
    * Shuts down the sdk and releases resources
    */
    void shutdownSdk();
    /**
    * Retrieves the user's login status.
    * @return int value from {@link ArctopSDK#LoginStatus}
    * */
    int getUserLoginStatus();

    /**
    * Checks the current user's calibration status
    * only calibrated users with available models can run predictions
    * @return int value from {@link ArctopSDK#UserCalibrationStatus}
    */
    int checkUserCalibrationStatus();
    /**
    * Begins a prediction session for the desired prediction
    * @param predictionName the prediction component's name / key to run
    * @return int value from {@link ArctopSDK#ResponseCodes}
    */
    int startPredictionSession(in String predictionName);
    /**
    * Finishes a running prediction session.
    * This will close out all the data files and upload them to arctopCloud
    * calls {@link IArctopSdkListener#onSessionComplete()} once the operation completed
    * the return code only pertains to the session close functionality, and is used to validate
    * that your app's call was accepted. You should still listen for the callback to complete.
    * @return int value from {@link ArctopSDK#ResponseCodes}
    */
    int finishSession();
    /**
    * Requests a marker to be written into the current session's data files
    * Markers will be written with current timestamp
    * @param line extra data line, can be plain text or JSON encoded values
    */
    void writeUserMarker(in String line);
    /**
    * Requests a marker to be written into the current session's data files with a specified timestamp
    * @param line extra data line, can be plain text or JSON encoded values
    * @param timeStamp unix time stamp in MS to use for marker
    */
    void writeUserTimedMarker(in String line , in long timeStamp);
    /**
    * Registers for SDK callbacks
    * @param listener IArctopSdkListener implementation
    * @return int value from {@link ArctopSDK#ResponseCodes}
    */
    int registerSDKCallback(in IArctopSdkListener listener);
    /**
    * Unregisters from SDK callbacks
    * @param listener previously registered listener
    */
    void unregisterSDKCallback(in IArctopSdkListener listener);
    /**
    * Requests connection to a sensor device via it's MAC Address
    * connection status is reported back via {@link IArctopSdkListener#onConnectionChanged(int previousConnection ,int currentConnection)}
    * @param macAddress the device's MAC address to attempt connection to
    */
    oneway void connectSensorDevice(in String macAddress);
    /**
    * Requests a disconnect from currently connected sensor device
    * connection status is reported back via {@link IArctopSdkListener#onConnectionChanged(int previousConnection ,int currentConnection)}
    */
    oneway void disconnectSensorDevice();
    /**
    * Starts a scan for headwear devices.
    * Device list is reported back via {@link IArctopSdkListener#onDeviceList(in Map deviceList)}
    */
    oneway void scanForDevices();

    int logoutUser();
}