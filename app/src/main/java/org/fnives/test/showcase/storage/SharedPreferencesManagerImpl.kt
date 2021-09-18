package org.fnives.test.showcase.storage

import android.content.Context
import android.content.SharedPreferences
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.model.session.Session
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferencesManagerImpl constructor(private val sharedPreferences: SharedPreferences) : UserDataLocalStorage {

    override var session: Session? by SessionDelegate(SESSION_KEY)

    private class SessionDelegate(private val key: String) : ReadWriteProperty<SharedPreferencesManagerImpl, Session?> {

        override fun setValue(thisRef: SharedPreferencesManagerImpl, property: KProperty<*>, value: Session?) {
            if (value == null) {
                thisRef.sharedPreferences.edit().remove(key).apply()
            } else {
                val values = setOf(
                    ACCESS_TOKEN_KEY + value.accessToken,
                    REFRESH_TOKEN_KEY + value.refreshToken
                )
                thisRef.sharedPreferences.edit().putStringSet(key, values).apply()
            }
        }

        override fun getValue(thisRef: SharedPreferencesManagerImpl, property: KProperty<*>): Session? {
            val values = thisRef.sharedPreferences.getStringSet(key, null)?.toList()
            val accessToken = values?.firstOrNull { it.startsWith(ACCESS_TOKEN_KEY) }
                ?.drop(ACCESS_TOKEN_KEY.length) ?: return null
            val refreshToken = values.firstOrNull { it.startsWith(REFRESH_TOKEN_KEY) }
                ?.drop(REFRESH_TOKEN_KEY.length) ?: return null

            return Session(accessToken = accessToken, refreshToken = refreshToken)
        }

        companion object {
            private const val ACCESS_TOKEN_KEY = "ACCESS_TOKEN_KEY"
            private const val REFRESH_TOKEN_KEY = "REFRESH_TOKEN_KEY"
        }
    }

    companion object {

        private const val SESSION_KEY = "SESSION_KEY"
        private const val SESSION_SHARED_PREFERENCES_NAME = "SESSION_SHARED_PREFERENCES_NAME"

        fun create(context: Context): SharedPreferencesManagerImpl {
            val sharedPreferences = context.getSharedPreferences(
                SESSION_SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )

            return SharedPreferencesManagerImpl(sharedPreferences)
        }
    }
}
