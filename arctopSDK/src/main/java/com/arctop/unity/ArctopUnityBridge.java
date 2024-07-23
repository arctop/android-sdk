package com.arctop.unity;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.RECEIVER_EXPORTED;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.arctop.ArctopSDK;
import com.arctop.IArctopSdk;
import com.arctop.IArctopSdkListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unity bridge. Entry point for C# -> Java communication between service and Unity3D
 * */
public class ArctopUnityBridge extends IArctopSdkListener.Stub {
    private final String TAG = "Arctop-Unity-Bridge";
    private IArctopSdk mService = null;
    private Activity mUnityActivity;
    private IArctopSdkCallback mSdkCallback;
    private IArctopServiceBindCallback mBindCallback;
    private LoginResultReceiver mLoginResultReceiver;


    private Map<Object, Object> m_devicesMap = new HashMap<>();
    // Called From C# to set the Activity Instance
    // This needs to be called after the C# side has requested the proper SDK permissions.
    public void setUnityActivity(Activity activity, IArctopServiceBindCallback callback) {
        mUnityActivity = activity;
        mBindCallback = callback;
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
                if (mBindCallback != null){
                    mBindCallback.onFailure(IArctopServiceBindCallback.BindError.ServiceNotFound);
                }
            }
            else if (matches.size() > 1) {
                // This is really just a sanity check
                // and should never occur in a real life scenario
                Log.d(TAG, "Found multiple matching services!");
                if (mBindCallback != null){
                    mBindCallback.onFailure(IArctopServiceBindCallback.BindError.MultipleServicesFound);
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
                    if (mBindCallback != null){
                        mBindCallback.onFailure(IArctopServiceBindCallback.BindError.PermissionDenied);
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
            if (mBindCallback != null){
                mBindCallback.onSuccess();
            }
            Log.d(TAG, "Unity connected to service");
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.d(TAG, "Unity disconnected to service");
        }
    };

    public int arctopSDKInit(String apiKey){
        try {
            return mService.initializeArctop(apiKey);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void arctopSDKShutdown()
    {
        try {
            mService.shutdownSdk();
            mService.unregisterSDKCallback(this);
            mService = null;
            mLoginCallback = null;
            mSdkCallback = null;
            mUnityActivity = null;
            mBindCallback = null;

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    private Intent getExplicitIntent(Intent activityIntent){
        List<ResolveInfo> matches = mUnityActivity.getPackageManager()
        .queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);
        if (matches.isEmpty()){
            return null;
        }
        Intent explicit = new Intent(activityIntent);
        ActivityInfo activityInfo = matches.get(0).activityInfo;
        ComponentName cn = new ComponentName(activityInfo.applicationInfo.packageName
                ,activityInfo.name);
        explicit.setComponent(cn);
        return explicit;
    }

    private class LoginResultReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            mUnityActivity.unregisterReceiver(mLoginResultReceiver);
            // TODO: We don't really have a fail here.
            // TODO: If you fail it's handled in the other activity.
            mLoginCallback.onSuccess();
            mLoginCallback = null;
        }
    }
    private IArctopSdkSuccessOrFailureCallback mLoginCallback;
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void arctopLaunchLogin(IArctopSdkSuccessOrFailureCallback callback){
        Intent activityIntent = getExplicitIntent(new Intent(ArctopSDK.ARCTOP_LOGIN));
        mLoginCallback = callback;
        mLoginResultReceiver = new LoginResultReceiver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mUnityActivity.registerReceiver(mLoginResultReceiver ,
                    new IntentFilter(ArctopSDK.ARCTOP_LOGIN_RESULT), RECEIVER_EXPORTED);
        }
        else{
            mUnityActivity.registerReceiver(mLoginResultReceiver ,
                    new IntentFilter(ArctopSDK.ARCTOP_LOGIN_RESULT));
        }

        mUnityActivity.startActivity(activityIntent);
    }

    public int arctopSDKIsUserLoggedIn()
    {
        try {
            return mService.getUserLoginStatus();
        } catch (RemoteException e) {
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
            mService.scanForDevices();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public void arctopSDKConnectToDeviceId(String deviceId)
    {
        Object macAddress = m_devicesMap.get(deviceId);
        if (macAddress != null) {
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
        try {
            mService.disconnectSensorDevice();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public int arctopSDKStartPredictions(String predictionName)
    {
        try {
            return mService.startPredictionSession(predictionName);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public int arctopSDKEndPrediction()
    {
        try {
            return mService.finishSession();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public void arctopSDKWriteUserMarker(String markerData)
    {
        try {
            mService.writeUserMarker(markerData);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onConnectionChanged(int previousConnection, int currentConnection) throws RemoteException {
        if (mSdkCallback!=null) {
            mSdkCallback.ConnectionStatusCallback(previousConnection, currentConnection);
        }
    }

    @Override
    public void onValueChanged(String key, float value) throws RemoteException {
        if (mSdkCallback!=null) {
            mSdkCallback.ValueChangedCallback(key, value);
        }
    }

    @Override
    public void onQAStatus(boolean passed, int type) throws RemoteException {
        if (mSdkCallback!=null) {
            mSdkCallback.QAStatusCallback(passed,type);
        }
    }

    @Override
    public void onSessionComplete() throws RemoteException {
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
        m_devicesMap = deviceList;
        if (mSdkCallback!=null) {
            for (Object item: deviceList.keySet()
                 ) {
                mSdkCallback.ScanResultCallback(item.toString());
            }
        }
    }

    @Override
    public void onSignalQuality(String quality) throws RemoteException {
        if (mSdkCallback!=null) {
            mSdkCallback.SignalQualityCallback(quality);
        }
    }
}
