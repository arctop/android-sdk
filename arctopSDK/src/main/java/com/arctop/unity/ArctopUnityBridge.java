package com.arctop.unity;

import static android.content.Context.BIND_AUTO_CREATE;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.arctop.ArctopSDK;
import com.arctop.IArctopSdk;

import java.util.List;

public class ArctopUnityBridge {
    private final String TAG = "Arctop-Unity-Bridge";
    private IArctopSdk mService = null;
    private Activity mUnityActivity;
    private IArctopSdkCallback mSdkCallback;
    private IArctopServiceBindCallback bindCallback;
    // Called From C# to set the Activity Instance
    // This needs to be called after the C# side has requested the proper SDK permissions.
    public void setUnityActivity(Activity activity, IArctopServiceBindCallback callback) {
        mUnityActivity = activity;
        bindCallback = callback;
        doBindService();
    }

    public void setSdkCallback(IArctopSdkCallback callback){
        mSdkCallback = callback;
    }

    void doBindService() {
        try {
            // Create an intent based on the class name
            Intent serviceIntent = new Intent(IArctopSdk.class.getName());
            // Use package manager to find intent receiver
            List<ResolveInfo> matches=mUnityActivity.getPackageManager()
                    .queryIntentServices(serviceIntent, 0);
            if (matches.isEmpty()) {
                Log.d(TAG, "Cannot find a matching service!");
                if (bindCallback != null){
                    bindCallback.onFailure(IArctopServiceBindCallback.BindError.ServiceNotFound);
                }
//                Toast.makeText(mUnityActivity, "Cannot find a matching service!",
//                        Toast.LENGTH_LONG).show();
            }
            else if (matches.size() > 1) {
                // This is really just a sanity check
                // and should never occur in a real life scenario
                Log.d(TAG, "Found multiple matching services!");
                if (bindCallback != null){
                    bindCallback.onFailure(IArctopServiceBindCallback.BindError.MultipleServicesFound);
                }
//                Toast.makeText(mUnityActivity, "Found multiple matching services!",
//                        Toast.LENGTH_LONG).show();
            }
            else {
                // Create an explicit intent
                Intent explicit=new Intent(serviceIntent);
                ServiceInfo svcInfo=matches.get(0).serviceInfo;
                ComponentName cn=new ComponentName(svcInfo.applicationInfo.packageName,
                        svcInfo.name);
                explicit.setComponent(cn);
                // Bind using AUTO_CREATE
                if (mUnityActivity.bindService(explicit, mConnection,  BIND_AUTO_CREATE)){
                    Log.d(TAG, "Bound to Arctop Service");
                } else {
                    Log.d(TAG, "Failed to bind to Arctop Service");
                    // TODO: Verify this is the right error
                    if (bindCallback != null){
                        bindCallback.onFailure(IArctopServiceBindCallback.BindError.PermissionDenied);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "can't bind to ArctopService, check permission in Manifest");
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mService = IArctopSdk.Stub.asInterface(service);
//            if (mGameQAReceiver != null){
//                try {
//                    mService.registerQACallback(mGameQAReceiver);
//                } catch (RemoteException e) {
//                    Log.e(TAG, "onServiceConnected: " + e.getMessage(), e);
//                }
//            }
            if (bindCallback != null){
                bindCallback.onSuccess();
            }
            Log.d(TAG, "Unity connected to service");
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.d(TAG, "Unity disconnected to service");
        }
    };

    public void InitializeSdk(String apiKey, String bundleId, IArctopSdkSuccessOrFailureCallback callback){
        Log.d(TAG, "InitializeSdk: " + bundleId + " " + apiKey);
        try {
            int response = mService.initializeArctop(apiKey);
            if (response == ArctopSDK.ResponseCodes.SUCCESS){
                callback.onSuccess();
            }
            else {
                callback.onFailure(response);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

}
