// IArctopSdk.aidl
package com.arctop;
import com.arctop.IArctopSdkListener;
import com.arctop.IArctopQAListener;
import com.arctop.IArctopSessionUploadListener;
import com.arctop.IArctopDeviceListener;

interface IArctopSdk {

    /**
    * Begins calibration session.
    * This is an INTERNAL CALL
    * Users of SDK NEVER need to call this function.
    */
    int startCalibrationSession(in String sessionId);
    /**
    * Internal function. Used for google testing only
    */
    void selectBtListener(in String userId);
    /**
    * Begins a scan of devices compatible with the SDK
    * Normally wrapped by PairingActivity and doesn't need to be called from outside
    */
    void startScanningForDevices();
    /**
    * Stops an ongoing scan of devices compatible with the SDK
    * Normally wrapped by PairingActivity and doesn't need to be called from outside
    */
    void stopScanningForDevices();
    /**
    * Initializes the SDK for usage
    * @param apiKey the client's api key to validate usage
    * @return int value from {@link ArctopSDK#ResponseCodes}
    */
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
    * Logs the current user out of the system
    */
    oneway void logoutUser();
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
    * Requests a marker to be written into the current session's data files
    * Markers will be written with current timestamp
    * @param markerId numerical identifier of marker
    * @param line extra data line, can be plain text or JSON encoded values
    */
    void writeMarker(in int markerId , in String line);
    /**
    * Requests a marker to be written into the current session's data files with a specified timestamp
    * @param markerId numerical identifier of marker
    * @param line extra data line, can be plain text or JSON encoded values
    * @param timeStamp unix time stamp in MS to use for marker
    */
    void writeTimedMarker(in int markerId , in String line , in long timeStamp);
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
    * Terminates a session in the event that QA has failed.
    * will close out the session data but will prevent uploading to cloud
    * @return int value from {@link ArctopSDK#ResponseCodes}
    */
    int terminateSessionQaFailed();
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
    * Registers for QA callbacks
    * @param listener IArctopQAListener implementation
    * @return int value from {@link ArctopSDK#ResponseCodes}
    */
    int registerQACallback(in IArctopQAListener listener);
    /**
    * Unregisters for QA callbacks
    * @param listener IArctopQAListener implementation
    */
    void unregisterQACallback(in IArctopQAListener listener);
    /**
    * Registers for Upload callbacks
    * @param listener IArctopQAListener implementation
    * @return int value from {@link ArctopSDK#ResponseCodes}
    */
    int registerUploadCallback(IArctopSessionUploadListener listener);
    /**
    * Unregisters for Upload callbacks
    * @param listener IArctopQAListener implementation
    */
    void unregisterUploadCallback(IArctopSessionUploadListener listener);
    /**
    * Registers for Device list callbacks
    * @param listener IArctopDeviceListener implementation
    * @return int value from {@link ArctopSDK#ResponseCodes}
    */
    int registerDeviceListCallback(IArctopDeviceListener listener);
    /**
    * Unregisters for Upload callbacks
    * @param listener IArctopQAListener implementation
    */
    void unregisterDeviceListCallback(IArctopDeviceListener listener);
    /**
    * Attempts to re-upload last session in case it failed.
    */
    int retryUploadLastSession();
    /**
    *  returns the prefix that is used in file names for this session
    */
    String getCurrentSessionBaseFilename();
    /**
    *   returns the name of currently connected headband, or an empty string
    */
    String getConnectedSensorDeviceName();
}