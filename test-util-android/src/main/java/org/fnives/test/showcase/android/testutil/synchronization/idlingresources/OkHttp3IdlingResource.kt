package org.fnives.test.showcase.android.testutil.synchronization.idlingresources

import androidx.annotation.CheckResult
import androidx.annotation.NonNull
import androidx.test.espresso.IdlingResource
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

/**
 * AndroidX version of Jake Wharton's OkHttp3IdlingResource.
 *
 * Reference: https://github.com/JakeWharton/okhttp-idling-resource/blob/master/src/main/java/com/jakewharton/espresso/OkHttp3IdlingResource.java
 */
class OkHttp3IdlingResource private constructor(
    private val name: String,
    private val dispatcher: Dispatcher
) : IdlingResource {
    @Volatile
    var callback: IdlingResource.ResourceCallback? = null

    init {
        val currentCallback = dispatcher.idleCallback
        dispatcher.idleCallback = Runnable {
            sleepForDispatcherDefaultCallInRetrofitErrorState()
            callback?.onTransitionToIdle()
            currentCallback?.run()
        }
    }

    override fun getName(): String = name

    override fun isIdleNow(): Boolean {
        val isIdle = dispatcher.runningCallsCount() == 0
        if (isIdle) {
            sleepForDispatcherDefaultCallInRetrofitErrorState()
        }
        return isIdle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }

    companion object {
        /**
         * Create a new [IdlingResource] from `client` as `name`. You must register
         * this instance using `Espresso.registerIdlingResources`.
         */
        @CheckResult
        @NonNull
        fun create(@NonNull name: String?, @NonNull client: OkHttpClient?): OkHttp3IdlingResource {
            if (name == null) throw NullPointerException("name == null")
            if (client == null) throw NullPointerException("client == null")
            return OkHttp3IdlingResource(name, client.dispatcher)
        }

        /**
         * This is required, because in case of Errors Retrofit uses Dispatcher.Default to suspendThrow
         * see: retrofit2.KotlinExtensions.kt Exception.suspendAndThrow
         * Relevant code issue: https://github.com/square/retrofit/blob/6cd6f7d8287f73909614cb7300fcde05f5719750/retrofit/src/main/java/retrofit2/KotlinExtensions.kt#L121
         * This is the current suggested approach to their problem with Unchecked Exceptions
         *
         * Sadly Dispatcher.Default cannot be replaced yet, so we can't swap it out in tests:
         * https://github.com/Kotlin/kotlinx.coroutines/issues/1365
         *
         * This brings us to this sleep for now.
         */
        private fun sleepForDispatcherDefaultCallInRetrofitErrorState() {
            Thread.sleep(200L)
        }
    }
}
