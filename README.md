# Arctop Native Android Software Development Kit (SDK)

The Arctop SDK repository contains everything you need to connect your application to Arctop's services. 

# Background

The Arctop SDK is a noninvasive neural interface technology that is the result of deep R&D performed by Arctop, Inc. It is fully functional, developed, and comes from proven research. A reference you may find helpful is this peer-reviewed article in Frontiers in Computational Neuroscience in which the Arctop SDK was used for a personalized audio application: https://www.frontiersin.org/articles/10.3389/fncom.2021.760561/full.

The current version of the Arctop SDK provides two unique cognition data streams: focus and enjoyment. It also provides body data streams including blinks, heart rate, and heart rate variability. All data streams are provided in real-time, meaning you’ll receive new data points several times per second. 

In short, Arctop brings new streams of personalized brain data directly to you, enabling endless creative development with real-time metrics. Examples of how this data can be used include: creating personally adaptive games and training scenarios for more immersive user experiences, enabling new modes of communication and accessibility features, gaining play-by-play insights into how users respond to your product, and tracking personal brain health measurements from home.

One way Arctop achieves its high performance analysis is by calibrating itself to each new user. This allows our models to be individually customized to your unique brain data baseline and ensure we can deliver personalized data. To unlock your personalized model, you’ll complete a one-time 10-minute calibration, consisting of a series of tasks and questions in the Arctop mobile app. After this one-time calibration, your baseline will be set to provide you with real-time cognition metrics for endless usage. For more information about calibration, see the section titled: [Verify a User is Calibrated for Arctop](https://github.com/arctop/android-sdk#verify-a-user-is-calibrated-for-arctop). 

# Installation

To add the SDK to your project use **ONE** of the following methods:

1. Use [JitPack](https://jitpack.io/private#arctop/android-sdk) to install the SDK as a dependency with gradle or maven. (Recommended) 
2. Clone the repository locally, and add it as a module into your Android project.

# Package Structure

The SDK contains the following components:

- The *NeuosSDK.java* class with all constants used to communicate with your application.

- A set of AIDL files, which define the interface of communication with the service.
    - *INeuosSdk.aidl* provides the public service API accessible by your app.
    - *INeuosSdkListener.aidl* defines the callback interface by which the service provides responses and notifications back to your app.
    - *INeuosQAListener.aidl* defines a callback interface that provides QA values from the sensor device to your app.
    - *INeuosSessionUploadListener.aidl* defines a callback interface that provides session upload monitoring.      

# Workflow

> **_NOTE:_**  The SDK is designed to work in a specific flow. 
> The Setup Phase only needs to be done once, as long as your application is running. The Session Phase can be done multiple times.

## Setup Phase

1. [Prerequisites](#prerequisites)
2. [Permissions](#permissions)
3. [Bind to Service](#binding-to-the-service)
4. [Initialize the SDK with Your API Key](#initialize-the-sdk-with-your-api-key)
5. [Register for Callbacks](#register-for-callbacks)

## Session Phase

1. [Verify That a User is Logged In](#verify-a-user-is-logged-in)
2. [Verify That a User Has Been Calibrated for Arctop](#verify-a-user-is-calibrated-for-arctop)
3. [Connect to an Arctop Sensor Device](#connect-to-an-arctop-sensor-device)
4. [Verify Signal Quality of Device](#verify-signal-quality-of-device)
5. [Begin a Session](#begin-a-session)
6. [Work With Session Data](#work-with-session-data)
7. [Finish Session](#finish-session)

## Cleanup

1. [Shutdown the SDK](#shutdown-the-sdk)
2. [Unbind From the Service](#unbind-from-the-service)

### Setup Phase

#### Prerequisites

To use the Arctop SDK, you'll need to install the Arctop mobile app on your mobile device. The Arctop app is available on both the App Store (iOS) and Google Play (Android) and can be found by searching for "Arctop".  

After downloading the mobile app, use the “Sign Up” screen to create an account. Follow instructions in the “Supplementary User Instructions'' document provided to you for guidance on how to set up and use the mobile app for Arctop Streaming.  

#### Permissions

Before binding to Arctop and receiving data, you will need to request permissions from the user.
In your *AndroidManifest.XML*, declare that you will be using the NEUOS_DATA permission:

    <uses-permission android:name="io.neuos.permission.NEUOS_DATA" />

Then, at runtime, verify that you have that permission or request it from the user, as described in this section:

[Requesting Runtime Permissions Reference in Android Developer Guide](https://developer.android.com/training/permissions/requesting)

#### Binding to the Service

The SDK revolves around a single service entry point that your application will need to bind to.
Note that Arctop's service currently allows only 1 app to connect at a time, so make sure you have only 1 app binding to the SDK at any time.

In order to perform the bind, you will need to declare that your application will query the service package.
This is done by adding this snippet into your *AndroidManifest.XML*

    <queries>
        <package android:name="io.neuos.central" />
    </queries>

[Query Element Reference in Android Geveloper Guide](https://developer.android.com/guide/topics/manifest/queries-element)

Then, in your code, locate the service, and perform the binding.

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
                    response = mService.registerSDKCallback(mCallback);
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

More information on bound services can be found in the [Android Developer Guide](https://developer.android.com/guide/components/bound-services)

#### Initialize the SDK with Your API Key

In order to use the Arctop SDK with your app, you will need to create an API key. To do so, please submit a request via the Arctop DevKit form provided to you in the “Welcome” email. Feel free to contact support@arctop.com with any questions you may have.

Once you have your API key and are ready to start working with the service, you will need to initialize it with your API key by calling **initializeNeuos(API_KEY)** method of the service. The service will return a response code letting you know if it successfully initialized or if there is an error.

#### Register for Callbacks

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

#### Verify a User is Logged In 

After successful initialization, your first step is to verify a user is logged into the Arctop mobile application.
You can query the service to get the logged in status of a user.
In case a user is not logged in, launch an intent that will take the user to the Log In / Sign Up screen of the Arctop mobile app.

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

To launch the login screen of the Arctop app, start an activity with the following intent:

    Intent activityIntent = new Intent(NeuosSDK.NEUOS_LOGIN);

The Arctop login activity will report close and report a result once complete.
You can either listen to that request or check the login status again.

#### Verify a User is Calibrated for Arctop

Before Arctop services can be used in any sessions, a user must complete calibration to generate their personal Arctop model. This is done via the Arctop mobile app. 

Calibration in the Arctop mobile app will be approximately 10 minutes long and only needs to be completed once per user. It consists of five short tasks (1-3 minutes each) and is performed while wearing a compatible headwear device to record brain signals throughout. At the end of each task, users are asked to rank their experience using slider scales and short questionnaires. 
    
This process is important for Arctop’s software to learn individual users' unique patterns and tune its algorithms to be as accurate and robust as possible. 

Users should follow the tips listed below for best practice in completing calibration.

*Before calibration:*

-Ensure you are in a quiet area, where you will not be interrupted for 10 minutes.

-Unplug your headwear and mobile device from any chargers.

*During calibration:*

-Sit still, as the headwear sensors are sensitive to motion. Do not eat, drink, or close your eyes. It is alright to blink normally.

-Do not multitask or exit the Arctop app. Focus all of your efforts on the tasks presented.

-Answer all questions honestly.


To verify that a user is calibrated, call the service to check the status:
    
    mService.checkUserCalibrationStatus();

In case the user is not calibrated, launch an intent to send the user into the calibration: 

    Intent activityIntent = new Intent(NeuosSDK.NEUOS_CALIBRATION);
    activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(activityIntent)

#### Connect to an Arctop Sensor Device 

Connecting to an Arctop sensor device, such as the headwear provided, is accomplished by calling **connectSensorDevice(String macAddress)** .
Available in the SDK is the PairingActivity class, which handles scanning and extracting the device's MAC address using builtin CompanionDeviceManager. You can launch the activity using the following code.
    
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

The call to **connectSensorDevice(String macAddress)** will initiate a series of callbacks to **onConnectionChanged(int previousConnection ,int currentConnection)** .

The values for *previousConnection* and *currentConnection* are defined in the NeuosSDK.java class.
Once the value of *currentConnection* == NeuosSDK.ConnectionState.CONNECTED , you may begin a session. 

#### Verify Signal Quality of Device

Once the device is connected, you should verify that the user's headwear is receiving the proper signal. 
The easiest way to verify this is to launch the in-app QA activity on the mobile device being used. This frees you from implementing your own version, and provides constants that are verified before the activity returns its result.
The QA screen in the mobile app also verifies that the user isn't actively charging their device, which would create noise in the signal readings. 

Create an intent to launch the screen:

    Intent activityIntent = new Intent(NeuosSDK.NEUOS_QA_SCREEN);
    
Add extras into the intent to denote you want it to be stand alone (required, or it won't work).
    
    activityIntent.putExtra(NeuosQAProperties.STAND_ALONE , true);
    
Add properties for the screen to verify.

    activityIntent.putExtra(NeuosQAProperties.TASK_PROPERTIES ,
                new NeuosQAProperties(NeuosQAProperties.Quality.Good , NeuosQAProperties.INFINITE_TIMEOUT));

The [QAProperties](neuosSDK/src/main/java/io/neuos/NeuosQAProperties.kt) class contains further explanation on different settings you can use to define your user's QA experience.

Optionally, you can add a flag to run the screen in debug mode. This is helpful when developing your apps. 
It provides 2 features that aren't available in a release setting:

1. There will be a "Continue" button at the bottom of the screen, allowing you to simulate successful QA.
2. Once you dismiss the "Please unplug your device." dialog, it will not show up again, and you can continue working with the device connected.

Adding the flag is done as follows:

    activityIntent.putExtra(NeuosQAProperties.RUN_IN_DEBUG , true);
   
                
The activity will call finish() with either RESULT_OK or RESULT_CANCELED, which you can use to determine your next steps.

#### Begin a Session

For a calibrated user, call **startPredictionSession(String predictionName)** to begin running the Arctop real-time prediction service.
You can find the predictions in **NeuosSDK.Predictions** .

#### Work with Session Data

At this point, your app will receive results via the **onValueChanged(String key,float value)** callback. 

Results are given in the form of values from 0-100 for focus and enjoyment. The neutral point for each user is at 50, meaning that values above 50 reflect high levels of the measured quantity. For example, a 76 in focus is a high level of focus, while a 99 is  nearly the highest focus that can be achieved. Values below 50 represent the opposite, meaning lack of focus or lack of enjoyment. For example, a 32 in focus is a lower level that reflects the user may not be paying much attention, while a 12 in enjoyment can mean the user dislikes the current experience.

Blink data is recorded as a binary. The presence of a blink will be indicated by a “1” in that column. Blink data for each individual eye will be provided in a future addition to the dataset. 

Arctop’s SDK is strict and always prefers to say "I don't know", producing a -1 or NaN value, rather than providing a value that may be inaccurate. Values of -1 or NaNs should be ignored as these reflect low confidence periods of analysis. This can occur for any reason, including a momentary lapse in sensor connection or a brain response that is anomalous. If you notice an excess of -1s or NaNs in your data, please contact Arctop for support as these values should occur on a limited basis.

Signal QA is reported via **onQAStatus(boolean passed ,int type)** callback. If QA failed during the analysis window, the **passed** parameter will be false, and the type of failure will be reported in **type**. Valid types are defined in **QAFailureType** class inside [NeuosSDK.java](neuosSDK/src/main/java/io/neuos/NeuosSDK.java).

#### Finish Session

When you are ready to complete the session, call **finishSession()**. The session will close and notify your app when done via **onSessionComplete()** callback.
You can use the **INeuosSessionUploadListener** interface to monitor the progress of uploading the session to the Arctop server. (Optional)

### Cleanup

#### Shutdown the SDK

Call **shutdownSdk()** to have Arctop release all of its resources.

#### Unbind From the Service

Once the SDK is shutdown, you can safely unbind from the service.

# Using the SDK with a Non-Android Client

Arctop provides a LAN webserver that allows non-Android clients access to the SDK data. For more info see [Stream Server Docs](Arctop-Stream.md).

It is highly recomended to first read through this documentation to have a better understanding of the SDK before trying to work with the stream server.
