// INeuosSdk.aidl
package io.neuos;
import io.neuos.INeuosSdkListener;
import io.neuos.INeuosQAListener;

// TODO: Think about versioning
interface INeuosSdk {
    /**
    * Initializes the SDK for usage
    * Once initialized, {@link INeuosSdkListener#onInitialized()} is called
    * @param apiKey the client's api key to validate usage
    */
    oneway void initializeNeuos(in String apiKey);
    /**
    * Shuts down the sdk and releases resources
    * Once complete, {@link INeuosSdkListener#onShutDown()} is called
    */
    oneway void shutdownSdk();
    /**
    * Retrieves the user's login status.
    * @return int value from {@link NeuosSDK#LoginStatus}
    * */
    int getUserLoginStatus();
    /**
    * Queries the initialization status of the SDK service
    * @return true if initialized false otherwise
    */
    boolean getIsInitialized();
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
    * result is reported via {@link INeuosSdkListener#onUserCalibrationStatus(int calibrationStatus)}
    * valid values are defined in {@link NeuosSDK#UserCalibrationStatus}
    */
    oneway void checkUserCalibrationStatus();
    /**
    * Begins a prediction session for the desired prediction
    * calls {@link INeuosSdkListener#onPredictionSessionStart()} once session has begun
    * @param predictionName the prediction component's name / key to run
    */
    oneway void startPredictionSession(in String predictionName);
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
    oneway void finishSession();
    /**
    * Terminates a session in the event that QA has failed.
    * will close out the session data but will prevent uploading to cloud
    */
    oneway void terminateSessionQaFailed();
    /**
    * Registers for SDK callbacks
    * @param listener INeuosSdkListener implementation
    */
    void registerSDKCallback(in INeuosSdkListener listener);
    /**
    * Unregisters from SDK callbacks
    * @param listener previously registered listener
    */
    void unregisterSDKCallback(in INeuosSdkListener listener);
    /**
    * Registers for QA callbacks
    * @param listener INeuosQAListener implementation
    */
    void registerQACallback(in INeuosQAListener listener);
    /**
    * Unregisters for QA callbacks
    * @param listener INeuosQAListener implementation
    */
    void unregisterQACallback(in INeuosQAListener listener);


    // TODO this is really just internal
    oneway void startCalibrationSession(in String sessionId);

}