// IArctopSdkListener.aidl
package com.arctop;

/**
* SDK Listener interface.
* Provides callbacks from service into client
*/
oneway interface IArctopSdkListener {
    /**
    * Reports headband connection status changes.
    * See {@link ArctopSDK#ConnectionState} for valid values
    * @param previousConnection the previous connection status
    * @param currentConnection the current connection status
    */
    void onConnectionChanged(in int previousConnection ,in int currentConnection);

    /**
    * Reports a value changed during prediciton.
    * @param key the value's key name {@link ArctopSDK#PredictionValues}
    * @param value the current value
    */
    void onValueChanged(in String key,in float value);
    /**
    * Reports QA status during prediction
    * See {@link ArctopSDK#QAFailureType}
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
    * see {@link ArctopSDK#ResponseCodes} for valid codes
    * @param errorCode the current error code
    * @param message extra data on the error
    */
    void onError(in int errorCode ,in String message);
    /**
    * Reports the found devices from a scan
    * @param Map deviceList - Hashmap<String,String> with device name / mac address
    */
    void onDeviceList(in Map deviceList);
    /**
    * Reports the signal quality values for each
    * electrode on the headband.
    * 0 - perfect quality
    * 113 - no signal
    * reports a string deliniated by commas, one per electrode
    * 0 -> TP9
    * 1 -> AF7
    * 2 -> AF8
    * 3 -> TP10
    */
    void onSignalQuality(in String quality);
}