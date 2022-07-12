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
            callback?.onTransitionToIdle()
            currentCallback?.run()
        }
    }

    override fun getName(): String = name

    override fun isIdleNow(): Boolean = dispatcher.runningCallsCount() == 0

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
    }
}
