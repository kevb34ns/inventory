package com.kevinkyang.inventory

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class SettingsActivity : AppCompatActivity() {
    // TODO see how to build settings activity using Preference objects at https://developer.android.com/guide/topics/ui/settings.html
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }
}
