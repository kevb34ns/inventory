package com.kevinkyang.inventory;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import expandableRVAdapter.ExpandableRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity implements ItemChangeListener {
	public static final String TAG = "inventory";
	public static final int SETTINGS_REQUEST = 0x73;

	private ItemManager itemManager = null;
	private DBManager dbManager = null;
	private SuggestionManager suggestionManager;
	private FloatingActionButton floatingAddButton;

	private DrawerLayout drawerLayout;
	private RecyclerView drawerRV;
	private DrawerRVAdapter drawerRVAdapter;
	private ExpandableDrawerAdapter drawerAdapter;
	private LinearLayoutManager drawerLayoutManager;

	private ImageButton drawerSettingsButton;

	// add item widget views
	private LinearLayout addItemWidget;
	private EditText addItemEditText;
	private ImageButton editNewItemButton;
	private ImageButton addNewItemButton;

	private Toolbar toolbar;

	private InventoryFragment inventoryFragment;
	private GroceryFragment groceryFragment;
	private boolean inGroceryMode;

	private TypedArray colorArray;

//	TODO manager classes need to conform to android definition of managers (itp341 fragments lecture slide 47)
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		itemManager = ItemManager.getInstance();
		if (!itemManager.isInitialized()) {
			itemManager.init(this);
		}
		dbManager = DBManager.getInstance();

		suggestionManager = new SuggestionManager(this);
		suggestionManager.executeThread();

		floatingAddButton = (FloatingActionButton) findViewById(R.id.add_item_button);
		floatingAddButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent, null)));
		floatingAddButton.setOnClickListener(view -> toggleAddButton(false));

		// Set up toolbar
		// TODO when returning from an onDestroy() (eg orientation change), the color of the inventory is not preserved; fix the bug so that the right color is shown
		toolbar = (Toolbar) findViewById(R.id.custom_action_bar);
		setSupportActionBar(toolbar);
		changeActionBarTitle("Inventory");

		toolbar.setNavigationOnClickListener((v) -> {
			if (drawerLayout != null &&
					!drawerLayout.isDrawerOpen(Gravity.LEFT)) {

				drawerLayout.openDrawer(Gravity.LEFT);
			}
		});

		FragmentManager fragmentManager = getSupportFragmentManager();

		inventoryFragment = (InventoryFragment) fragmentManager
				.findFragmentByTag("0");
		if (inventoryFragment == null) {
			inventoryFragment = new InventoryFragment();
			fragmentManager.beginTransaction()
					.add(R.id.fragment_container,
							inventoryFragment,
							"0")
					.commit();
		}

		groceryFragment = (GroceryFragment) fragmentManager
				.findFragmentByTag("1");
		if (groceryFragment == null) {
			groceryFragment = new GroceryFragment();
			fragmentManager.beginTransaction()
					.add(R.id.fragment_container,
							groceryFragment,
							"1")
					.hide(groceryFragment)
					.commit();
		}

		Intent intent = getIntent();
		String intentInventory = intent.getStringExtra("inventory");
		if (intentInventory != null) {
			inventoryFragment.setInventory(intentInventory);
			changeActionBarTitle(intentInventory);
		}

		if (savedInstanceState != null) {
			inGroceryMode =
					savedInstanceState
							.getBoolean("inGroceryMode", false);
		} else {
			inGroceryMode = false;
		}

		colorArray = null;

		initializeDrawer();
		addItemWidgetSetup();
		checkNotificationStatus();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("inGroceryMode", inGroceryMode);
		super.onSaveInstanceState(outState);
}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// TODO on settings result, check resultCode that settings were changed, and get the changed settings from the intent, and apply them, if applicable (some settings wouldn't require any action)
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
		switch (item.getItemId()) {
			// TODO some options for testing only; get rid of it
			case R.id.options_item_suggestions:
				ArrayList<SuggestionItem> sItems = suggestionManager.getItemSuggestions();
				String msg = "Suggestions:\n";
				for (SuggestionItem sItem : sItems) {
					msg += sItem.getName() + "\n";
				}
				Log.d(TAG, msg);
				return true;
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
			case R.id.options_item_cancel:
				manager = new ExpirationManager(this);
				manager.cancelNotifications();
				return true;
			case R.id.options_item_check_enabled:
				PendingIntent pendingIntent =
						PendingIntent.getBroadcast(this, 0,
								new Intent(this, NotificationReceiver.class),
								PendingIntent.FLAG_NO_CREATE);
				Boolean enabled = pendingIntent != null;
				Toast.makeText(this, enabled.toString(), Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		if (addItemWidget != null && addItemWidget.getVisibility() == View.VISIBLE) {
			//TODO find way to make additemwidget invisible on keyboard hide
			addItemEditText.clearFocus();
		} else {
			super.onBackPressed();
		}
	}

	private void initializeDrawer() {
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerLayout.setStatusBarBackground(R.color.colorPrimary);

		ArrayList<DrawerGroupItem> groups = new ArrayList<>();
		groups.add(new DrawerGroupItem("Inventory"));
		groups.add(new DrawerGroupItem("Expiring"));
		groups.add(new DrawerGroupItem("Grocery List"));

		ArrayList<ArrayList<DrawerChildItem>> children = new ArrayList<>();
		children.add(getInventoryChildren());
		children.add(new ArrayList<>());
		children.add(new ArrayList<>());

		drawerRV = (RecyclerView) findViewById(R.id.drawer_rv_list);
		drawerAdapter = new ExpandableDrawerAdapter(this, groups, children);
		drawerRV.setAdapter(drawerAdapter);
		drawerLayoutManager = new LinearLayoutManager(this);
		drawerRV.setLayoutManager(drawerLayoutManager);

		drawerAdapter.setOnItemClickListener(new ExpandableRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public boolean onGroupClick(int groupPosition) {
				String title = drawerAdapter.getGroup(groupPosition).getName();
				switch (title) {
					case "Inventory":
						inventoryFragment.showInventory(null);
						changeFragments("0");
						break;
					case "Expiring":
						inventoryFragment.showInventory(title);
						changeFragments("0");
						break;
					case "Grocery List":
						changeFragments("1");
						break;
					default: break;
				}
				changeActionBarTitle(title);

				setUIColor(getResources()
						.getColor(R.color.colorPrimary, null));
				return true;
			}

			@Override
			public boolean onChildClick(int groupPosition, int childPosition) {
				String title = drawerAdapter.getGroup(groupPosition).getName();
				if (title.equals("Inventory")) {
					if (childPosition != drawerAdapter.getChildrenCount(groupPosition) - 1) {
						String inventory = (String) drawerAdapter
								.getChild(groupPosition, childPosition)
								.getName();
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

		drawerSettingsButton = (ImageButton)
				findViewById(R.id.drawer_settings_button);
		drawerSettingsButton.setOnClickListener((v -> {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, SETTINGS_REQUEST);
		}));

		// TODO fix R.string.app_name
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
		toggle.syncState();
	}

	public void changeActionBarTitle(String title) {
		// TODO the way the title is changed right now is not very integrated; whoever changes the inventory is responsible for changing the toolbar title. This needs to be overhauled so that any time the inventory is changed, the title is guaranteed to be changed to the correct thing
		if (title == null) {
			title = "Inventory";
		}

		SpannableString spannableString =
				new SpannableString(title);
		spannableString.setSpan(new TypefaceSpan("sans-serif-condensed"),
				0, spannableString.length(),
				Spanned.SPAN_INCLUSIVE_INCLUSIVE);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(spannableString);
		}
	}

	private void newInventoryDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

		TextView title = new TextView(this);
		title.setText("Add New Inventory");
		title.setGravity(Gravity.START);
		title.setPadding(30, 30, 30, 30);
		title.setTextSize(20);
		title.setTextColor(getColor(android.R.color.primary_text_light));
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
							// TODO 'Expiring' is a reserved inventory; do not hardcode, make array of reserved inventories and check against it; also, 'Expiring' shouldn't be reserved so this whole section may be unnecessary
							String msg = "This inventory is reserved by the app.";
							Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
						} else if (dbManager.getInventories().contains(newInventory)) {
							String msg = "This inventory already exists.";
							Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
						} else {
							dbManager.addInventory(newInventory);
							drawerRVAdapter.addInventory(newInventory);
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
		dialog.getWindow().setLayout(800, 500); //TODO need to dpi scale
	}

	private ArrayList<DrawerChildItem> getInventoryChildren() {
		ArrayList<DrawerChildItem> invList = new ArrayList<>();
		for (String name : dbManager.getInventories()) {
			invList.add(new DrawerChildItem(name));
		}
		invList.add(new DrawerChildItem("New Inventory..."));

		return invList;
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

	private void toggleAddButton(boolean isVisible) {
		boolean currentlyVisible = floatingAddButton.getVisibility() ==
				View.VISIBLE;
		if (currentlyVisible == isVisible) {
			return;
		}

		if (isVisible) {

			floatingAddButton.setVisibility(View.VISIBLE);
			Animation growAnim = AnimationUtils.loadAnimation(MainActivity.this,
					R.anim.button_grow);
			floatingAddButton.startAnimation(growAnim);
		} else {

			Animation shrinkAnim = AnimationUtils.loadAnimation(MainActivity.this,
					R.anim.button_shrink);
			shrinkAnim.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					floatingAddButton.setVisibility(View.GONE);
					toggleAddItemWidget(true);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			floatingAddButton.startAnimation(shrinkAnim);
		}
	}

	private void toggleAddItemWidget(boolean isVisible) {
		boolean currentlyVisible = addItemWidget.getVisibility() ==
				View.VISIBLE;
		if (currentlyVisible == isVisible) {
			return;
		}

		if (isVisible) {

			addItemWidget.setVisibility(View.VISIBLE);
			addItemEditText.requestFocus();
			InputMethodManager imMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imMgr.showSoftInput(addItemEditText, 0);

		} else {

			addItemWidget.setVisibility(View.GONE);
			InputMethodManager imMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imMgr.hideSoftInputFromWindow(addItemEditText.getWindowToken(),
					0);
		}
	}

	private void quickAddItem() {
		String name = addItemEditText.getText().toString();
		if (name.isEmpty()) {
			return;
		}

		boolean inGroceryList = isInGroceryMode();
		String inventory = "";
		if (!inGroceryList &&
				inventoryFragment.getCurrentInventory() != null) {

			inventory = inventoryFragment.getCurrentInventory();
		}

		onItemAdded(name, 1, "", "", "", inventory, inGroceryList);

		addItemEditText.setText("");
	}

	private void addItemWidgetSetup() {
		addItemWidget = (LinearLayout) findViewById(R.id.add_item_widget);
		addItemEditText = (EditText) findViewById(R.id.widget_edittext);

		editNewItemButton = (ImageButton)
				findViewById(R.id.widget_more_button);
		editNewItemButton.setOnClickListener((v -> {
			AddItemDialog dialog = new AddItemDialog();

			Bundle args = new Bundle();
			if (!inGroceryMode) {
				args.putString("Inventory",
						inventoryFragment.getCurrentInventory());
			}

			String name = addItemEditText.getText().toString();
			if (!name.isEmpty()) {
				args.putString("NameToSet", name);
			}

			dialog.setArguments(args);
			dialog.show(getSupportFragmentManager(), "dialog");

			addItemEditText.setText("");
			addItemWidget.setVisibility(View.GONE);
			toggleAddButton(true);
		}));

		addNewItemButton = (ImageButton)
				findViewById(R.id.widget_add_button);

		addNewItemButton.setOnClickListener((v -> {
			quickAddItem();
		}));

		addItemEditText.setFocusableInTouchMode(true);
		addItemEditText.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus) {
				toggleAddItemWidget(false);
				toggleAddButton(true);
			}
		});
		addItemEditText.setOnEditorActionListener(((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				quickAddItem();
			}

			return true;
		}));
	}

	private void checkNotificationStatus() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean enabled = preferences.getBoolean(SettingsFragment.PREFKEY_NOTIFICATIONS_ENABLED, true);

		PendingIntent pendingIntent =
				PendingIntent.getBroadcast(this, 0,
						new Intent(this, NotificationReceiver.class),
						PendingIntent.FLAG_NO_CREATE);
		ExpirationManager mgr = new ExpirationManager(this);

		if (pendingIntent == null && enabled) {
			mgr.scheduleNotifications();
		} else if (pendingIntent != null && !enabled) {
			mgr.cancelNotifications();
		}
	}

	@Override
	public void onItemAdded(String name, float quantity, String unit,
							String type, String expiresDate,
							String inventory,
							boolean inGroceryList) {
		/* Selected item Positions TODO find better solution for visibility
		   None = 0
		   1 day = 1
		   3 days = 2
		   1 week = 3
		   2 weeks = 4
		   1 month = 5
		   3 months = 6
		 */
//		int daysToAdd = 0; TODO
//		switch (expCode) {
//			case 1: daysToAdd = 1; break;
//			case 2: daysToAdd = 3; break;
//			case 3: daysToAdd = 7; break;
//			case 4: daysToAdd = 14; break;
//			case 5: daysToAdd = 30; break;
//			case 6: daysToAdd = 90; break;
//			default: break;
//		}
		Item item = new Item(-1, name, TimeManager.getDateTimeLocal(),
				expiresDate, quantity, unit, type, inventory,
				inGroceryList);
		itemManager.addItem(item);
		addToCurrentList(item);
	}

	@Override
	public void onItemSaved(String name, float quantity, String unit,
							String type, String expiresDate,
							String inventory,
							boolean inGroceryList, Item item,
							int position) {
		item.setName(name);
		item.setQuantity(quantity);
		item.setExpiresDate(expiresDate);
		item.setUnit(unit);
		item.setType(type);
		item.setInventory(inventory);
		item.setInGroceryList(inGroceryList); // TODO is this needed

		itemManager.updateItem(item);
		saveToCurrentList(position);
	}

	@Override
	public void onDialogDismissed() {
		floatingAddButton.setVisibility(View.VISIBLE);
		Animation growAnim = AnimationUtils.loadAnimation(MainActivity.this,
				R.anim.button_grow);
		floatingAddButton.startAnimation(growAnim);
	}

	@Override
	public boolean isInGroceryMode() {
		return inGroceryMode;
	}

	/**
	 *
	 * @param item
	 * @param position position of the item in the current
	 *                 adapter.
	 */
	public void showEditDialog(Item item, int position) {
		Bundle args = new Bundle();
		args.putBoolean("InEditMode", true);
		args.putParcelable("ItemParcel", item);
		args.putInt("Position", position);

		AddItemDialog dialog = new AddItemDialog();
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), "dialog");
	}

	public void changeToCurrentList() {
		if (isInGroceryMode() &&
				groceryFragment.isInitFinished()) {
			changeFragments("1");
		} else if (inventoryFragment.isInitFinished()) {
			changeFragments("0");
		}
	}

	private void refreshCurrentList() {
		if (isInGroceryMode()) {
			groceryFragment.refresh();
		} else {
			inventoryFragment.refresh();
		}
	}

	private void addToCurrentList(Item item) {
		if (isInGroceryMode()) {
			groceryFragment.itemAdded(item);
		} else {
			inventoryFragment.itemAdded(item);
		}
	}

	private void saveToCurrentList(int position) {
		if (isInGroceryMode()) {
			groceryFragment.itemSaved(position);
		} else {
			inventoryFragment.itemSaved(position);
		}
	}

	private void setUIColor(int color) {
		//TODO don't need to change toolbar color anymore, and must set FAB to accent, not primary
		toolbar.setBackgroundColor(color);
		floatingAddButton.setBackgroundTintList(
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

	public SuggestionManager getSuggestionManager() {
		return suggestionManager;
	}

	public void showSnackbar(final Item item,
							 View view,
							 final int position,
							 String msg,
							 final BiConsumer<Item, Integer>
									 clickMethod) {
		View.OnClickListener listener = v -> {
			clickMethod.accept(item, position);
		};
		Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
				.setAction("Undo", listener)
				.show();
	}

	//TODO mimics Java 8 functional interface. replace when Java 8 support releases, currently also requires minSDK = 24
	public static interface BiConsumer<T, U> {
		public void accept(T t, U u);
	}

	public static class AddItemDialog extends DialogFragment {
		private AutoCompleteTextView nameEditText;
		private EditText quantityEditText;
		private AutoCompleteTextView unitEditText;
		private AutoCompleteTextView typeEditText;
		private Button expirationButton;
		private Spinner inventorySpinner;
		private Button addButton;
		private Button cancelButton;

		private String currentInventory;
		private boolean inEditMode;
		private Item itemToEdit;
		private int position;
		private String nameToSet;

		private boolean dateSet;

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
				position = args.getInt("Position");
				nameToSet = args.getString("NameToSet");

			}

			dateSet = false;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ItemChangeListener parent = (ItemChangeListener) getActivity();
			if (inEditMode) {
				getDialog().setTitle("Edit Item");
			} else if (parent.isInGroceryMode()) {
				getDialog().setTitle("New Grocery Item");
			} else {
				getDialog().setTitle("New Item");
			}

			View view = inflater.inflate(R.layout.add_item_dialog, container, false);

			nameEditText = (AutoCompleteTextView) view.findViewById(R.id.input_name);
			if (nameToSet != null) {
				nameEditText.setText(nameToSet);
			}

			quantityEditText = (EditText) view.findViewById(R.id.input_quantity);
			unitEditText = (AutoCompleteTextView)
					view.findViewById(R.id.input_unit);
			typeEditText = (AutoCompleteTextView)
					view.findViewById(R.id.input_type);
			expirationButton = (Button) view.findViewById(R.id.button_expiration);

			inventorySpinner = (Spinner) view.findViewById(R.id.spinner_inventory);
			ArrayList<String> inventories = DBManager.getInstance().getInventories();
			inventories.add(0, "Inventory");
			inventorySpinner.setAdapter(new ArrayAdapter<String>(getContext(),
					android.R.layout.simple_spinner_dropdown_item,
					inventories));

			addButton = (Button) view.findViewById(R.id.add_button);
			cancelButton = (Button) view.findViewById(R.id.cancel_button);

			addListeners();
			setAutoCompleteViews();
			populateFields();
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			return view;
		}

		@Override
		public void onStart() {
			getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			super.onStart();
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			ItemChangeListener listener = (ItemChangeListener) getActivity();
			listener.onDialogDismissed();
			super.onDismiss(dialog);
		}

		private void addListeners() {
			addButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					String name = nameEditText.getText().toString().trim();
					if (!name.isEmpty()) {
						String quantityString = quantityEditText.getText().toString();
						float quantity = 1;
						if (!quantityString.isEmpty()) {
							quantity = Float.parseFloat(quantityString);
						}
						String unitString = unitEditText.getText().toString();
						String typeString = typeEditText.getText().toString();
						String expiresString = "";
						if (dateSet) {
							expiresString = expirationButton
									.getText()
									.toString();
						}
						String invString = "";
						if (inventorySpinner.getSelectedItemPosition() != 0) {
							invString = (String) inventorySpinner.getSelectedItem();
						}

						ItemChangeListener activity = (ItemChangeListener) getActivity();
						if (inEditMode) {
							activity.onItemSaved(name, quantity,
									unitString, typeString, expiresString,
									invString, activity.isInGroceryMode(),
									itemToEdit, position);
						} else {
							activity.onItemAdded(name, quantity,
									unitString, typeString, expiresString,
									invString, activity.isInGroceryMode());
						}

						AddItemDialog.this.dismiss();
					}

				}
			});

			cancelButton.setOnClickListener((view) -> {
				AddItemDialog.this.dismiss();
			});

			expirationButton.setOnClickListener((view) -> {
				if (dateSet) {
					String date = expirationButton.getText().toString();
					setUpPopupWindow(date);
				} else {
					setUpPopupWindow(null);
				}
			});
		}

		private void populateFields() {
			if (inEditMode) {
				addButton.setText("Save");
				nameEditText.setText(itemToEdit.getName());
				quantityEditText.setText(Utilities.Math.formatFloat(itemToEdit.getQuantity()));
				unitEditText.setText(itemToEdit.getUnit());
				typeEditText.setText(itemToEdit.getType());
				if (!itemToEdit.getExpiresDate().isEmpty()) {
					expirationButton.setText(
							itemToEdit.getExpiresDate());
					dateSet = true;
				}
				setInventorySpinnerPosition(itemToEdit.getInventory());
			} else if (currentInventory != null) {
				setInventorySpinnerPosition(currentInventory);
			}
		}

		private void setAutoCompleteViews() {
			if (getActivity() instanceof MainActivity) {
				SuggestionManager suggestionManager =
						((MainActivity) getActivity())
						.getSuggestionManager();

				final ArrayList<SuggestionItem> items =
						suggestionManager.getItemSuggestions();
				ArrayAdapter<SuggestionItem> nameAdapter =
						new ArrayAdapter<SuggestionItem>(getContext(),
								R.layout.simple_dropdown_item_1line,
								items);
				nameEditText.setAdapter(nameAdapter);
				nameEditText.setOnItemClickListener(
						(parent, view, position, id) -> {

					SuggestionItem item = items.get(position);
					if (!item.getDefaultUnit().equals("none")) {
						unitEditText.setText(item.getDefaultUnit());
					}
					if (!item.getType().equals("none")) {
						typeEditText.setText(item.getType());
					}
					if (!item.getDefaultExpiration().equals("none")) {
						expirationButton.setText(
								TimeManager.getDateFromSuggestion(
										item.getDefaultExpiration()));
						dateSet = true;
					}
				});

				ArrayAdapter<String> typeAdapter =
						new ArrayAdapter<String>(getContext(),
								R.layout.simple_dropdown_item_1line,
								suggestionManager.getTypeSuggestions());
				typeEditText.setAdapter(typeAdapter);

				ArrayAdapter<String> unitAdapter =
						new ArrayAdapter<String>(getContext(),
								R.layout.simple_dropdown_item_1line,
								suggestionManager.getUnitSuggestions());
				unitEditText.setAdapter(unitAdapter);
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

		/**
		 *
		 * @param dateToSet a date string formatted according to the
		 *                  format defined in TimeManager, or null if
		 *                  no date has been set yet.
		 */
		@SuppressWarnings("ResourceType")
		private void setUpPopupWindow(String dateToSet) {
			ExpirationPickerPopup popup = new ExpirationPickerPopup
					(this.getContext(), dateToSet);
			popup.setClearButtonClickListener(this::clearExpirationDate);
			popup.setSaveButtonClickListener(this::setExpirationDate);

			popup.showAtLocation(typeEditText,
					Gravity.START,
					(int) expirationButton.getX(),
					(int) expirationButton.getY());
		}

		void clearExpirationDate() {
			String text = getResources().getString(
					R.string.expires_button_default_string);
			expirationButton.setText(text);
			dateSet = false;
		}

		void setExpirationDate(int year, int month, int day) {
			final SimpleDateFormat sdFormat =
					new SimpleDateFormat(
							TimeManager.DEFAULT_DATE_FORMAT);

			Calendar cal = Calendar.getInstance();
			cal.set(year, month, day);
			String date = sdFormat.format(cal.getTime());
			expirationButton.setText(date);

			dateSet = true;
		}
	}

}
