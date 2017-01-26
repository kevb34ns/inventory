package com.kevinkyang.inventory;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements AddItemDialogListener {
	private ItemData itemData = null;
	private ListView itemListView;
	private ItemAdapter itemAdapter;
	private FloatingActionButton addItemButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		// TODO need better solution
		DBManager dbManager = DBManager.getInstance();
		dbManager.init(this);
		itemData = ItemData.getInstance();

		itemListView = (ListView) findViewById(R.id.item_list);
		itemAdapter = new ItemAdapter(this, itemData.getItems());
		itemListView.setAdapter(itemAdapter);

		addItemButton = (FloatingActionButton) findViewById(R.id.add_item_button);
		addListeners();
	}

	private void addListeners() {
		addItemButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AddItemDialog dialog = new AddItemDialog();
				dialog.show(getSupportFragmentManager(), "dialog");
			}
		});
	}

	@Override
	public void onAddItemClicked(String name, int quantity, int expCode) {
		/* Selected Item Positions TODO find better solution for visibility
		   None = 0
		   1 day = 1
		   3 days = 2
		   1 week = 3
		   2 weeks = 4
		   1 month = 5
		   3 months = 6
		 */
		int daysToAdd = 0;
		switch (expCode) {
			case 1: daysToAdd = 1; break;
			case 2: daysToAdd = 3; break;
			case 3: daysToAdd = 7; break;
			case 4: daysToAdd = 14; break;
			case 5: daysToAdd = 30; break;
			case 6: daysToAdd = 90; break;
			default: break;
		}

		itemData.addItem(new Item(name,
				TimeManager.getDateTimeLocal(),
				TimeManager.addDaysToDate(TimeManager.getDateTimeLocal(), daysToAdd),
				quantity));
		itemAdapter.notifyDataSetChanged();
	}

	public static class AddItemDialog extends DialogFragment {
		private EditText nameEditText;
		private EditText quantityEditText;
		private Spinner expirationSpinner;
		private Button addButton;

		public AddItemDialog() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			getDialog().setTitle("Add Item");

			View view = inflater.inflate(R.layout.add_item_dialog, container, false);
			nameEditText = (EditText) view.findViewById(R.id.input_name);
			quantityEditText = (EditText) view.findViewById(R.id.input_quantity);
			expirationSpinner = (Spinner) view.findViewById(R.id.spinner_expiration);
			addButton = (Button) view.findViewById(R.id.add_button);

			addListeners();
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			return view;
		}

		@Override
		public void onStart() {
			getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			super.onStart();
		}

		private void addListeners() {
			addButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					String name = nameEditText.getText().toString().trim();
					if (!name.isEmpty()) {
						String quantityString = quantityEditText.getText().toString();
						int quantity = 1;
						if (!quantityString.isEmpty()) {
							quantity = Integer.parseInt(quantityString);
						}

						int expCode = expirationSpinner.getSelectedItemPosition();

						AddItemDialogListener activity = (AddItemDialogListener) getActivity();
						activity.onAddItemClicked(name, quantity, expCode);
						AddItemDialog.this.dismiss();
					}

				}
			});
		}
	}

}
