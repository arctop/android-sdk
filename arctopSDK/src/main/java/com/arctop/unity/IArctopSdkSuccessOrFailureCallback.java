package com.arctop.unity;

public interface IArctopSdkSuccessOrFailureCallback {
    void onSuccess();
    //void onFailure();
    void onFailure(int response);
}

