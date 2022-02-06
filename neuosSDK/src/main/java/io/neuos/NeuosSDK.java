package io.neuos;

/**
 * neuosSDK Constants definitions
 * Holds all values arriving via {@link INeuosSdkListener} AIDL callback methods
 * */
public final class NeuosSDK {

    /**
     * Connection values.
     * These arrive via {@link INeuosSdkListener#onConnectionChanged(int, int)}
     **/
    public static final class ConnectionState {
        public static final int UNKNOWN  = 0;
        public static final int CONNECTING = 1;
        public static final int CONNECTED = 2;
        public static final int CONNECTION_FAILED = 3;
        public static final int DISCONNECTED = 4;
        public static final int DISCONNECTED_UPON_REQUEST = 5;
    }

    /**
     * Error Codes.
     * These arrive via {@link INeuosSdkListener#onError(int, String)}
     **/
    public static final class ErrorCodes {
        public static final int NOT_INITIALIZED = 0;
        public static final int API_KEY_ERROR = 1;
        public static final int MODEL_DOWNLOAD_ERROR = 2;
        public static final int SESSION_UPDATE_FAILURE = 3;
        public static final int SESSION_UPLOAD_FAILURE = 4;
        public static final int USER_NOT_LOGGED_IN = 5;
        public static final int CHECK_CALIBRATION_FAILED = 6;
        public static final int SESSION_CREATE_FAILURE = 7;
        public static final int SERVER_CONNECTION_ERROR = 8;
        public static final int MODELS_NOT_AVAILABLE = 9;
        public static final int PREDICTION_NOT_AVAILABLE = 10;
    }

    /**
     * User login status results
     * Received via {@link INeuosSdk#getUserLoginStatus()}
     */
    public static final class LoginStatus {
        public static final int NOT_LOGGED_IN = 0;
        public static final int LOGGED_IN = 1;
    }

    /**
     * User calibration status results
     * Received via {@link INeuosSdk#checkUserCalibrationStatus()}
     */
    public static final class UserCalibrationStatus {
        public static final int BLOCKED  = -1;
        public static final int NEEDS_CALIBRATION = 0;
        public static final int CALIBRATION_DONE = 1;
        public static final int MODELS_AVAILABLE = 2;
    }

    /**
     * QA Failure type results
     * Recieved via {@link INeuosSdkListener#onQAStatus(boolean, int)}
     * */
    public static final class QAFailureType {
        public static final int HEADBAND_OFF_HEAD = 1;
        public static final int MOTION_TOO_HIGH = 2;
        public static final int EEG_FAILURE = 3;
    }

    /**
     * Names of predictions available
     * Use these when calling {@link INeuosSdk#startPredictionSession(String)}
     */
    public static final class Predictions {
        public static final String ZONE = "zone";
    }

    /**
     * Names of prediction values available via {@link INeuosSdkListener#onValueChanged(String, float)}
     */
    public static final class PredictionValues{
        public static final String ZONE_STATE = "zone_state";
        public static final String FOCUS_STATE = "focus";
        public static final String ENJOYMENT_STATE = "enjoyment";
        public static final String HEART_RATE = "heart_rate";
        public static final String AVG_MOTION = "avg_motion";
    }

    /**
     * Values for session upload status messages
     * */
    public static final class UploadStatus {
        public static final int STARTING = 1;
        public static final int COMPRESSING = 2;
        public static final int UPLOADING = 3;
        public static final int SUCCESS = 4;
        public static final int FAILED = 5;
    }
    
    /**
     * Constants used for pairing activity
     * */
    public static final String IO_NEUOS_DEVICE_PAIRING_ACTION = "io.neuos.device_connect";
    public static final int SELECT_DEVICE_REQUEST_CODE = 66;
    public static final String DEVICE_ADDRESS_EXTRA = "deviceAddress";
    public static final String DEVICE_NAME_EXTRA = "deviceName";
}
