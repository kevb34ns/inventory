package com.kevinkyang.inventory;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

/**
 * Created by Kevin on 3/7/2017.
 */

public class ExpirationPickerDialog extends DialogFragment {
	private DatePicker datePicker;
	private Button okButton;
	private Button cancelButton;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.expiration_picker_dialog, container, false);

		datePicker = (DatePicker) view.findViewById(R.id.date_picker);
		datePicker.setMinDate(System.currentTimeMillis());

		return view;
	}
}
