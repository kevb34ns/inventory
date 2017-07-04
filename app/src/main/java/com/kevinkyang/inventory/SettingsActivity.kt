package com.kevinkyang.inventory

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class SettingsActivity : AppCompatActivity(),OnSharedPreferenceChangeListener {

    companion object {
        const val PREFKEY_NOTIFICATIONS_ENABLED = "notificationsEnabled"
        const val PREFKEY_EXPIRATION_INTERVAL = "expirationInterval"
        const val PREFKEY_NOTIFICATION_TIME = "notificationTime"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREFKEY_NOTIFICATIONS_ENABLED) {
            val expirationManager = ExpirationManager(this)

            val enabled = sharedPreferences?.
                    getBoolean(PREFKEY_NOTIFICATIONS_ENABLED, true)
            if (enabled != null && enabled) {
                expirationManager.scheduleNotifications()
            } else if (enabled != null) {
                expirationManager.cancelNotifications()
            }
        }
    }
}
