package io.neuos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/***
 * Data class the provide QA properties when launching QA from third party client
 * Include this as extras in your intent
 */
@Parcelize
data class NeuosQAProperties(
        val quality:Quality,
        val maxTimeout:Float = INFINITE_TIMEOUT

) : Parcelable {
    enum class Quality { Perfect, Good, Normal }
    companion object {
        const val TASK_PROPERTIES: String = "properties"
        const val STAND_ALONE:String = "standalone"
        const val INFINITE_TIMEOUT:Float = 0f
    }
}
