package com.arctop.unity;

import static android.content.Context.BIND_AUTO_CREATE;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.arctop.ArctopSDK;
import com.arctop.IArctopSdk;
import com.arctop.IArctopSdkListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ArctopUnityBridge extends IArctopSdkListener.Stub {
    private final String TAG = "Arctop-Unity-Bridge";
    private IArctopSdk mService = null;
    private Activity mUnityActivity;
    private IArctopSdkCallback mSdkCallback;
    private IArctopServiceBindCallback bindCallback;

    private Map m_devicesMap = new HashMap<>();
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

    private void doBindService() {
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
            }
            else if (matches.size() > 1) {
                // This is really just a sanity check
                // and should never occur in a real life scenario
                Log.d(TAG, "Found multiple matching services!");
                if (bindCallback != null){
                    bindCallback.onFailure(IArctopServiceBindCallback.BindError.MultipleServicesFound);
                }
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
            try {
                mService.registerSDKCallback(ArctopUnityBridge.this);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
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

    public int InitializeSdk(String apiKey, String bundleId){
        Log.d(TAG, "InitializeSdk: " + bundleId + " " + apiKey);
        try {
            return mService.initializeArctop(apiKey);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void arctopSDKShutdown()
    {
        Log.d(TAG, "arctopSDKShutdown");
        try {
            mService.shutdownSdk();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public int arctopSDKIsUserLoggedIn()
    {
        try {
            Log.d(TAG, "arctopSDKIsUserLoggedIn - checking");
            int response = mService.getUserLoginStatus();
            Log.d(TAG, "arctopSDKIsUserLoggedIn: Checked native : " + response);
            return response;
        } catch (RemoteException e) {
            Log.e(TAG, "arctopSDKIsUserLoggedIn: ", e );
            throw new RuntimeException(e);
        }
    }


    public int arctopSDKGetUserCalibrationStatus()
    {
        try {
            return mService.checkUserCalibrationStatus();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public void arctopSDKScanForDevices()
    {
        try {
            Log.d(TAG, "arctopSDKScanForDevices: starting scan");
            mService.scanForDevices();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public void arctopSDKConnectToDeviceId(String deviceId)
    {
        Log.d(TAG, "arctopSDKConnectToDeviceId: " + deviceId);
        Object macAddress = m_devicesMap.get(deviceId);
        if (macAddress != null) {
            Log.d(TAG, "arctopSDKConnectToDeviceId: " + macAddress);
            try {
                mService.connectSensorDevice(macAddress.toString());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            Log.d(TAG, "arctopSDKConnectToDeviceId: device not found");
        }
    }


    public void arctopSDKDisconnectDevice()
    {
        Log.d(TAG, "arctopSDKDisconnectDevice");
        try {
            mService.disconnectSensorDevice();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public int arctopSDKStartPredictions(String predictionName)
    {
        Log.d(TAG, "arctopSDKStartPredictions: " + predictionName);
        try {
            int response = mService.startPredictionSession(predictionName);
            Log.d(TAG, "arctopSDKStartPredictions: " + response);
            return response;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public int arctopSDKEndPrediction()
    {
        Log.d(TAG, "arctopSDKEndPrediction");
        try {
            return mService.finishSession();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public void arctopSDKWriteUserMarker(String markerData)
    {
        Log.d(TAG, "arctopSDKWriteUserMarker " + markerData);
        try {
            mService.writeUserMarker(markerData);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onConnectionChanged(int previousConnection, int currentConnection) throws RemoteException {
        Log.d(TAG, "onConnectionChanged: p:" + previousConnection + " c:" + currentConnection);
        if (mSdkCallback!=null) {
            mSdkCallback.ConnectionStatusCallback(previousConnection, currentConnection);
        }
    }

    @Override
    public void onValueChanged(String key, float value) throws RemoteException {
        Log.d(TAG, "onValueChanged: key:" + key + " value:" + value);
        if (mSdkCallback!=null) {
            mSdkCallback.ValueChangedCallback(key, value);
        }
    }

    @Override
    public void onQAStatus(boolean passed, int type) throws RemoteException {
        Log.d(TAG, "onQAStatus: passed:" + passed + " type:" + type);
        if (mSdkCallback!=null) {
            mSdkCallback.QAStatusCallback(passed,type);
        }
    }

    @Override
    public void onSessionComplete() throws RemoteException {
        Log.d(TAG, "onSessionComplete: ");
        if (mSdkCallback!=null) {
            mSdkCallback.SessionCompleteCallback();
        }
    }

    @Override //TODO: rethink this in general. can we avoid this?
    public void onError(int errorCode, String message) throws RemoteException {
            // TODO:
        Log.d(TAG, "onError: got error code: " + errorCode + "\nMessage: "+ message);
    }

    @Override
    public void onDeviceList(Map deviceList) throws RemoteException {
        Log.d(TAG, "onDeviceList: got list" + deviceList);
        m_devicesMap = deviceList;
        if (mSdkCallback!=null) {
            for (Object item: deviceList.keySet()
                 ) {
                Log.d(TAG, "onDeviceList: " + item.toString());
                mSdkCallback.ScanResultCallback(item.toString());
            }
        }
    }

    @Override
    public void onSignalQuality(String quality) throws RemoteException {
        //Log.d(TAG, "onSignalQuality: " + quality);
        if (mSdkCallback!=null) {
            mSdkCallback.SignalQualityCallback(quality);
        }
    }
}
