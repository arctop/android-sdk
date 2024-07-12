package com.arctop.unity;

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
