package com.arctop
object ArctopStreamService {
    /**
     * General Constants
     * */
    // Name of broadcasting service on the local network
    const val SERVICE_NAME = "ArctopService"
    // Service type / protocol for network discovery
    const val SERVICE_TYPE = "_http._tcp"

    /**
     * List of constants that can be sent / received as keys inside JSON objects over the
     * socket connection
     * */
    object StreamObjectKeys {
        // defines the command that is encapsulated in this JSON
        const val COMMAND = "command"
        // defines the times stamp in unix ms that the data has arrived from the prediction service
        // value is a long int
        const val TIME_STAMP = "timestamp"
        // key for previous connection state ( when COMMAND == CONNECTION)
        const val PREVIOUS = "previous"
        // key for current connection state ( when COMMAND == CONNECTION)
        const val CURRENT = "current"
        // key that holds the key name of the stream value ( when COMMAND == VALUE_CHANGED )
        const val KEY = "key"
        // key that holds the new value ( when COMMAND == VALUE_CHANGED )
        const val VALUE = "value"
        // key that holds the challenge request / response ( when COMMAND == CHALLENGE )
        const val CHALLENGE_DATA = "challenge_data"
        // key to use when sending apiKey to server ( when COMMAND == AUTH )
        const val API_KEY = "apiKey"
        // key that holds the passed value of QA data ( when COMMAND == QA )
        const val PASSED = "passed"
        // key that holds the type of QA failure if PASSED == false ( when COMMAND == QA )
        const val TYPE = "type"
        // key that holds the error code sent by an error command ( when COMMAND == ERROR )
        const val ERROR_CODE = "errorCode"
        // key that holds the error message sent by an error command ( when COMMAND == ERROR )
        const val MESSAGE = "message"
    }

    /**
     * Possible values that a COMMAND key can hold in a transported object
     * */
    object StreamCommandValues {
        // object describes a connection change
        const val CONNECTION = "connection"
        // object describes a value change
        const val VALUE_CHANGED = "valueChange"
        // object describes QA status
        const val QA = "qa"
        // object notifies that the session has completed
        const val SESSION_COMPLETE = "sessionComplete"
        // object describes an error
        const val ERROR = "error"
        // object for requesting authentication with server
        const val AUTH = "auth"
        // object contains auth challenge / response
        const val CHALLENGE = "challenge"
        // object notifies successful authentication
        const val AUTH_SUCCESS = "auth-success"
        // object notifies failed authentication
        const val AUTH_FAILED = "auth-failed"
    }
}

