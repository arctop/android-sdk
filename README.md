# Neuos™ Software Development Kit (SDK)

The Neuos SDK public repository contains everything you need to connect your application to Neuos services. 

# Background

Neuos is a noninvasive neural interface technology that is the fruit of deep R&D performed by Arctop Inc. It is fully working, developed, and comes out of proven research — a reference you may find helpful is a peer-reviewed article in Frontiers in Computational Neuroscience where Neuos SDK was used for a personalized audio application: https://www.frontiersin.org/articles/10.3389/fncom.2021.760561/full

The current version of Neuos provides three unique brain data streams: Focus, Enjoyment, and 'The Zone' (aka flow-state). It also provides two streams of body data: Heart Rate and Head Motion. All in real-time! Meaning new data points several times a second. This new data can be used to, for example, monitor brain health from home, create adaptive features that enhance applications by making them "smarter" and more user-centric, or during app development to quantitatively a/b test different  experiences.

In short, Neuos brings a new stream of information direct from brain to computer and it can be used to power all sorts of applications/uses.

One way Neuos achieves its high performance analysis is by calibrating itself to each new user. This allows the brain pattern analysis that Neuos performs to be customized and take into account each person's baseline. More information about the calibration is provided in the section #### Verify a user is calibrated for Neuos. Calibration is required only one-time for each user and takes approximately 10 minutes to complete.

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

# Workflow

> **_NOTE:_**  The SDK is designed to work in a specific flow. 
> The setup phase needs to be done once as long as your application is running. The session phase can be done multiple times.

## Setup Phase

