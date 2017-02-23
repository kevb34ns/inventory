package com.kevinkyang.inventory;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements AddItemDialogListener {
	private ItemData itemData = null;
	private SuggestionManager suggestionManager;
	private FloatingActionButton addItemButton;

	private String[] drawerTitles;
	private DrawerLayout drawerLayout;
	private ListView drawerList;

	private InventoryFragment inventoryFragment;
	private GroceryFragment groceryFragment;

//	TODO app currently does not handle this activity being destroyed and recreated, eg fragment persists so you need to check for that
//	TODO manager classes need to conform to android definition of managers (itp341 fragments lecture slide 47)
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		// TODO need better solution
		DBManager dbManager = DBManager.getInstance();
		dbManager.init(this);
		itemData = ItemData.getInstance();

		suggestionManager = new SuggestionManager(this);
		suggestionManager.executeThread();

		addItemButton = (FloatingActionButton) findViewById(R.id.add_item_button);

		// Set up toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.custom_action_bar);
		toolbar.setTitleTextColor(0xFFFFFFFF);
		toolbar.setNavigationIcon(R.drawable.ic_menu);
		setSupportActionBar(toolbar);

		FragmentManager fragmentManager = getSupportFragmentManager();
		inventoryFragment = new InventoryFragment();
		fragmentManager.beginTransaction()
				.add(R.id.fragment_container,
						inventoryFragment,
						"0")
				.commit();

		groceryFragment = new GroceryFragment();
		fragmentManager.beginTransaction()
				.add(R.id.fragment_container,
						groceryFragment,
						"1")
				.hide(groceryFragment)
				.commit();

		initializeDrawer();
		addListeners();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ExpirationManager manager = null; //TODO fix this
//		SuggestionManager suggestionManager = null; TODO
		switch (item.getItemId()) {
			// TODO some options for testing only; get rid of it
			case R.id.options_item_groceries:
				// TODO go to grocery list
				return true;
//			case R.id.options_item_suggestions:
//				SuggestionAdapter suggestionAdapter = new SuggestionAdapter(this, suggestionManager.getSuggestionData());
//				itemListView.setAdapter(suggestionAdapter);
//				return true; //TODO to test this, must show in a fragment now
			case R.id.options_item_clear:
				suggestionManager.clearData();
				return true;
			case R.id.options_item_notify:
				manager = new ExpirationManager(this);
				manager.sendNotifications();
				return true;
			case R.id.options_item_schedule:
				manager = new ExpirationManager(this);
				manager.scheduleNotifications();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void initializeDrawer() {
		drawerTitles = getResources().getStringArray(R.array.array_nav_drawer);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerLayout.setStatusBarBackground(R.color.colorPrimary);

		drawerList = (ListView) findViewById(R.id.navigation_drawer_list);
		drawerList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, drawerTitles));
		drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				changeFragments(Integer.toString(position));

			}
		});
	}

	private void changeFragments(String tag) {
		FragmentManager fragmentManager = getSupportFragmentManager();

		Fragment fragment;
		fragment = fragmentManager.findFragmentByTag(tag);

		String otherTag = (tag.equals("0")) ? "1" : "0"; // TODO unsustainable solution if more than 2 fragments
		Fragment otherFragment;
		otherFragment = fragmentManager.findFragmentByTag(otherTag);

		fragmentManager.beginTransaction()
				.hide(otherFragment)
				.commit();

		fragmentManager.beginTransaction()
				.show(fragment)
				.commit();

		drawerLayout.closeDrawers();
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

		itemData.addItem(new Item(-1, name,
				TimeManager.getDateTimeLocal(),
				TimeManager.addDaysToDate(TimeManager.getDateTimeLocal(), daysToAdd),
				quantity, "", "", ""));//TODO have to add unit, type, and inventory here
		inventoryFragment.refresh();
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
