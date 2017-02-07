package com.kevinkyang.inventory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GroceryListActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Grocery List");
		setContentView(R.layout.activity_grocery_list);
	}
}
