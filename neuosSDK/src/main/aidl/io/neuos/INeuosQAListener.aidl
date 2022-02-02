package io.neuos;
/**
* QA listener interface
* Provides feedback on signal quality and headband status
*/
oneway interface INeuosQAListener {
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
    /**
    * Reports headband on / off head status
    */
    void onHeadbandStatusChange(in boolean headbandOn);
    /**
    * Reports battery charge left value in increments of 5%
    */
    void onBatteryStatus(float chargeLeft);
    /**
    * Reports device's plugged in state changes
    * Having a device plugged in causes noise and reduces signal quality
    */
    void onDevicePluggedInStatusChange(in boolean pluggedIn);

}