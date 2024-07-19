package com.arctop.unity;
/**
 * Callback for service bind.
 * Notifies the Unity agent on status of binding for the service
 * */
public interface IArctopServiceBindCallback {
    enum BindError {
        ServiceNotFound,
        MultipleServicesFound,
        PermissionDenied,
        UnknownError
    }

    void onSuccess();

    void onFailure(BindError error);
}
