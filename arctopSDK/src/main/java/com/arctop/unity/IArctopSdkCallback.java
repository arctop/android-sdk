package com.arctop.unity;

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
    // TODO: Why isn't this on the iOS side?
    void SessionCompleteCallback();
}
