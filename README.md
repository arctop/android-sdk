# Neuos™ Software Development Kit (SDK)

The Neuos SDK public repository contains everything you need to connect your application to Neuos services.

# Installation

To add the SDK to your project use **ONE** of the following methods:

1. Use [JitPack](https://jitpack.io/private#arctop/Neuos-SDK) to install the SDK as a dependency with gradle or maven. (Recommended) 
2. Clone the repository locally, and add it as a module into your Android project.


# Package structure

The SDK contains the following components:

- The *NeuosSDK.java* class with all constants used to communicate with your application.

- A set of AIDL files, which define the interface of communication with the service.
    - *INeuosSdk.aidl* provides the public service API accessible by your app.
    - *INeuosSdkListener.aidl* defines the callback interface by which the service provides responses and notifications back to your app.
    - *INeuosQAListener.aidl* defines a callback interface that provides QA values from the sensor device to your app.
    - *INeuosSessionUploadListener.aidl* defines a callback interface that provides session upload monitoring.      
1. 
# Workflow

> **_NOTE:_**  The SDK is designed to work in a specific flow. 
> The setup phase needs to be done once as long as your application is running. The session phase can be done multiple times.

## Setup Phase

1. [Neuos Permissions](#permissions)
2. [Bind to service](#binding-to-the-service)
3. [Register for callbacks](#register-for-callbacks)
4. [Initialize the SDK with your API key](#initialize-the-sdk-with-your-api-key)

## Session Phase

1. [Verify that a user is logged in](#verify-a-user-is-logged-in)
2. [Verify that a user has been calibrated for Neuos](#verify-a-user-is-calibrated-for-neuos)
3. [Connect to a Neuos sensor device](#connect-to-a-neuos-sensor-device)
4. [Verify Signal Quality of device](#verify-signal-quality-of-device)
5. [Begin a session](#begin-a-session)
6. [Work with session data](#work-with-session-data)
7. [Finish session](#finish-session)

## Cleanup

1. [Shutdown the SDK](#shutdown-the-sdk)
2. [Unbind from the service](#unbind-from-the-service)

### Setup Phase

#### Permissions

Before binding to Neuos and receiving data, you will need to request permissions from the user.
In your *AndroidManifest.XML*, declare that you will be using the NEUOS_DATA permission:

    <uses-permission android:name="io.neuos.permission.NEUOS_DATA" />

Then, at runtime, verify that you have that permission, or request it from the user, as per:

[Requesting runtime permissions reference in Android developer guide](https://developer.android.com/training/permissions/requesting)

#### Binding to the service

The SDK revolves around a single service entry point that your application will need to bind to.

In order to perform the bind, you will need to declare that your application will query the service package.
This is done by adding this snippet into your *AndroidManifest.XML*

    <queries>
        <package android:name="io.neuos.central" />
    </queries>

[Query element reference in Android developer guide](https://developer.android.com/guide/topics/manifest/queries-element)

Then, in your code, locate the service, and perform the binding

    void doBindService() {
        try {
            // Create an intent based on the class name
            Intent serviceIntent = new Intent(INeuosSdk.class.getName());
            // Use package manager to find intent reciever
            List<ResolveInfo> matches=getPackageManager()
                    .queryIntentServices(serviceIntent, 0);
            if (matches.size() == 0) {
                Log.d(TAG, "Cannot find a matching service!");
                Toast.makeText(this, "Cannot find a matching service!",
                        Toast.LENGTH_LONG).show();
            }
            else if (matches.size() > 1) {
                // This is really just a sanity check
                // and should never occur in a real life scenario
                Log.d(TAG, "Found multiple matching services!");
                Toast.makeText(this, "Found multiple matching services!",
                        Toast.LENGTH_LONG).show();
            }
            else {
                // Create an explicit intent
                Intent explicit=new Intent(serviceIntent);
                ServiceInfo svcInfo=matches.get(0).serviceInfo;
                ComponentName cn=new ComponentName(svcInfo.applicationInfo.packageName,
                        svcInfo.name);
                explicit.setComponent(cn);
                // Bind using AUTO_CREATE
                if (bindService(explicit, mConnection,  BIND_AUTO_CREATE)){
                    Log.d(TAG, "Bound to Neuos Service");
                } else {
                    Log.d(TAG, "Failed to bind to Neuos Service");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "can't bind to NeuosService, check permission in Manifest");
        }
    }

Your application will also need to create a ServiceConnection class that will handle connection and disconnection responses.

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "Attached.");
            // Once connected, you can cast the binder object 
            // into the proper type 
            mService = INeuosSdk.Stub.asInterface(service);
            //And start interacting with the service.
            try {
                // Register for service callbacks
                mService.registerCallback(mCallback);
                // Initialize the service API with your API key
                mService.initializeNeuos(API_KEY);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This will be called when an Unexpected disconnection happens.
            Log.d(TAG, "Detached.");
        }
    };

More information on bound services can be found in the [Android developer guide](https://developer.android.com/guide/components/bound-services)

#### Register for callbacks

Since the service runs in its own process and not the calling application process, most of the calls inside INeuosSdk.aidl are defined as *oneway*. This effectively creates an asynchronous call into the service process which returns immediately. The service reports results via the INeuosSdkListener.aidl interface.

The **onSdkError(int errorCode, String message)** call is available for reporting errors back to the calling application. As in the example above, best practice is to register the listener(s) after the service is bound and before you initialize it, to make sure that you receive all messages promptly. Unregistering the listener should be performed after your application has received the *onSdkShutDown()* message.

For more information on the AIDL interface, and calling IPC methods, visit the [Android Developer Guide](https://developer.android.com/guide/components/aidl#Calling).

The callback interface is defined in [INeuosSdkListener.aidl](neuosSDK/src/main/aidl/io/neuos/INeuosSdkListener.aidl).

Note that this entire interface is defined as *oneway* as all calls from the service work via a messaging thread, and do not wait for the listener to perform any actions before returning and notifying the next listener in line.

Implementing the interface is as simple as creating a private class deriving from *INeuosSdkListener.Stub()*

    private final INeuosSdkListener mCallback = new INeuosSdkListener.Stub() {
        // Implement your interface here
    }

#### Initialize the SDK with your API key

Once you are ready to start working with the service, you will need to initialize it with your API key by calling **initializeNeuos(API_KEY)** method of the service. 
The service will notify you when ready with the **onSdkInitialized()** callback.

### Session Phase

#### Verify a user is logged in 

After successful initialization, your first step is to verify a user is logged into the Neuos Central application.
You can query the service to get the logged in status of a user.
In case a user is not logged in, launch an intent that will take the user to the login / register screen of the Neuos Central app.

    int status = mService.getUserLoginStatus();
    switch (status){
        case NeuosSDK.LoginStatus.LOGGED_IN:{
            Log.i(TAG, "login: Logged In");
            break;
        }
        case NeuosSDK.LoginStatus.NOT_LOGGED_IN:{
            Log.i(TAG, "login: Not Logged In");
            launchHome();
            break;
        }
    }

To launch the login page of the Neuos app, start an activity with the following intent:

    Intent activityIntent = new Intent("io.neuos.NeuosLogin");

The Neuos login activity will report close and report a result once complete.
You can either listen to that request or check the login status again.

#### Verify a user is calibrated for Neuos

Before Neuos services can be used in any sessions, a user must complete calibration and have personal Neuos models generated for them.
This is done via the Neuos Central app.

Call the service to check the status:
    
    mService.checkUserCalibrationStatus();

Result will be returned via the **onUserCalibrationStatus(int calibrationStatus)** callback. 
In case the user is not calibrated, launch an intent to send the user into the calibration: 

    Intent activityIntent = new Intent("io.neuos.NeuosCalibration");

#### Connect to a Neuos sensor device 

Connecting to a Neuos sensor device, for example a headband, is accomplished by calling **connectSensorDevice(String macAddress)** 
Available in the SDK is the PairingActivity class, which handles scanning and extracting the device's MAC address using builtin CompanionDeviceManager. You can launch the activity using the following code
    
    Intent activityIntent = new Intent("io.neuos.PairDevice");

The activity will dispatch a broadcast once the user has selected a device. You will need to create a BroadcastReceiver that will be called with the result.

    private class DevicePairingBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String address = intent.getStringExtra(NeuosSDK.DEVICE_ADDRESS_EXTRA);
            Log.d(TAG, "Connection Intent : " + address);
            try {
                // Tell the service to initiate a connection to the selected device
                mService.connectSensorDevice(address);
            } catch (RemoteException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

Before launching the activity you will need to register for the broadcast.

    devicePairingBroadcastReceiver = new DevicePairingBroadcastReceiver();
        registerReceiver(devicePairingBroadcastReceiver,
                new IntentFilter(NeuosSDK.IO_NEUOS_DEVICE_PAIRING_ACTION));

The call to **connectSensorDevice(String macAddress)** will initiate a series of callbacks to **onConnectionChanged(int previousConnection ,int currentConnection)**

The values for *previousConnection* and *currentConnection* are defined in the NeuosSDK.java class.
Once the value of *currentConnection* == NeuosSDK.ConnectionState.CONNECTED , you may begin a session. 

#### Verify Signal Quality of device

Once the device is connected you should verify that the user it is receiving proper signal. 
The easiest way is to launch the QA activity bundled along with the app. This frees you from implementing your own version, and provides constants that are verified before the activity returns its result.

Create an intent to launch the screen:

    Intent activityIntent = new Intent("io.neuos.QAScreen");
    
Add extras into the intent to denote you want it to be stand alone
    
    activityIntent.putExtra(NeuosQAProperties.STAND_ALONE , true);
    
Add properties for the screen to verify

    activityIntent.putExtra(NeuosQAProperties.TASK_PROPERTIES ,
                new NeuosQAProperties(NeuosQAProperties.Quality.Good , NeuosQAProperties.INFINITE_TIMEOUT));
                
The activity will call finish() with either RESULT_OK or RESULT_CANCELED, which you can use to determine your next steps.

#### Begin a session

For a calibrated user, call **startPredictionSession(String predictionName)** to begin running the Neuos real-time prediction service. Once the session is ready, a callback to **onPredictionSessionStart()** will happen.

#### Work with session data

At this point, your app will receive results via the **onValueChanged(String key,float value)** callback. Signal QA is reported via **onQAStatus(boolean passed ,int type)** callback.

#### Finish session

When you are ready to complete the session, call **finishSession()**. The session will close and notify your app when done via **onSessionComplete()** callback.
You can use the **INeuosSessionUploadListener** interface to monitor the progress of uploading the session to the Neuos server. (Optional)

### Cleanup

#### Shutdown the SDK

Call **shutdownSdk()** to have Neuos release all of its resources.
Neuos will notify your app via the **onSdkShutDown()** callback.

#### Unbind from the service

Once the SDK is shutdown, you can safely unbind from the service.
