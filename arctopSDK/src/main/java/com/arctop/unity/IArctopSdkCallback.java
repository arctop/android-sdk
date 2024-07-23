package com.arctop.unity;
/**
 * Unity callback interface.
 * Used in Java -> C# interop to notify the native plugin on events in the SDK
 * */
public interface IArctopSdkCallback {
    void ScanResultCallback(String device);
    // delegate for connection status
    void ConnectionStatusCallback(int previous, int current);
    // delegate for realtime values
    void ValueChangedCallback(String key, float value);
    // delegate for qa
    void QAStatusCallback(Boolean passed, int errorCode);
    // delegate for signalQuality
    void SignalQualityCallback(String signalQuality);
    // delegate for session complete
    void SessionCompleteCallback();
}
