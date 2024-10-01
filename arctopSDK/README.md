# Arctop™ Software Development Kit (SDK)

Arctop's SDK repository contains everything you need to connect your application to Arctop’s services. 

## Background

Arctop is a software company that makes real-time cognition decoding technology. Arctop's software applies artificial intelligence to electric measurements of brain activity — translating people’s feelings, reactions, and intent — into actionable data that empower applications with new capabilities. Since its founding in 2016, Arctop has worked to develop a cross-platform SDK that provides noninvasive brain-computer interface capabilities. The SDK is being developed continuously and is the result of  deep R&D, such as [this peer-reviewed study](https://www.frontiersin.org/articles/10.3389/fncom.2021.760561/full) published in _Frontiers in Computational Neuroscience_ where Arctop's SDK was used in a personalized audio application.

The current version of the Arctop SDK provides three unique cognition data streams: focus, enjoyment, flow, and sleep state. It also provides body data streams including eye blinks, heart rate, and heart rate variability. All data streams are provided in real-time, meaning applications receive new data points several times per second. 

In short, Arctop's SDK gives the ability to add new streams of personalized cognition data directly into applications.

Examples of how this data can be used include:
* Developing neurology, psychiatry, and psychology applications.
* Enabling new modes of communication and accessibility features.
* Creating  high performance training scenarios  that are more effective with cognition data in the feedback loop.
* Building generative AI applications to improve foundation models and add empathy and responsiveness to individual preferences in the moment.
* Gaining play-by-play insights into how users respond to products.
* Tracking personal brain health measurements from home.
* Developing novel headwear for gamers.
* Creating adaptive audio applications that tailor sound to user mood.

One way Arctop achieves its high performance analysis is by calibrating data processing models to each new user. This is done through a one-time 10-minute session in Arctop's mobile app that consists of a series of interactive tasks and questions. The calibration process is important since it allows Arctop's AI models of brain function to be individually customized to each user's unique brain data baseline and deliver personalized dynamics measures that are accurate. 

