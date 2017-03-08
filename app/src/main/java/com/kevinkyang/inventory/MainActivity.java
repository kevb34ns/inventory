package com.kevinkyang.inventory;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements AddItemDialogListener {
	private ItemData itemData = null;
	private DBManager dbManager = null;
	private SuggestionManager suggestionManager;
	private FloatingActionButton addItemButton;

	private DrawerLayout drawerLayout;
	private ExpandableListView drawerList;
	private DrawerAdapter drawerAdapter;

	private Toolbar toolbar;

	private InventoryFragment inventoryFragment;
	private GroceryFragment groceryFragment;
	private boolean inGroceryMode;

	private TypedArray colorArray;

//	TODO app currently does not handle this activity being destroyed and recreated, eg fragment persists so you need to check for that
//	TODO manager classes need to conform to android definition of managers (itp341 fragments lecture slide 47)
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		dbManager = DBManager.getInstance();
		dbManager.init(this);
		itemData = ItemData.getInstance();

		suggestionManager = new SuggestionManager(this);
		suggestionManager.executeThread();

		addItemButton = (FloatingActionButton) findViewById(R.id.add_item_button);
		addItemButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary, null)));

		// Set up toolbar
		toolbar = (Toolbar) findViewById(R.id.custom_action_bar);
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

		inGroceryMode = false;

		colorArray = null;

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
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerLayout.setStatusBarBackground(R.color.colorPrimary);

		HashMap<String, ArrayList<String>> childrenMap = getInventoryMap();
		ArrayList<String> titles = new ArrayList<String>();
		titles.add("Inventory");
		titles.add("Expiring");
		titles.add("Grocery List");

		drawerList = (ExpandableListView) findViewById(R.id.navigation_drawer_list);
		drawerAdapter = new DrawerAdapter(this, titles, childrenMap, drawerList);
		drawerList.setAdapter(drawerAdapter);

		drawerList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
				String title = (String) drawerAdapter
						.getGroup(groupPosition);
				if (title.equals("Inventory")) {
					inventoryFragment.showInventory(null);
					changeFragments("0");
				} else if (title.equals("Expiring")) {
					inventoryFragment.showInventory(title);
					changeFragments("0");
				} else if (title.equals("Grocery List")) {
					changeFragments("1");
				}
				changeActionBarTitle(title);

				setUIColor(getResources()
						.getColor(R.color.colorPrimary, null));

				return true;
			}
		});

		drawerList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
				String title = (String) drawerAdapter
						.getGroup(groupPosition);
				if (title.equals("Inventory")) {
					if (childPosition != drawerAdapter.getChildrenCount(groupPosition) - 1) {
						String inventory = (String) drawerAdapter
								.getChild(groupPosition, childPosition);
						inventoryFragment.showInventory(inventory);
						changeFragments(Integer.toString(groupPosition));
						changeActionBarTitle(inventory);

						setUIColor(getInventoryColor(childPosition));

					} else {
						newInventoryDialog();
					}
				}
				return true;
			}
		});
	}

	private void changeActionBarTitle(String title) {
		toolbar.setTitle(title);
	}

	private void newInventoryDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

		TextView title = new TextView(this);
		title.setText("Add New Inventory");
		title.setGravity(Gravity.START);
		title.setPadding(30, 30, 30, 30);
		title.setTextSize(20);
		title.setTextColor(getColor(android.R.color.primary_text_light));
