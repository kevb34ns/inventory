package com.kevinkyang.inventory

import android.content.Context
import android.content.res.TypedArray
import android.preference.DialogPreference
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.View
import android.widget.TextView


class TimePreference(context: Context, attrs: AttributeSet) :
        DialogPreference(context, attrs) {

    var mTimeDisplay: TextView? = null
    var mCurrentValue: String? = null

    init {
        widgetLayoutResource =
                R.layout.notification_time_preference_widget_view
        dialogLayoutResource = R.layout.time_preference_dialog
        dialogIcon = null

        setDefaultValue(ExpirationManager.DEFAULT_NOTIFICATION_TIME)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            mCurrentValue = getPersistedString("")
        } else {
            mCurrentValue = ExpirationManager.DEFAULT_NOTIFICATION_TIME
            persistString(mCurrentValue)
        }
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a?.getString(index) as Any
    }

    override fun onBindView(view: View?) {
        super.onBindView(view)

        val preferences =
                PreferenceManager.getDefaultSharedPreferences(context)
        val timeString = preferences.
                getString(SettingsActivity.PREFKEY_NOTIFICATION_TIME, "")

        mTimeDisplay = view?.
                findViewById(R.id.notification_time_display) as TextView
        mTimeDisplay?.text = timeString
    }
}