1. [Neuos Permissions](#permissions)
2. [Bind to service](#binding-to-the-service)
3. [Initialize the SDK with your API key](#initialize-the-sdk-with-your-api-key)
4. [Register for callbacks](#register-for-callbacks)

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
Note that Neuos service currently allows only 1 app to connect at a time, so make sure you have only 1 app binding to the SDK at any time.

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
                // Initialize the service API with your API key
                int response = mService.initializeNeuos(API_KEY);
                if ( response == NeuosSDK.ResponseCodes.SUCCESS){
                    // Register for service callbacks
                    response = mService.registerCallback(mCallback);
                    if ( response == NeuosSDK.ResponseCodes.SUCCESS){
                        // Service is ready to work with
                    }
                }
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

#### Initialize the SDK with your API key

You need an API Key to use Neuos SDK in your app. To request your API key, email contact@neuos.io with the subject line: "Neuos API Key Request."

Once you have your API key and are ready to start working with the service, you will need to initialize it with your API key by calling **initializeNeuos(API_KEY)** method of the service. The service will return a response code letting you know if it successfully initialized or if there is an error.

#### Register for callbacks

The service runs in its own process and not the calling application process, so some of the calls inside INeuosSdk.aidl are defined as *oneway*. This effectively creates an asynchronous call into the service process which returns immediately. The service reports results via the INeuosSdkListener.aidl interface.

The **onSdkError(int errorCode, String message)** call is available for reporting errors back to the calling application. 

For more information on the AIDL interface, and calling IPC methods, visit the [Android Developer Guide](https://developer.android.com/guide/components/aidl#Calling).

The callback interface is defined in [INeuosSdkListener.aidl](neuosSDK/src/main/aidl/io/neuos/INeuosSdkListener.aidl).

Note that this entire interface is defined as *oneway* as all calls from the service work via a messaging thread, and do not wait for the listener to perform any actions before returning and notifying the next listener in line.

Implementing the interface is as simple as creating a private class deriving from *INeuosSdkListener.Stub()*

    private final INeuosSdkListener mCallback = new INeuosSdkListener.Stub() {
        // Implement your interface here
    }

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

    Intent activityIntent = new Intent(NeuosSDK.NEUOS_LOGIN);

The Neuos login activity will report close and report a result once complete.
You can either listen to that request or check the login status again.

#### Verify a user is calibrated for Neuos

Before Neuos services can be used in any sessions, a user must complete calibration and have personal Neuos models generated for them. This is done via the Neuos Central app. To calibrate, in the Neuos Central app users will go through a short session:
    
The calibration process is approximately 10 minutes long and asks users to complete six short tasks (1-3 minutes each) while their brain signal is recorded by a compatible headband or other sensor device. At the end of each task users are asked to rank their experience using slider scales and questionairres. 
    
This process is crucial in order for Neuos to learn individual users and adjust its algorithms to be as accurate and robust as possible. Therefore, it is important that users complete this session while they are in a quiet place, with no disruptions, and making sure the headband is positioned properly.

It is best practice that during the calibration tasks each user focuses on the screen as much as they can and do not close their eyes. It is OK to blink normally but otherwise they should try not to make unnecessary movements during the tasks since the headband is very sensitive to motion. Other guidance given for the calibration session includes:
    -Do NOT eat or drink.
    -Do NOT plug in the headband or connect it to any battery while wearing it
    -Do NOT press the tablet’s ‘sleep’ button.
    -Answer questions honestly: the session is designed to calibrate the software to each person so there are no “right” or “wrong” answers.

To verify that a user is calibrated, call the service to check the status:
    
    mService.checkUserCalibrationStatus();

In case the user is not calibrated, launch an intent to send the user into the calibration: 

    Intent activityIntent = new Intent(NeuosSDK.NEUOS_CALIBRATION);

#### Connect to a Neuos sensor device 

Connecting to a Neuos sensor device, for example a headband, is accomplished by calling **connectSensorDevice(String macAddress)** 
Available in the SDK is the PairingActivity class, which handles scanning and extracting the device's MAC address using builtin CompanionDeviceManager. You can launch the activity using the following code
    
    Intent activityIntent = new Intent(NeuosSDK.NEUOS_PAIR_DEVICE);

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

    Intent activityIntent = new Intent(NeuosSDK.NEUOS_QA_SCREEN);
    
Add extras into the intent to denote you want it to be stand alone
    
    activityIntent.putExtra(NeuosQAProperties.STAND_ALONE , true);
    
Add properties for the screen to verify

    activityIntent.putExtra(NeuosQAProperties.TASK_PROPERTIES ,
                new NeuosQAProperties(NeuosQAProperties.Quality.Good , NeuosQAProperties.INFINITE_TIMEOUT));
                
The activity will call finish() with either RESULT_OK or RESULT_CANCELED, which you can use to determine your next steps.

#### Begin a session

For a calibrated user, call **startPredictionSession(String predictionName)** to begin running the Neuos real-time prediction service.
You can find the predictions in **NeuosSDK.Predictions**

#### Work with session data

At this point, your app will receive results via the **onValueChanged(String key,float value)** callback. 

Results are given in the form of values from 0-100 for Focus, Enjoyment, and The Zone. The neutral point for each user is at 50, meaning that values above 50 reflect high levels of the measured quantity, for example a 76 in Focus is a high level of Focus, a 99 in The Zone is a near perfect "flow state." Values below 50 represent the opposite, meaning lack of focus or lack of enjoyment. For example a 32 in Focus is a low level that reflects the user not paying attention, a 12 in Enjoyment means the user really dislikes whatever is happening. Low Zone levels reflect being distracted or generally not immersed in an experience.

For Heart Rate the value is given per minute. For example, 56 means 56 beats per minute, 82 means 82 beats per minute.
For Head Motion the value is given in steps from 1-4 where 1 is static i.e. the user is not moving their head at all, while 4 is active, i.e. the user is moving their head a lot. 4 is a near constant movement of the head while 1 is essentially no movement, 2 and 3 are somewhere in between with 2 being a lower amount of movement than 3. 

Values of -1 or NaNs should be ignored as these reflect low confidence periods of analysis. This can occur for any reason, including a momentary lapse in sensor connection or a brain response that is anomalous. Neuos is strict and always prefers to say "I don't know" with a -1 or NaN rather than imply that it knows by giving a value which has a high chance of being inaccurate. If you notice an excess of -1s or NaNs in your data please contact Arctop for support as these values should occur only very limitedly.

Signal QA is reported via **onQAStatus(boolean passed ,int type)** callback. 

#### Finish session

When you are ready to complete the session, call **finishSession()**. The session will close and notify your app when done via **onSessionComplete()** callback.
You can use the **INeuosSessionUploadListener** interface to monitor the progress of uploading the session to the Neuos server. (Optional)

### Cleanup

#### Shutdown the SDK

Call **shutdownSdk()** to have Neuos release all of its resources.

#### Unbind from the service

Once the SDK is shutdown, you can safely unbind from the service.