After the one-time calibration, real-time cognition metrics for endless usage are unlocked. For more information about calibration, see the section titled [Verify That a User Has Been Calibrated for Arctop](#2-verify-that-a-user-has-been-calibrated-for-arctop) within this file.


## Installation

To add the SDK to your project use **ONE** of the following methods:

1. Use [JitPack](https://jitpack.io/private#arctop/android-sdk) to install the SDK as a dependency with gradle or maven. (Recommended) 
2. Clone the repository locally, and add it as a module into your Android project.


## Package Structure
The SDK contains the following components.

* The _ArctopSDK.java_ class with all constants used to communicate with your application
  
* A set of AIDL files, which define the interface of communication with the service:
  * _IArctopSdk.aidl_ provides the public service API accessible by your app.
  * _IArctopSdkListener.aidl_ defines the callback interface by which the service provides responses and notifications back to your app.
  * _IArctopQAListener.aidl_ defines a callback interface that provides QA values from the sensor device to your app.
  * _IArctopSessionUploadListener.aidl_ defines a callback interface that provides session upload monitoring.      
  
## Workflow

> **_GENERAL NOTE:_**  The SDK is designed to work in a specific flow. 
> The Setup Phase only needs to be done once, as long as your application is running. The Session Phase can be done multiple times.



### Setup Phase

1. [Prerequisites](#1-prerequisites)
2. [Permissions](#2-permissions)
3. [Bind to the Service](#3-bind-to-the-service)
4. [Initialize the SDK with Your API Key](#4-initialize-the-sdk-with-your-api-key)
5. [Register for Callbacks](#5-register-for-callbacks)

### Session Phase

1. [Verify That a User is Logged In](#1-verify-that-a-user-is-logged-in)
2. [Verify That a User Has Been Calibrated for Arctop](#2-verify-that-a-user-has-been-calibrated-for-arctop)
3. [Connect to an Arctop Sensor Device](#3-connect-to-an-arctop-sensor-device)
4. [Verify Signal Quality of Device](#4-verify-signal-quality-of-device)
5. [Begin a Session](#5-begin-a-session)
6. [Work with Session Data](#6-work-with-session-data)
7. [Finish Session](#7-finish-session)

### Cleanup Phase

1. [Shutdown the SDK](#1-shutdown-the-sdk)
2. [Unbind From the Service](#2-unbind-from-the-service)


## Phase Instructions
### Setup Phase

#### 1. Prerequisites

###### Mobile App
To use the Arctop SDK, you'll need to install the Arctop  app on your mobile device. The Arctop app is available on both the App Store (iOS) and Google Play (Android) and can be found by searching "Arctop".

After downloading the mobile app, use the Sign Up screen to create an account. Follow instructions in the _Supplementary User Instructions_ document provided to you via email for guidance on how to set up and use the mobile app for Arctop streaming.  

###### API Key
You will also need to create an API key in order to use the Arctop SDK with your app. To do so, please submit a request via the Arctop DevKit form provided to you in the “Welcome” email. Feel free to contact us at support@arctop.com with any questions you may have.

#### 2. Permissions
    
Before binding to Arctop and receiving data, you will need to request permissions from the user.
In your _AndroidManifest.XML_, declare that you will be using the ARCTOP_DATA permission:
    
        <uses-permission android:name="com.arctop.permission.ARCTOP_DATA" />
    
Then, at runtime, verify that you have that permission, or request it from the user, as per:
    
[Requesting Runtime Permissions Reference in Android Developer Guide](https://developer.android.com/training/permissions/requesting)
 
#### 3. Bind to the Service

The SDK revolves around a single service entry point that your application will need to bind to.
Note that Arctop's service currently allows only 1 app to connect at a time, so make sure you have only 1 app binding to the SDK at any time.
    
In order to perform the bind, you will need to declare that your application will query the service package.
This is done by adding this snippet into your _AndroidManifest.XML_  
    
        <queries>
            <package android:name="com.arctop.app" />
        </queries>
    
[Query Element Reference in Android Developer Guide](https://developer.android.com/guide/topics/manifest/queries-element)
    
Then, in your code, locate the service, and perform the binding.
    
        void doBindService() {
            try {
                // Create an intent based on the class name
                Intent serviceIntent = new Intent(IArctopSdk.class.getName());
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
                        Log.d(TAG, "Bound to Arctop Service");
                    } else {
                        Log.d(TAG, "Failed to bind to Arctop Service");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "can't bind to ArctopService, check permission in Manifest");
            }
        }

Your application will also need to create a _ServiceConnection_ class that will handle connection and disconnection responses.
    
        private ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                Log.d(TAG, "Attached.");
                // Once connected, you can cast the binder object 
                // into the proper type 
                mService = IArctopSdk.Stub.asInterface(service);
                //And start interacting with the service.
                try {
                    // Initialize the service API with your API key
                    int response = mService.initializeArctop(API_KEY);
                    if ( response == ArctopSDK.ResponseCodes.SUCCESS){
                        // Register for service callbacks
                        response = mService.registerSDKCallback(mCallback);
                        if ( response == ArctopSDK.ResponseCodes.SUCCESS){
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
    
More information on bound services can be found in the [Android Developer Guide](https://developer.android.com/guide/components/bound-services).

#### 4. Initialize the SDK With Your API Key

You need an API key to use the Arctop SDK in your app (see [Prerequisites](#1-prerequisites)). Once you have your API key and are ready to start working with the service, you will need to initialize the SDK.
    
This can be done with your API key by calling **initializeArctop(API_KEY)** method of the service. The service will return a response code letting you know if it successfully initialized or if there is an error.   
        
#### 5. Register for Callbacks
    
The service runs in its own process and not the calling application process, so some of the calls inside _IArctopSdk.aidl_ are defined as _oneway_. This effectively creates an asynchronous call into the service process which returns immediately. The service reports results via the _IArctopSdkListener.aidl_ interface.
    
The **onSdkError(int errorCode, String message)** call is available for reporting errors back to the calling application. 
    
For more information on the AIDL interface, and calling IPC methods, visit the [Android Developer Guide](https://developer.android.com/guide/components/aidl#Calling).
    
The callback interface is defined in [_IArctopSdkListener.aidl_](https://github.com/arctop/android-sdk/blob/25fdc767f6f9ca0b1f5d33a6515bc7f954267566/arctopSDK/src/main/aidl/com/arctop/IArctopSdkListener.aidl).
    
Note that this entire interface is defined as _oneway_ as all calls from the service work via a messaging thread, and do not wait for the listener to perform any actions before returning and notifying the next listener in line.
    
Implementing the interface is as simple as creating a private class deriving from *IArctopSdkListener.Stub()*
    
        private final IArctopSdkListener mCallback = new IArctopSdkListener.Stub() {
            // Implement your interface here
        }

### Session Phase

#### 1. Verify That a User is Logged In 

After successful initialization, your first step is to verify a user is logged into the Arctop mobile application.

You can query the service to get the logged in status of a user.
In case a user is not logged in, launch an intent that will take the user to the Login / Sign Up screen of the Arctop mobile app.

    int status = mService.getUserLoginStatus();
    switch (status){
        case ArctopSDK.LoginStatus.LOGGED_IN:{
            Log.i(TAG, "login: Logged In");
            break;
        }
        case ArctopSDK.LoginStatus.NOT_LOGGED_IN:{
            Log.i(TAG, "login: Not Logged In");
            launchHome();
            break;
        }
    }

To launch the login page of the Arctop app, start an activity with the following intent:

    Intent activityIntent = new Intent(ArctopSDK.ARCTOP_LOGIN);

The Arctop login activity will report close and report a result once complete.
You can either listen to that request or check the login status again.

#### 2. Verify That a User Has Been Calibrated for Arctop

Before Arctop services can be used in any sessions, a user must complete calibration to generate their personal Arctop model. This is done via the Arctop mobile app. 

Calibration takes approximately 10 minutes and only needs to be completed once per user. It consists of five short tasks (1-3 minutes each) and is performed while wearing a compatible headwear device to record brain signals throughout. At the end of each task, users are asked to rank their experience using slider scales and short questionnaires.

This process is important for Arctop’s software to learn individual users' unique patterns and tune its algorithms to be as accurate and robust as possible. 

The best practices users should follow in completing calibration are listed below.
* Before starting the calibration session:
    * Go to a quiet place where you will not be interrupted for 10 minutes.
    * Unplug your headwear and mobile device from any chargers.
* During calibration:
    * Try to move as little as possible. Headwear sensors are very sensitive to motion.
    * Do not eat, drink, or close your eyes. It is alright to blink normally.
    * Do not multitask or exit the Arctop app. Focus all of your efforts on the tasks presented. 
    * Complete the session within one sitting and in the same location. Moving around too much during calibration will impact results. 
    * Answer all questions honestly, related to how you felt during the tasks. Calibration takes into account individual user feedback so answer as accurately as you can.
    
To verify that a user is calibrated, call the service to check the status:
        
        mService.checkUserCalibrationStatus();
    
In case the user is not calibrated, launch an intent to send the user into the calibration: 
    
        Intent activityIntent = new Intent(ArctopSDK.ARCTOP_CALIBRATION);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activityIntent)

#### 3. Connect to an Arctop Sensor Device 
    
Connecting to an Arctop sensor device, for example a headband, is accomplished by calling **connectSensorDevice(String macAddress)**. 
Available in the SDK is the _PairingActivity_ class, which handles scanning and extracting the device's MAC address using builtin CompanionDeviceManager. You can launch the activity using the following code:
        
        Intent activityIntent = new Intent(ArctopSDK.ARCTOP_PAIR_DEVICE);
    
The activity will dispatch a broadcast once the user has selected a device. You will need to create a BroadcastReceiver that will be called with the result.
    
        private class DevicePairingBroadcastReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                String address = intent.getStringExtra(ArctopSDK.DEVICE_ADDRESS_EXTRA);
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
                    new IntentFilter(ArctopSDK.IO_ARCTOP_DEVICE_PAIRING_ACTION));
    
The call to **connectSensorDevice(String macAddress)** will initiate a series of callbacks to **onConnectionChanged(int previousConnection ,int currentConnection)**
    
The values for _previousConnection_ and _currentConnection_ are defined in the _ArctopSDK.java_ class.
Once the value of _currentConnection == ArctopSDK.ConnectionState.CONNECTED_, you may begin a session. 

#### 4. Verify Signal Quality of Device

Once the device is connected, you should verify that the user's headwear is receiving proper signal. 

The easiest way is to launch the QA activity bundled along with the app. This frees you from implementing your own version, and provides constants that are verified before the activity returns its result.
The screen also verifies that the user isn't actively charging their device, which creates noise in the signal readings. 
    
Create an intent to launch the screen:
    
        Intent activityIntent = new Intent(ArctopSDK.ARCTOP_QA_SCREEN);
        
Add extras into the intent to denote you want it to be stand alone (required, or it won't work):
        
        activityIntent.putExtra(ArctopQAProperties.STAND_ALONE , true);
        
Add properties for the screen to verify:
    
        activityIntent.putExtra(ArctopQAProperties.TASK_PROPERTIES ,
                    new ArctopQAProperties(ArctopQAProperties.Quality.Good , ArctopQAProperties.INFINITE_TIMEOUT));
    
The [_ArctopQAProperties_](https://github.com/arctop/android-sdk/blob/25fdc767f6f9ca0b1f5d33a6515bc7f954267566/arctopSDK/src/main/java/com/arctop/ArctopQAProperties.kt) class contains further explanation on different settings you can use to define your user's QA experience.
    
Optionally, you can add a flag to run the screen in debug mode. This is helpful when developing your apps. 
It provides 2 features that aren't available in a release setting:

1. There will be a "Continue" button at the bottom of the screen, allowing you to simulate successful QA.
2. Once you dismiss the "Please unplug your device" dialog, it will not show up again, and you can continue working with the device connected.
    
Adding the flag is done as follows:
    
        activityIntent.putExtra(ArctopQAProperties.RUN_IN_DEBUG , true);
       
                    
The activity will call finish() with either RESULT_OK or RESULT_CANCELED, which you can use to determine your next steps.

#### 5. Begin a Session
    
For a calibrated user, call **startPredictionSession(String predictionName)** to begin running the Arctop real-time prediction service.
You can find the predictions in **ArctopSDK.Predictions**.

#### 6. Work with Session Data

At this point, your app will receive results via the **onValueChanged(String key,float value)** callback. 

Users will also be provided with a post-session report and CSV files containing metric data, timestamps, and tags from their most recent session. Reports and CSV files for each session will automatically upload to their Developer Portal. Users can access their centralized session history within their Developer Portal at any time. 

Arctop's focus, enjoyment, flow, sleep, and eye blink metrics are derived exclusively from brain data, while heart rate and heart rate variability metrics are derived from body data. 

Focus, enjoyment, and flow data is provided within the "...Cognition" CSV file. These values range from 0-100. The neutral point for each user is at 50, meaning that values above 50 reflect high levels of the measured quantity. For example, a 76 in focus is a high level of focus, while a 99 is nearly the highest focus that can be achieved. Values below 50 represent the opposite, meaning lack of focus or lack of enjoyment or lack of flow. For example, a 32 in focus is a lower level that reflects the user may not be paying much attention, while a 12 in enjoyment can mean the user dislikes the current experience. A value of 23 in flow means that the user is not in a high flow state. 

Sleep data is presented in binary values of 0 or 1 in the "Sleep Detection" column of the provided CSV data file ("...Sleep"). This information tells whether a user is detected to be asleep or awake at each timestamp, with the awake state indicated by a 0 and asleep state indicated by a 1. Additional sleep metrics will be provided in a future version. If sleep onset (0->1) is detected during a session, an automatic tag will be added at that timestamp ("Sleep Onset"). Focus, enjoyment, flow, and blink metrics are not yet validated during the asleep-state, so will be displayed as NaN until the awake state is detected. At the timestamp of this transition (1->0), another automatic tag will be added ("Wake Up") and metrics previously disabled will resume streaming. No report will be generated for sleep sessions at this time. 

Eye blink values are also recorded as a binary. The presence of a blink will be indicated by a value of 1 within the "...Blinks" CSV data file. Blink data for each individual eye will be provided in a future version.

Within the "...Heart Rate" CSV file, heart rate data is provided in units of beats per minute and heart rate variability data is provided in units of milliseconds. 

Any tags added during a session will be provided with their timestamps corresponding to when the user initiated the tag creation. This data is displayed in the "...Tags" CSV file. This file will only be present if tags were added during the session.

Excessively noisy data that cannot be decoded accurately in Arctop’s SDK is represented as NaN. Values of NaNs should be ignored as these reflect low confidence periods of analysis. This can occur for many reasons, such as a lapse in sensor connection to the skin. If you notice an excess of NaNs in your data, please contact Arctop for support as these values typically only occur on a limited basis.
    
Signal QA is reported via **onQAStatus(boolean passed ,int type)** callback. If QA failed during the analysis window, the **passed** parameter will be false, and the type of failure will be reported in **type**. Valid types are defined in **QAFailureType** class inside [ArctopSDK.java](https://github.com/arctop/android-sdk/blob/25fdc767f6f9ca0b1f5d33a6515bc7f954267566/arctopSDK/src/main/java/com/arctop/ArctopSDK.java).

#### 7. Finish Session
    
When you are ready to complete the session, call **finishSession()**. The session will close and notify your app when done via **onSessionComplete()** callback.
You can use the **IArctopSessionUploadListener** interface to monitor the progress of uploading the session to the Arctop server. (Optional)

### Cleanup Phase

#### 1. Shutdown the SDK
    
Call **shutdownSdk()** to have Arctop release all of its resources.

#### 2. Unbind From the Service
    
Once the SDK is shutdown, you can safely unbind from the service.
