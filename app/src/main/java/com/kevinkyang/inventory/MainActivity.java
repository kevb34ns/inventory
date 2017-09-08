package com.kevinkyang.inventory;

import android.app.PendingIntent;
import android.app.SearchManager;
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
import android.support.v7.widget.SearchView;
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

import com.kevinkyang.expandableRVAdapter.ExpandableRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity implements ItemChangeListener {
	public static final String TAG = "inventory";
	public static final int SETTINGS_REQUEST = 0x73;

	private ItemManager mItemManager = null;
	private DBManager mDbManager = null;
	private SuggestionManager mSuggestionManager;
	private FloatingActionButton mFloatingAddButton;

	private SearchView mSearchView;

	private DrawerLayout mDrawerLayout;
	private RecyclerView mDrawerRV;
	private DrawerRVAdapter mDrawerRVAdapter;
	private ExpandableDrawerAdapter mDrawerAdapter;
	private LinearLayoutManager mDrawerLayoutManager;

	private ImageButton mDrawerSettingsButton;

	// add item widget views
	private LinearLayout mAddItemWidget;
	private EditText mAddItemEditText;
	private ImageButton mEditNewItemButton;
	private ImageButton mAddNewItemButton;

	private Toolbar mToolbar;

	private InventoryFragment mInventoryFragment;
	private GroceryFragment mGroceryFragment;
	private boolean mInGroceryMode;

	private TypedArray mColorArray;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		mItemManager = ItemManager.getInstance();
		if (!mItemManager.isInitialized()) {
			mItemManager.init(this);
		}
		mDbManager = DBManager.getInstance();

		mSuggestionManager = new SuggestionManager(this);
		mSuggestionManager.executeThread();

		mFloatingAddButton = (FloatingActionButton) findViewById(R.id.add_item_button);
		mFloatingAddButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent, null)));
		mFloatingAddButton.setOnClickListener(view -> toggleAddButton(false));

		// Set up mToolbar
		// TODO when returning from an onDestroy() (eg orientation change), the color of the inventory is not preserved; fix the bug so that the right color is shown
		mToolbar = (Toolbar) findViewById(R.id.custom_action_bar);
		setSupportActionBar(mToolbar);
		changeActionBarTitle("Inventory");

		mToolbar.setNavigationOnClickListener((v) -> {
			if (mDrawerLayout != null &&
					!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {

				mDrawerLayout.openDrawer(Gravity.LEFT);
			}
		});

		FragmentManager fragmentManager = getSupportFragmentManager();

		mInventoryFragment = (InventoryFragment) fragmentManager
				.findFragmentByTag("0");
		if (mInventoryFragment == null) {
			mInventoryFragment = new InventoryFragment();
			fragmentManager.beginTransaction()
					.add(R.id.fragment_container,
							mInventoryFragment,
							"0")
					.commit();
		}

		mGroceryFragment = (GroceryFragment) fragmentManager
				.findFragmentByTag("1");
		if (mGroceryFragment == null) {
			mGroceryFragment = new GroceryFragment();
			fragmentManager.beginTransaction()
					.add(R.id.fragment_container,
							mGroceryFragment,
							"1")
					.hide(mGroceryFragment)
					.commit();
		}

		//TODO should probably handle the 'getIntent' below this in handleIntent too
		handleIntent(getIntent());

		Intent intent = getIntent();
		String intentInventory = intent.getStringExtra("inventory");
		if (intentInventory != null) {
			mInventoryFragment.setInventory(intentInventory);
			changeActionBarTitle(intentInventory);
		}

		if (savedInstanceState != null) {
			mInGroceryMode =
					savedInstanceState
							.getBoolean("mInGroceryMode", false);
		} else {
			mInGroceryMode = false;
		}

		mColorArray = null;

		initializeDrawer();
		addItemWidgetSetup();
		checkNotificationStatus();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("mInGroceryMode", mInGroceryMode);
		super.onSaveInstanceState(outState);
}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// TODO on settings result, check resultCode that settings were changed, and get the changed settings from the intent, and apply them, if applicable (some settings wouldn't require any action)
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			// TODO eventually, use searchview's onquerytextlistener to search every time the search text changes (put in an autocompletetextview maybe?

			mToolbar.collapseActionView();
			if (mItemManager != null) {
				mItemManager.search(query, (items) -> {
					mInventoryFragment.showSearchResults(query, items);
				});
				changeFragments("0");
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_options_menu, menu);

		SearchManager searchManager =
				(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		mSearchView =
				(SearchView) menu.findItem(R.id.options_item_search)
						.getActionView();
		mSearchView.setSearchableInfo(
				searchManager.getSearchableInfo(getComponentName()));

		EditText searchField = (EditText) mSearchView.findViewById(
				android.support.v7.appcompat.R.id.search_src_text);
		searchField.setTextColor(getResources()
				.getColor(R.color.defaultTextColor));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ExpirationManager manager = null; //TODO fix this
		switch (item.getItemId()) {
			// TODO some options for testing only; get rid of it
			case R.id.options_item_suggestions:
				ArrayList<SuggestionItem> sItems = mSuggestionManager.getItemSuggestions();
				String msg = "Suggestions:\n";
				for (SuggestionItem sItem : sItems) {
					msg += sItem.getName() + "\n";
				}
				Log.d(TAG, msg);
				return true;
			case R.id.options_item_clear:
				mSuggestionManager.clearData();
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
		if (mAddItemWidget != null && mAddItemWidget.getVisibility() == View.VISIBLE) {
			//TODO find way to make additemwidget invisible on keyboard hide
			mAddItemEditText.clearFocus();
		} else {
			super.onBackPressed();
		}
	}

	private void initializeDrawer() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setStatusBarBackground(R.color.colorPrimary);

		ArrayList<DrawerGroupItem> groups = new ArrayList<>();
		groups.add(new DrawerGroupItem("Inventory"));
		groups.add(new DrawerGroupItem("Expiring"));
		groups.add(new DrawerGroupItem("Grocery List"));

		ArrayList<ArrayList<DrawerChildItem>> children = new ArrayList<>();
		children.add(getInventoryChildren());
		children.add(new ArrayList<>());
		children.add(new ArrayList<>());

		mDrawerRV = (RecyclerView) findViewById(R.id.drawer_rv_list);
		mDrawerAdapter = new ExpandableDrawerAdapter(this, groups, children);
		mDrawerRV.setAdapter(mDrawerAdapter);
		mDrawerLayoutManager = new LinearLayoutManager(this);
		mDrawerRV.setLayoutManager(mDrawerLayoutManager);

		mDrawerAdapter.setOnItemClickListener(new ExpandableRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public boolean onGroupClick(int groupPosition) {
				String title = mDrawerAdapter.getGroup(groupPosition).getName();
				switch (title) {
					case "Inventory":
						mInventoryFragment.showInventory(null);
						changeFragments("0");
						break;
					case "Expiring":
						mInventoryFragment.showInventory(title);
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
				String title = mDrawerAdapter.getGroup(groupPosition).getName();
				if (title.equals("Inventory")) {
					if (childPosition != mDrawerAdapter.getChildrenCount(groupPosition) - 1) {
						String inventory = (String) mDrawerAdapter
								.getChild(groupPosition, childPosition)
								.getName();
						mInventoryFragment.showInventory(inventory);
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

		mDrawerSettingsButton = (ImageButton)
				findViewById(R.id.drawer_settings_button);
		mDrawerSettingsButton.setOnClickListener((v -> {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, SETTINGS_REQUEST);
		}));

		// TODO fix R.string.app_name
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
		toggle.syncState();
	}

	public void changeActionBarTitle(String title) {
		// TODO the way the title is changed right now is not very integrated; whoever changes the inventory is responsible for changing the mToolbar title. This needs to be overhauled so that any time the inventory is changed, the title is guaranteed to be changed to the correct thing
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
						} else if (mDbManager.getInventories().contains(newInventory)) {
							String msg = "This inventory already exists.";
							Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
						} else {
							mDbManager.addInventory(newInventory);
							mDrawerRVAdapter.addInventory(newInventory);
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
		for (String name : mDbManager.getInventories()) {
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
		mInGroceryMode = tag.equals("1"); //TODO must change once structure of drawer changes
		Fragment otherFragment;
		otherFragment = fragmentManager.findFragmentByTag(otherTag);

		fragmentManager.beginTransaction()
				.hide(otherFragment)
				.commit();

		fragmentManager.beginTransaction()
				.show(fragment)
				.commit();

		refreshCurrentList();

		mDrawerLayout.closeDrawers();
	}

	private void toggleAddButton(boolean isVisible) {
		boolean currentlyVisible = mFloatingAddButton.getVisibility() ==
				View.VISIBLE;
		if (currentlyVisible == isVisible) {
			return;
		}

		if (isVisible) {

			mFloatingAddButton.setVisibility(View.VISIBLE);
			Animation growAnim = AnimationUtils.loadAnimation(MainActivity.this,
					R.anim.button_grow);
			mFloatingAddButton.startAnimation(growAnim);
		} else {

			Animation shrinkAnim = AnimationUtils.loadAnimation(MainActivity.this,
					R.anim.button_shrink);
			shrinkAnim.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mFloatingAddButton.setVisibility(View.GONE);
					toggleAddItemWidget(true);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			mFloatingAddButton.startAnimation(shrinkAnim);
		}
	}

	private void toggleAddItemWidget(boolean isVisible) {
		boolean currentlyVisible = mAddItemWidget.getVisibility() ==
				View.VISIBLE;
		if (currentlyVisible == isVisible) {
			return;
		}

		if (isVisible) {

			mAddItemWidget.setVisibility(View.VISIBLE);
			mAddItemEditText.requestFocus();
			InputMethodManager imMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imMgr.showSoftInput(mAddItemEditText, 0);

		} else {

			mAddItemWidget.setVisibility(View.GONE);
			InputMethodManager imMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imMgr.hideSoftInputFromWindow(mAddItemEditText.getWindowToken(),
					0);
		}
	}

	private void quickAddItem() {
		String name = mAddItemEditText.getText().toString();
		if (name.isEmpty()) {
			return;
		}

		boolean inGroceryList = isInGroceryMode();
		String inventory = "";
		if (!inGroceryList &&
				mInventoryFragment.getCurrentInventory() != null) {

			inventory = mInventoryFragment.getCurrentInventory();
		}

		onItemAdded(name, 1, "", "", "", inventory, inGroceryList);

		mAddItemEditText.setText("");
	}

	private void addItemWidgetSetup() {
		mAddItemWidget = (LinearLayout) findViewById(R.id.add_item_widget);
		mAddItemEditText = (EditText) findViewById(R.id.widget_edittext);

		mEditNewItemButton = (ImageButton)
				findViewById(R.id.widget_more_button);
		mEditNewItemButton.setOnClickListener((v -> {
			AddItemDialog dialog = new AddItemDialog();

			Bundle args = new Bundle();
			if (!mInGroceryMode) {
				args.putString("Inventory",
						mInventoryFragment.getCurrentInventory());
			}

			String name = mAddItemEditText.getText().toString();
			if (!name.isEmpty()) {
				args.putString("NameToSet", name);
			}

			dialog.setArguments(args);
			dialog.show(getSupportFragmentManager(), "dialog");

			mAddItemEditText.setText("");
			mAddItemWidget.setVisibility(View.GONE);
			toggleAddButton(true);
		}));

		mAddNewItemButton = (ImageButton)
				findViewById(R.id.widget_add_button);

		mAddNewItemButton.setOnClickListener((v -> {
			quickAddItem();
		}));

		mAddItemEditText.setFocusableInTouchMode(true);
		mAddItemEditText.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus) {
				toggleAddItemWidget(false);
				toggleAddButton(true);
			}
		});
		mAddItemEditText.setOnEditorActionListener(((v, actionId, event) -> {
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
		mItemManager.addItem(item);
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

		mItemManager.updateItem(item);
		saveToCurrentList(position);
	}

	@Override
	public void onDialogDismissed() {
		mFloatingAddButton.setVisibility(View.VISIBLE);
		Animation growAnim = AnimationUtils.loadAnimation(MainActivity.this,
				R.anim.button_grow);
		mFloatingAddButton.startAnimation(growAnim);
	}

	@Override
	public boolean isInGroceryMode() {
		return mInGroceryMode;
	}

	/**
	 *
	 * @param item
	 * @param position mPosition of the item in the current
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
				mGroceryFragment.isInitFinished()) {
			changeFragments("1");
		} else if (mInventoryFragment.isInitFinished()) {
			changeFragments("0");
		}
	}

	private void refreshCurrentList() {
		if (isInGroceryMode()) {
			mGroceryFragment.refresh();
		} else {
			mInventoryFragment.refresh();
		}
	}

	private void addToCurrentList(Item item) {
		if (isInGroceryMode()) {
			mGroceryFragment.itemAdded(item);
		} else {
			mInventoryFragment.itemAdded(item);
		}
	}

	private void saveToCurrentList(int position) {
		if (isInGroceryMode()) {
			mGroceryFragment.itemSaved(position);
		} else {
			mInventoryFragment.itemSaved(position);
		}
	}

	private void setUIColor(int color) {
		//TODO don't need to change mToolbar color anymore, and must set FAB to accent, not primary
		mToolbar.setBackgroundColor(color);
		mFloatingAddButton.setBackgroundTintList(
				ColorStateList.valueOf(
						color));
		mDrawerLayout.setStatusBarBackgroundColor(color);
	}

	private int getInventoryColor(int position) {
		if (mColorArray == null) {
			mColorArray = getResources().obtainTypedArray(R.array.array_inventory_colors);
		}

		return mColorArray.getColor(position, 0);
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
		return mSuggestionManager;
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

	// mimics Java 8 functional interface, since my minSDKVersion < 24
	public static interface BiConsumer<T, U> {
		public void accept(T t, U u);
	}

	public static class AddItemDialog extends DialogFragment {
		private AutoCompleteTextView mNameEditText;
		private EditText mQuantityEditText;
		private AutoCompleteTextView mUnitEditText;
		private AutoCompleteTextView mTypeEditText;
		private Button mExpirationButton;
		private Spinner mInventorySpinner;
		private Button mAddButton;
		private Button mCancelButton;

		private String mCurrentInventory;
		private boolean mInEditMode;
		private Item mItemToEdit;
		private int mPosition;
		private String mNameToSet;

		private boolean mDateSet;

		public AddItemDialog() {

		}

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle args = getArguments();
			if (args != null) {
				mInEditMode = args.getBoolean("InEditMode");
				mItemToEdit = args.getParcelable("ItemParcel");
				mCurrentInventory = args.getString("Inventory");
				mPosition = args.getInt("Position");
				mNameToSet = args.getString("NameToSet");

			}

			mDateSet = false;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ItemChangeListener parent = (ItemChangeListener) getActivity();
			if (mInEditMode) {
				getDialog().setTitle("Edit Item");
			} else if (parent.isInGroceryMode()) {
				getDialog().setTitle("New Grocery Item");
			} else {
				getDialog().setTitle("New Item");
			}

			View view = inflater.inflate(R.layout.add_item_dialog, container, false);

			mNameEditText = (AutoCompleteTextView) view.findViewById(R.id.input_name);
			if (mNameToSet != null) {
				mNameEditText.setText(mNameToSet);
			}

			mQuantityEditText = (EditText) view.findViewById(R.id.input_quantity);
			mUnitEditText = (AutoCompleteTextView)
					view.findViewById(R.id.input_unit);
			mTypeEditText = (AutoCompleteTextView)
					view.findViewById(R.id.input_type);
			mExpirationButton = (Button) view.findViewById(R.id.button_expiration);

			mInventorySpinner = (Spinner) view.findViewById(R.id.spinner_inventory);
			ArrayList<String> inventories = DBManager.getInstance().getInventories();
			inventories.add(0, "Inventory");
			mInventorySpinner.setAdapter(new ArrayAdapter<String>(getContext(),
					android.R.layout.simple_spinner_dropdown_item,
					inventories));

			mAddButton = (Button) view.findViewById(R.id.add_button);
			mCancelButton = (Button) view.findViewById(R.id.cancel_button);

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
			mAddButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					String name = mNameEditText.getText().toString().trim();
					if (!name.isEmpty()) {
						String quantityString = mQuantityEditText.getText().toString();
						float quantity = 1;
						if (!quantityString.isEmpty()) {
							quantity = Float.parseFloat(quantityString);
						}
						String unitString = mUnitEditText.getText().toString();
						String typeString = mTypeEditText.getText().toString();
						String expiresString = "";
						if (mDateSet) {
							expiresString = mExpirationButton
									.getText()
									.toString();
						}
						String invString = "";
						if (mInventorySpinner.getSelectedItemPosition() != 0) {
							invString = (String) mInventorySpinner.getSelectedItem();
						}

						ItemChangeListener activity = (ItemChangeListener) getActivity();
						if (mInEditMode) {
							activity.onItemSaved(name, quantity,
									unitString, typeString, expiresString,
									invString, activity.isInGroceryMode(),
									mItemToEdit, mPosition);
						} else {
							activity.onItemAdded(name, quantity,
									unitString, typeString, expiresString,
									invString, activity.isInGroceryMode());
						}

						AddItemDialog.this.dismiss();
					}

				}
			});

			mCancelButton.setOnClickListener((view) -> {
				AddItemDialog.this.dismiss();
			});

			mExpirationButton.setOnClickListener((view) -> {
				if (mDateSet) {
					String date = mExpirationButton.getText().toString();
					setUpPopupWindow(date);
				} else {
					setUpPopupWindow(null);
				}
			});
		}

		private void populateFields() {
			if (mInEditMode) {
				mAddButton.setText("Save");
				mNameEditText.setText(mItemToEdit.getName());
				mQuantityEditText.setText(Utilities.Math.formatFloat(mItemToEdit.getQuantity()));
				mUnitEditText.setText(mItemToEdit.getUnit());
				mTypeEditText.setText(mItemToEdit.getType());
				if (!mItemToEdit.getExpiresDate().isEmpty()) {
					mExpirationButton.setText(
							mItemToEdit.getExpiresDate());
					mDateSet = true;
				}
				setInventorySpinnerPosition(mItemToEdit.getInventory());
			} else if (mCurrentInventory != null) {
				setInventorySpinnerPosition(mCurrentInventory);
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
				mNameEditText.setAdapter(nameAdapter);
				mNameEditText.setOnItemClickListener(
						(parent, view, position, id) -> {

					SuggestionItem item = items.get(position);
					if (!item.getDefaultUnit().equals("none")) {
						mUnitEditText.setText(item.getDefaultUnit());
					}
					if (!item.getType().equals("none")) {
						mTypeEditText.setText(item.getType());
					}
					if (!item.getDefaultExpiration().equals("none")) {
						mExpirationButton.setText(
								TimeManager.getDateFromSuggestion(
										item.getDefaultExpiration()));
						mDateSet = true;
					}
				});

				ArrayAdapter<String> typeAdapter =
						new ArrayAdapter<String>(getContext(),
								R.layout.simple_dropdown_item_1line,
								suggestionManager.getTypeSuggestions());
				mTypeEditText.setAdapter(typeAdapter);

				ArrayAdapter<String> unitAdapter =
						new ArrayAdapter<String>(getContext(),
								R.layout.simple_dropdown_item_1line,
								suggestionManager.getUnitSuggestions());
				mUnitEditText.setAdapter(unitAdapter);
			}
		}

		private void setInventorySpinnerPosition(String inventory) {
			for (int i = 1;
				 i < mInventorySpinner.getCount();
				 i++) {
				if (mInventorySpinner
						.getItemAtPosition(i)
						.equals(inventory)) {
					mInventorySpinner.setSelection(i);
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

			popup.showAtLocation(mTypeEditText,
					Gravity.START,
					(int) mExpirationButton.getX(),
					(int) mExpirationButton.getY());
		}

		void clearExpirationDate() {
			String text = getResources().getString(
					R.string.expires_button_default_string);
			mExpirationButton.setText(text);
			mDateSet = false;
		}

		void setExpirationDate(int year, int month, int day) {
			final SimpleDateFormat sdFormat =
					new SimpleDateFormat(
							TimeManager.DEFAULT_DATE_FORMAT);

			Calendar cal = Calendar.getInstance();
			cal.set(year, month, day);
			String date = sdFormat.format(cal.getTime());
			mExpirationButton.setText(date);

			mDateSet = true;
		}
	}

}
