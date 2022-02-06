// INeuosSdkListener.aidl
package io.neuos;

/**
* SDK Listener interface.
* Provides callbacks from service into client
*/
oneway interface INeuosSdkListener {
    /**
    * Reports headband connection status changes.
    * See {@link NeuosSDK#ConnectionState} for valid values
    * @param previousConnection the previous connection status
    * @param currentConnection the current connection status
    */
    void onConnectionChanged(in int previousConnection ,in int currentConnection);
    /**
    * Reports a value changed during prediciton.
    * @param key the value's key name {@link NeuosSDK#PredictionValues}
    * @param value the current value
    */
    void onValueChanged(in String key,in float value);
    /**
    * Reports QA status during prediction
    * See {@link NeuosSDK#QAFailureType}
    * @param passed did QA pass for current run
    * @param type if QA failed, provides the reason for failure
    */
    void onQAStatus(in boolean passed ,in int type);
    /**
    * Reports result of querying the user's calibration status
    * See {@link NeuosSDK#UserCalibrationStatus}
    * @param calibrationStatus current status
    */
    void onUserCalibrationStatus(int calibrationStatus);
    /**
    * Notifies client that a predictions session has begun
    */
    void onPredictionSessionStart();
    /**
    * Notifies client that an experiment session has begun
    */
    void onExperimentSessionStart();
    /**
    * Notifies client that a running session has completed
    */
    void onSessionComplete();
    /**
    * Notifies client that the sdk has successfully been initialized
    * and is now ready for predctions / experiments
    */
    void onInitialized();
    /**
    * Notifies client that SDK has finished shutting down
    */
    void onShutDown();
    /**
    * Callback for SDK errors encountered during opertion
    * see {@link NeuosSDK#ErrorCodes} for valid codes
    * @param errorCode the current error code
    * @param message extra data on the error
    */
    void onError(in int errorCode ,in String message);
}