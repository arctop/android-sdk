// INeuosSdk.aidl
package io.neuos;
import io.neuos.INeuosSdkListener;
import io.neuos.INeuosQAListener;
import io.neuos.INeuosSessionUploadListener;

interface INeuosSdk {

    /**
    * Begins calibration session.
    * This is an INTERNAL CALL
    * Users of SDK NEVER need to call this function.
    */
    int startCalibrationSession(in String sessionId);

    /**
    * Initializes the SDK for usage
    * @param apiKey the client's api key to validate usage
    * @return int value from {@link NeuosSDK#ResponseCodes}
    */
    int initializeNeuos(in String apiKey);
    /**
    * Shuts down the sdk and releases resources
    */
    void shutdownSdk();
    /**
    * Retrieves the user's login status.
    * @return int value from {@link NeuosSDK#LoginStatus}
    * */
    int getUserLoginStatus();
    /**
    * Requests connection to a sensor device via it's MAC Address
    * connection status is reported back via {@link INeuosSdkListener#onConnectionChanged(int previousConnection ,int currentConnection)}
    * @param macAddress the device's MAC address to attempt connection to
    */
    oneway void connectSensorDevice(in String macAddress);
    /**
    * Requests a disconnect from currently connected sensor device
    * connection status is reported back via {@link INeuosSdkListener#onConnectionChanged(int previousConnection ,int currentConnection)}
    */
    oneway void disconnectSensorDevice();
    /**
    * Checks the current user's calibration status
    * only calibrated users with available models can run predictions
    * @return int value from {@link NeuosSDK#UserCalibrationStatus}
    */
    int checkUserCalibrationStatus();
    /**
    * Begins a prediction session for the desired prediction
    * @param predictionName the prediction component's name / key to run
    * @return int value from {@link NeuosSDK#ResponseCodes}
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
    * Finishes a running prediction session.
    * This will close out all the data files and upload them to neuosCloud
    * calls {@link INeuosSdkListener#onSessionComplete()} once the operation completed
    */
    void finishSession();
    /**
    * Terminates a session in the event that QA has failed.
    * will close out the session data but will prevent uploading to cloud
    */
    void terminateSessionQaFailed();
    /**
    * Registers for SDK callbacks
    * @param listener INeuosSdkListener implementation
    * @return int value from {@link NeuosSDK#ResponseCodes}
    */
    int registerSDKCallback(in INeuosSdkListener listener);
    /**
    * Unregisters from SDK callbacks
    * @param listener previously registered listener
    */
    void unregisterSDKCallback(in INeuosSdkListener listener);
    /**
    * Registers for QA callbacks
    * @param listener INeuosQAListener implementation
    * @return int value from {@link NeuosSDK#ResponseCodes}
    */
    int registerQACallback(in INeuosQAListener listener);
    /**
    * Unregisters for QA callbacks
    * @param listener INeuosQAListener implementation
    */
    void unregisterQACallback(in INeuosQAListener listener);
    /**
    * Registers for Upload callbacks
    * @param listener INeuosQAListener implementation
    * @return int value from {@link NeuosSDK#ResponseCodes}
    */
    int registerUploadCallback(INeuosSessionUploadListener listener);
    /**
    * Unregisters for Upload callbacks
    * @param listener INeuosQAListener implementation
    */
    void unregisterUploadCallback(INeuosSessionUploadListener listener);

}