package io.neuos;

/**
* Listener interface allowing reception of messages regarding
* upload progress of session
*/
interface INeuosSessionUploadListener {
    /**
    * Reports status of current upload phases
    * values are defined {@link NeuosSDK#UploadStatus}
    */
    void onUploadStatus(in int status);
    /**
    * Reports progress of current upload phase
    * Useful for diplaying progress indicators to user
    */
    void onUploadProgress(in int current , in int total);
}