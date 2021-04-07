package org.fnives.test.showcase.session

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import org.fnives.test.showcase.core.session.SessionExpirationListener
import org.fnives.test.showcase.ui.auth.AuthActivity

class SessionExpirationListenerImpl(private val context: Context) : SessionExpirationListener {

    override fun onSessionExpired() {
        Handler(Looper.getMainLooper()).post {
            context.startActivity(
                AuthActivity.getStartIntent(context)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
