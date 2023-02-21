package com.udacity.project4.utils

import androidx.test.espresso.idling.CountingIdlingResource

object DBIdlingResource {

    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
    inline fun <T> wrapEspressoIdlingResource(function: () -> T): T {
        // Espresso does not work well with coroutines yet. See
        // https://github.com/Kotlin/kotlinx.coroutines/issues/982
        DBIdlingResource.increment() // Set app as busy.
        return try {
            function()
        } finally {
            DBIdlingResource.decrement() // Set app as idle.
        }
    }
}