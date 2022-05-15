#Neuos™ Stream Server

The purpose of the stream server is to provide a platform independent API.
The server operates on the user's LAN, and allows authorized clients access to the realtime prediction data that Neuos™ provides.

##General Structure

The Neuos™ Central app launches a socket server on the LAN it is connected to. It publishes the service for discovery, 
and awaits connections from clients.

When a client connects, an authentication handshake is performed, and once successful, the server will transmit the data 
received over the network stream. The data is identical to what a client connecting directly to the Neuos™ SDK can receive
via the INeuosSdkListener interface.

The data is transmitted as JSON objects, allowing easy interpretation in almost any programming language.

##Stream data structure

Since the server is implemented on Android, it uses a BIG ENDIAN byte order.

All data is sent with a leading 2 bytes defining the size of the following message as an unsigned short / UInt16.
To properly read the stream, a developer should first read 2 byte, convert the endianness if needed and convert to a UShort.
This will provide the length in bytes of the following message. At that point you can fully read X bytes off the stream.
Those X bytes should then be converted to a UTF8 string, that will contain the JSON object.

##Command Structure

Every command that is sent between the server and the client follows the same structure.

1. A mandatory *command* field, describing the type of command.
2. Optional values defined per command (see ...)
3. Timestamp ( only available within commands sent as part of the stream ) as a long int (Unix MS)

an example of an object notifying the client of a new value received for enjoyment:

    {"command" : "valueChange" , "timestamp" : 123612321 , "key" : "enjoyment" , "value" : 33.4442}

All the available commands, values, and object key are listed in the [NeuosStreamService.kt](neuosSDK/src/main/java/io/neuos/NeuosStreamService.kt) file.

##Connection flow

####Connect to server
   
The Neuos™ Central app provides its IP / Port on the SDK Stream page.
Your client will need to connect to this address. 
The server also publishes itself on the LAN as a service,
by the name of *NeuosService* and protocol of *_http._tcp*

####Send authentication request 

Once connected the server awaits your client expecting to receive a JSON object containing your application's API key.
The expected JSON contains 2 fields, once defining the command and another holding the value of the key.

    {"command" : "auth" , "apiKey" : "yourkeystringhere"}

The server will reply with a command of either "auth-success" or "auth-failed".
In the event of a failed authentication, the connection will be closed, and you can try again.
If the authentication succeeds, the server will begin transmitting events. 

#### Working with the stream

Once the server starts streaming commands, you should read the stream to extract the data.

As described before, the server writes an unsigned short (16 bit uint) into the stream that tells you the size of the next command in bytes.
This allows you to read the next command fully, and convert it into a string/JSON for processing. 
See [Stream data structure](#stream-data-structure) section for full details.

For a better understanding of commands, values, and constants, please review the [NeuosSDK.java](neuosSDK/src/main/java/io/neuos/NeuosSDK.java) and [INeuosSdkListener.aidl](neuosSDK/src/main/aidl/io/neuos/INeuosSdkListener.aidl) files

### Session complete

When the user finishes the session on the Neuos™ central app, the server will send out a "sessionComplete" command to the client.
This will be the final message before the server is shut down. This is your notification to release all resources and shut down the connection on the client's side.

## C# example (Based on a Unity 3D client)

The following code illustrates connecting and reading values using a C# Unity3D client.

    // Connects to the socket server
    public void ConnectSocket()
    {
        try
        {
            // Generate a server address
            IPEndPoint serverAddress = new IPEndPoint(IPAddress.Parse(serverIpInput.text), serverPort);
            // Construct a TCP socket  
            m_Socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            // Connect to the address
            m_Socket.Connect(serverAddress);
            // Send auth command
            SendAuth();
        }
        catch (FormatException e)
        {
            errorField.text = e.Message;
        }
        catch (SocketException ex)
        {
            errorField.text = ex.Message;

        }
    }
    // Sends the Auth Command to the server
    private void SendAuth()
    {
        // Using JSON.Net to produce JSON
        var JObject = new JObject();
        JObject.Add(new JProperty("command", "auth"));
        JObject.Add(new JProperty("apiKey", API_KEY));
        string toSend = JObject.toString();
        // Calculate the byte count of our JSON object
        ushort toSendLen = (ushort)Encoding.UTF8.GetByteCount(toSend);
        // Get the bytes from the JSON object to send
        byte[] toSendBytes = Encoding.UTF8.GetBytes(toSend);
        // Get the bytes describing the length
        byte[] toSendLenBytes = BitConverter.GetBytes(toSendLen);
        // Make sure the length bytes are in BIG_ENDIAN
        if (BitConverter.IsLittleEndian)
            Array.Reverse(toSendLenBytes);
        // Send the size bytes
        m_Socket.Send(toSendLenBytes);
        // Send the message bytes
        m_Socket.Send(toSendBytes);
        // Await response from server
        GetAuthResponse();
    }
    // Gets a single message from the stream
    private string GetMessage()
    {
        // Receiving
        byte[] m_CommandLength = new byte[2];
        // Read the next command's length
        m_Socket.Receive(m_CommandLength);
        // Make sure it is in BIG_ENDIAN
        if (BitConverter.IsLittleEndian)
            Array.Reverse(m_CommandLength);
        // Convert to unsigned short
        int rcvLen = BitConverter.ToUInt16(m_CommandLength, 0);
        // the from stream into buffer the length we just got
        m_Socket.Receive(m_recBuffer , rcvLen , SocketFlags.None);
        // convert it to a string that represents the JSON object
        string rcv = Encoding.UTF8.GetString(m_recBuffer, 0, rcvLen);
        return rcv;
    }
    // Checks the response from the auth command
    private void GetAuthResponse()
    {
        var msg = GetMessage();
        var response = JObject.Parse(msg);
        var commandValue = ((string)response.Property("command")?.Value);
        if (commandValue == "auth-success")
        {
            IsConnected = true;
            /// you are sucessfully connected, start reading the stream in 
            /// some update loop
        }
        else
        {
            errorField.text = "Failed to authenticate with server";
        }
    }
    // Update loop reads one message at a time
    private void Update()
    {
        // check that we have at least 2 bytes availabe to read
        if (m_Socket.Available > 2)
        {
            // get one message
            var data = GetMessage();
            // turn it into a JSON object for easier parsing
            var response = JObject.Parse(data);
            // extract the command value
            var commandValue = (string)response.Property("command")?.Value;
            // Command is a value change, read the data
            if (commandValue == "valueChange")
            {
                var key = (string)response.Property("key")?.Value;
                var value = (float)response.Property("value")?.Value;
                switch (key)
                {
                    case "zone_state":
                        {
                            m_values.ZoneValue = value;
                            // Do something with the zone value
                            break;
                        }
                    case "avg_motion":
                        {
                            m_values.MotionValue = value;
                            // Do something with the motion value
                            break;
                        }
                    case "enjoyment":
                        {
                            m_values.EnjoymentValue = value;
                            // Do something with the enjoyment value
                            break;
                        }
                    case "focus":
                        {
                            m_values.FocusValue = value;
                            // Do something with the focus value
                            break;
                        }
                    case "heart_rate":
                        {
                            m_values.HeartRateValue = value;
                            // Do something with the heart rate value
                            break;
                        }
                    default:
                        {
                            break;
                        }
                    }
                }
                else if (commandValue == "sessionComplete" 
                    || commandValue == "socketClosing")
                {
                    Disconnect();
                }
            }
    }


## Kotlin Example (Android Client)