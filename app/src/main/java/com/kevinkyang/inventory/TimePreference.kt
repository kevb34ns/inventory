package com.kevinkyang.inventory

import android.content.Context
import android.content.DialogInterface
import android.content.res.TypedArray
import android.preference.DialogPreference
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import android.widget.TimePicker


class TimePreference(context: Context, attrs: AttributeSet) :
        DialogPreference(context, attrs) {

    var mTimeDisplay: TextView? = null
    var mTimePicker: TimePicker? = null

    init {
        widgetLayoutResource =
                R.layout.notification_time_preference_widget_view
        dialogLayoutResource = R.layout.time_preference_dialog
        dialogIcon = null

        setDefaultValue(ExpirationManager.DEFAULT_NOTIFICATION_TIME)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (!restorePersistedValue) {
            persistString(ExpirationManager.DEFAULT_NOTIFICATION_TIME)
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
                getString(SettingsFragment.PREFKEY_NOTIFICATION_TIME, "")

        mTimeDisplay = view?.
                findViewById(R.id.notification_time_display) as TextView
        mTimeDisplay?.text = timeString
    }

    override fun onBindDialogView(view: View?) {
        mTimePicker = view?.findViewById(R.id.notification_time_picker)
                as TimePicker?
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)

        var hour = mTimePicker?.hour
        var minute = mTimePicker?.minute

        if (hour == null || minute == null) {
            return;
        }

        var amPm = "PM"
        if (hour > 12) {
            hour -= 12;
        } else if (hour == 0) {
            hour = 12;
        } else {
            amPm = "AM"
        }

        var timeString = "" + hour + ":" + minute + " " + amPm

        if (timeString != getPersistedString("")) {
            persistString(timeString)
            mTimeDisplay?.text = timeString
        }
    }
}