//		title.setTypeface(null, Typeface.BOLD);
		alertDialog.setCustomTitle(title);

		final EditText input = new EditText(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		input.setLayoutParams(lp);
		input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		alertDialog.setView(input);

		alertDialog.setPositiveButton("Add",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String newInventory = input.getText().toString().trim();
						if (newInventory.isEmpty()) {
							return;
						}

						if (newInventory.equals("Expiring")) {
							// TODO 'Expiring' is a reserved inventory; do not hardcode, make array of reserved inventories and check against it
							String msg = "This inventory is reserved by the app.";
							Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
						} else if (dbManager.getInventories().contains(newInventory)) {
							String msg = "This inventory already exists.";
							Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
						} else {
							dbManager.addInventory(newInventory);
							HashMap<String, ArrayList<String>> childrenMap = getInventoryMap();
							ArrayList<String> titles = new ArrayList<String>();
							titles.add("Inventory");
							titles.add("Expiring");
							titles.add("Grocery List");
							drawerAdapter = new DrawerAdapter(MainActivity.this, titles, childrenMap, drawerList);
							drawerList.setAdapter(drawerAdapter);
						}
					}
				});

		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		AlertDialog dialog = alertDialog.create();
		dialog.show();
		dialog.getWindow().setLayout(800, 500);
	}

	private HashMap<String, ArrayList<String>> getInventoryMap() {
		ArrayList<String> inventoryList = dbManager.getInventories();
		inventoryList.add("New inventory...");

		HashMap<String, ArrayList<String>> invMap =
				new HashMap<String, ArrayList<String>>();
		invMap.put("Inventory", inventoryList);
		invMap.put("Expiring", new ArrayList<String>());
		invMap.put("Grocery List", new ArrayList<String>());

		return invMap;
	}

	private void changeFragments(String tag) {
		FragmentManager fragmentManager = getSupportFragmentManager();

		Fragment fragment;
		fragment = fragmentManager.findFragmentByTag(tag);

		String otherTag = (tag.equals("0")) ? "1" : "0"; // TODO unsustainable solution if more than 2 fragments
		inGroceryMode = tag.equals("1"); //TODO must change once structure of drawer changes
		Fragment otherFragment;
		otherFragment = fragmentManager.findFragmentByTag(otherTag);

		fragmentManager.beginTransaction()
				.hide(otherFragment)
				.commit();

		fragmentManager.beginTransaction()
				.show(fragment)
				.commit();

		refreshCurrentList();

		drawerLayout.closeDrawers();
	}

	private void addListeners() {
		addItemButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AddItemDialog dialog = new AddItemDialog();
				if (!inGroceryMode) {
					Bundle args = new Bundle();
					args.putString("Inventory",
							inventoryFragment.getCurrentInventory());
					dialog.setArguments(args);
				}
				dialog.show(getSupportFragmentManager(), "dialog");
			}
		});

	}

	@Override
	public void onAddItemClicked(String name, int quantity, int expCode,
								 String unit, String type, String inventory,
								 boolean inGroceryList) {
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
				TimeManager.addDaysToDate(
						TimeManager.getDateTimeLocal(),
						daysToAdd), quantity, unit, type,
						inventory, inGroceryList));

		refreshCurrentList();
	}

	@Override
	public void onSaveItemClicked(String name, int quantity, int expCode, String unit, String type, String inventory, boolean inGroceryList, Item item) {
		item.setName(name);
		item.setQuantity(quantity);
		// TODO expiration, created date doesn't need update
		item.setUnit(unit);
		item.setType(type);
		item.setInventory(inventory);
		item.setInGroceryList(inGroceryList); // TODO is this needed

		dbManager.updateItem(item);
		refreshCurrentList();
	}

	@Override
	public boolean isInGroceryMode() {
		return inGroceryMode;
	}

	public void showEditDialog(Item item) {
		Bundle args = new Bundle();
		args.putBoolean("InEditMode", true);
		args.putParcelable("ItemParcel", item);

		AddItemDialog dialog = new AddItemDialog();
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), "dialog");
	}

	private void refreshCurrentList() {
		if (isInGroceryMode()) {
			groceryFragment.refresh();
		} else {
			inventoryFragment.refresh();
		}
	}

	private void setUIColor(int color) {
		toolbar.setBackgroundColor(color);
		addItemButton.setBackgroundTintList(
				ColorStateList.valueOf(
						color));
		drawerLayout.setStatusBarBackgroundColor(color);
	}

	private int getInventoryColor(int position) {
		if (colorArray == null) {
			colorArray = getResources().obtainTypedArray(R.array.array_inventory_colors);
		}

		return colorArray.getColor(position, 0);
	}

	// TODO should be part of a static/manager class
	public static int getAttributeColor(Context context, int attrId) {
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(attrId, typedValue, true);
		int colorRes = typedValue.resourceId;
		int color = -1;
		try {
			color = context.getResources().getColor(colorRes, context.getTheme());
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
		}
		return color;
	}

	public static class AddItemDialog extends DialogFragment {
		private EditText nameEditText;
		private EditText quantityEditText;
		private EditText unitEditText;
		private EditText typeEditText;
		private Spinner expirationSpinner;
		private ImageButton expirationButton;
		private Spinner inventorySpinner;
		private Button addButton;
		private Button cancelButton;

		private String currentInventory;
		private boolean inEditMode;
		private Item itemToEdit;

		private int defaultColor;

		public AddItemDialog() {

		}

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle args = getArguments();
			if (args != null) {
				inEditMode = args.getBoolean("InEditMode");
				itemToEdit = args.getParcelable("ItemParcel");
				currentInventory = args.getString("Inventory");
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			AddItemDialogListener parent = (AddItemDialogListener) getActivity();
			if (inEditMode) {
				getDialog().setTitle("Edit Item");
			} else if (parent.isInGroceryMode()) {
				getDialog().setTitle("New Grocery Item");
			} else {
				getDialog().setTitle("New Item");
			}

			View view = inflater.inflate(R.layout.add_item_dialog, container, false);
			nameEditText = (EditText) view.findViewById(R.id.input_name);
			quantityEditText = (EditText) view.findViewById(R.id.input_quantity);
			unitEditText = (EditText) view.findViewById(R.id.input_unit);
			typeEditText = (EditText) view.findViewById(R.id.input_type);
			expirationSpinner = (Spinner) view.findViewById(R.id.spinner_expiration);
			expirationButton = (ImageButton) view.findViewById(R.id.button_expiration);

			inventorySpinner = (Spinner) view.findViewById(R.id.spinner_inventory);
			ArrayList<String> inventories = DBManager.getInstance().getInventories();
			inventories.add(0, "Inventory");
			inventorySpinner.setAdapter(new ArrayAdapter<String>(getContext(),
					android.R.layout.simple_spinner_dropdown_item,
					inventories));

			addButton = (Button) view.findViewById(R.id.add_button);
			cancelButton = (Button) view.findViewById(R.id.cancel_button);

			addListeners();
			populateFields();
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
						String unitString = unitEditText.getText().toString();
						String typeString = typeEditText.getText().toString();
						int expCode = expirationSpinner.getSelectedItemPosition();
						String invString = "";
						if (inventorySpinner.getSelectedItemPosition() != 0) {
							invString = (String) inventorySpinner.getSelectedItem();
						}

						AddItemDialogListener activity = (AddItemDialogListener) getActivity();
						if (inEditMode) {
							activity.onSaveItemClicked(name, quantity, expCode,
									unitString, typeString, invString,
									activity.isInGroceryMode(),
									itemToEdit);
						} else {
							activity.onAddItemClicked(name, quantity, expCode,
									unitString, typeString, invString,
									activity.isInGroceryMode());
						}

						AddItemDialog.this.dismiss();
					}

				}
			});

			cancelButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					AddItemDialog.this.dismiss();
				}
			});

			expirationButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View v = inflater.inflate(R.layout.expiration_picker_dialog, null, false);
					DatePicker datePicker = (DatePicker) v.findViewById(R.id.date_picker);
					datePicker.setMinDate(System.currentTimeMillis());
					PopupWindow popupWindow = new PopupWindow(v, 700, 400, true);
					popupWindow.setAnimationStyle(-1);
					popupWindow.setElevation(16.0f);
					popupWindow.showAtLocation(typeEditText,
							Gravity.START,
							(int) expirationButton.getX(),
							(int) expirationButton.getY());
				}
			});
		}

		private void populateFields() {
			if (inEditMode) {
				addButton.setText("Save");
				nameEditText.setText(itemToEdit.getName());
				quantityEditText.setText(Integer.toString(itemToEdit.getQuantity()));
				unitEditText.setText(itemToEdit.getUnit());
				typeEditText.setText(itemToEdit.getType());
				//TODO need expiration to be exact dates
				setInventorySpinnerPosition(itemToEdit.getInventory());
			} else if (currentInventory != null) {
				setInventorySpinnerPosition(currentInventory);
			}
		}

		private void setInventorySpinnerPosition(String inventory) {
			for (int i = 1;
				 i < inventorySpinner.getCount();
				 i++) {
				if (inventorySpinner
						.getItemAtPosition(i)
						.equals(inventory)) {
					inventorySpinner.setSelection(i);
					break;
				}
			}
		}
	}

}
