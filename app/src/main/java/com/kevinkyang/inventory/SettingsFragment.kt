package com.kevinkyang.inventory

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment


class SettingsFragment : PreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val PREFKEY_NOTIFICATIONS_ENABLED = "notificationsEnabled"
        const val PREFKEY_EXPIRATION_INTERVAL = "expirationInterval"
        const val PREFKEY_NOTIFICATION_TIME = "notificationTime"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREFKEY_NOTIFICATIONS_ENABLED) {
            val expirationManager = ExpirationManager(context)

            val enabled = sharedPreferences?.
                    getBoolean(PREFKEY_NOTIFICATIONS_ENABLED, true)
            if (enabled != null && enabled) {
                expirationManager.scheduleNotifications()
            } else if (enabled != null) {
                expirationManager.cancelNotifications()
            }
        } else if (key == PREFKEY_NOTIFICATION_TIME) {
            val expirationManager = ExpirationManager(context)
            expirationManager.cancelNotifications()
            expirationManager.scheduleNotifications()
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }
}