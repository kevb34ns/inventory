package com.kevinkyang.inventory

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.PopupWindow
import kotlinx.android.synthetic.main.add_item_dialog.view.*

import kotlinx.android.synthetic.main.expiration_picker_dialog.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.YEAR

class ExpirationPickerPopup(val context: Context,
                            val dateToSet: String?)
    : PopupWindow(context) {

    private val presetArray = context.resources
            .getStringArray(R.array.array_expires_dates)

    var clearButtonClickListener: ClearButtonClickListener? = null
    var saveButtonClickListener: SaveButtonClickListener? = null

    init {
        val inflater = context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        contentView = inflater.inflate(R.layout.expiration_picker_dialog,
                null, false)

        createGUI()
        setListeners()
    }

    @SuppressLint("WrongConstant") // workaround for IDE/compiler bug
    private fun createGUI() {

        val dim = getPopupResolution()
        width = dim[0]
        height = dim[1]

        isFocusable = true
        animationStyle = R.style.PopupAnimation
        elevation = 8.0f
        setBackgroundDrawable(null)

        contentView.preset_exp_1.text = presetArray[0]
        contentView.preset_exp_2.text = presetArray[1]
        contentView.preset_exp_3.text = presetArray[2]
        contentView.preset_exp_4.text = presetArray[3]
        contentView.preset_exp_5.text = presetArray[4]
        contentView.preset_exp_6.text = presetArray[5]

        val sdFormat = SimpleDateFormat(TimeManager.DEFAULT_DATE_FORMAT)
        val datePicker = contentView.date_picker
        datePicker.minDate = System.currentTimeMillis()
        if (dateToSet != null) {
            val cal = Calendar.getInstance()
            try {
                cal.time = sdFormat.parse(dateToSet)
                datePicker.updateDate(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH))
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
    }

    private fun setListeners() {
        val cancelButton = contentView.clear_button
        cancelButton.setOnClickListener {
            clearButtonClickListener?.onClick()

            dismiss()
        }

        val doneButton = contentView.done_button
        doneButton.setOnClickListener {
            val year = contentView.date_picker.year
            val month = contentView.date_picker.month
            val day = contentView.date_picker.dayOfMonth
            saveButtonClickListener?.onClick(year, month, day)

            dismiss()
        }
    }

    /**
     * Scales the popup dimensions based on the device's
     * screen resolution.
     * @return an integer array of size 2, with the
     * width in pixels at indice 0 and height at indice 1
     */
    private fun getPopupResolution() : IntArray {
        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                220f, context.resources.displayMetrics).toInt()
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                250f, context.resources.displayMetrics).toInt()

        return intArrayOf(width, height)
    }

    interface ClearButtonClickListener {
        fun onClick()
    }

    interface SaveButtonClickListener {
        fun onClick(year: Int, month: Int, day: Int)
    }
}