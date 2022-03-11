package io.neuos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/***
 * Data class the provide QA properties when launching QA from third party client
 * Include this as extras in your intent
 * You can set a maximum timeout to receive allow the user to get the requested quality
 * If timeout expires, RESULT_CANCELED will be returned from the activity.
 * Otherwise RESULT_OK will return
 */
@Parcelize
data class NeuosQAProperties(
        val quality:Quality,
        val maxTimeout:Float = INFINITE_TIMEOUT

) : Parcelable {
    /**
     * Defines the quality of signal you wish to verify before QA screen returns.
     * */
    enum class Quality { Perfect, Good, Normal }
    companion object {
        const val TASK_PROPERTIES: String = "properties"
        const val STAND_ALONE:String = "standalone"
        const val INFINITE_TIMEOUT:Float = 0f
    }
}
