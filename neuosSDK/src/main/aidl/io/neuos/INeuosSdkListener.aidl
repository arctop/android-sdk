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
    * Notifies client that a running session has completed
    */
    void onSessionComplete();
    /**
    * Callback for SDK errors encountered during opertion
    * see {@link NeuosSDK#ResponseCodes} for valid codes
    * @param errorCode the current error code
    * @param message extra data on the error
    */
    void onError(in int errorCode ,in String message);
}