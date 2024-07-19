package com.arctop.unity;

/**
 * Callback interface for a success / failure of operation
 * */
public interface IArctopSdkSuccessOrFailureCallback {
    void onSuccess();
    void onFailure(int response);
}

