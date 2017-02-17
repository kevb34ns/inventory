package com.kevinkyang.inventory;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.JsonReader;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Kevin on 2/9/2017.
 */

public class SuggestionManager {
	public static final String PREFS_NAME = "SuggestionPrefs";
	public static final String DATE_PREF = "lastCheckDate";
	public static final String LOCAL_VERSION_PREF = "localVersion";
	public static final String ASSET_VERSION_PREF = "assetVersion";

	public static final String VERSION_CHECK_URL = "https://inventory-3dbe4.firebaseapp.com/version.json";
	// TODO save file names as final Strings instead of hardcoding them in the code

	public static final int LOCAL_INDEX = 0;
	public static final int ASSET_INDEX = 1;

	private Context context;
	private ArrayList<SuggestionItem> data;

	public SuggestionManager(Context context) {
		this.context = context;
		data = new ArrayList<SuggestionItem>();
	}

	public void executeThread() {
		SuggestionDBTask task = new SuggestionDBTask();
		task.execute();
	}

	/**
	 * Get a copy of the suggestion list. This list is
	 * populated in a background thread, so it may be
	 * empty or incomplete when this method is called.
	 * @return a copy of the suggestion items array
	 */
	public ArrayList<SuggestionItem> getSuggestionData() {
		return new ArrayList<SuggestionItem>(data);
	}

	/**
	 * Checks if there is a newer version of the
	 * suggestion database online, and downloads it.
	 */
	private void checkSuggestionDb() {
		double latestVersion = -1.0;
		double localVersion = -1.0;

		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		if (mostRecentLocalDatabase() == LOCAL_INDEX) {
			localVersion = prefs.getFloat(LOCAL_VERSION_PREF, -1.0f);
		} else {
			localVersion = prefs.getFloat(ASSET_VERSION_PREF, -1.0f);
		}

		try {
			URL verURL = new URL(VERSION_CHECK_URL);
			JsonReader reader = new JsonReader(
					new InputStreamReader(
							verURL.openConnection().getInputStream()));
			reader.beginObject();
			String name = reader.nextName();
			if (name.equals("version")) {
				latestVersion = reader.nextDouble();
			} else {
				throw new IOException("Invalid Version File Formatting");
			}
			reader.endObject();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		if (latestVersion > localVersion) {
			// download a newer version online
			// TODO for safety, should download to a temp file, then transfer to the local file when safely downloaded
			StorageReference dbStorageRef =
					FirebaseStorage.getInstance().getReference().child("database.json");
			final File localFile = new File(context.getFilesDir(), "suggestion_database.json");
			dbStorageRef.getFile(localFile)
					.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
						@Override
						public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
							SharedPreferences.Editor prefsEditor =
									context.getSharedPreferences(PREFS_NAME, 0)
									.edit();
							updatePrefs(prefsEditor);
							String date = TimeManager.getDateTimeLocal();
							prefsEditor.putString(DATE_PREF, date);
							prefsEditor.apply();
							readDatabase();
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							readDatabase();
							e.printStackTrace();
						}
					});
		}

	}

	private void readDatabase() {
		JsonReader reader = null;
		try {
			if (mostRecentLocalDatabase() == ASSET_INDEX) {
				reader = new JsonReader(
						new InputStreamReader(
								context.getAssets().open("suggestion_database.json")));
			} else {
				File localFile = new File(context.getFilesDir(), "suggestion_database.json");
				reader = new JsonReader(
						new FileReader(localFile));
			}

			reader.beginObject();
			String key = reader.nextName();
			if (key.equals("database")) {
				reader.beginObject();
				while (reader.hasNext()) {
					key = reader.nextName();
					if (key.equals("items")) {
						reader.beginArray();
						while (reader.hasNext()) {
							data.add(readSuggestionItem(reader));
						}
						reader.endArray();
					} else {
						reader.skipValue();
					}
				}
				reader.endObject();
			}
			reader.endObject();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private SuggestionItem readSuggestionItem(JsonReader reader) throws IOException {
		SuggestionItem item = new SuggestionItem();

		reader.beginObject();
		while (reader.hasNext()) {
			String key = reader.nextName();
			if (key.equals("name")) {
				item.setName(reader.nextString());
			} else if (key.equals("type")) {
				item.setType(reader.nextString());
			} else if (key.equals("def_expr")) {
				item.setDefaultExpiration(reader.nextString());
			} else if (key.equals("def_unit")) {
				item.setDefaultUnit(reader.nextString());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();

		return item;
	}

	private void updatePrefs(SharedPreferences.Editor editor) {
		try {
			JsonReader assetReader = new JsonReader(
					new InputStreamReader(
							context.getAssets().open("suggestion_database.json")));
			double assetVersion = getVersionFromFile(assetReader);
			if (assetVersion >= 0.0) {
				editor.putFloat(ASSET_VERSION_PREF, (float) assetVersion);
			}
			assetReader.close();

			File localFile = new File(context.getFilesDir(), "suggestion_database.json");
			if (localFile.exists()) {
				JsonReader localReader = new JsonReader(
						new FileReader(localFile));
				double localVersion = getVersionFromFile(localReader);
				if (localVersion >= 0.0) {
					editor.putFloat(LOCAL_VERSION_PREF, (float) localVersion);
				}
				localReader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double getVersionFromFile(JsonReader reader) throws IOException {
		double version = -1.0;
		reader.beginObject();
		String key = reader.nextName();
		if (key.equals("database")) {
			reader.beginObject();
			while (reader.hasNext()) {
				key = reader.nextName();
				if (key.equals("version")) {
					version = reader.nextDouble();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		}
		reader.endObject();

		return version;
	}

	private boolean hasCheckedOnlineToday() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		String date = TimeManager.getDateTimeLocal();
		String prefDate = prefs.getString(DATE_PREF, "none");
		return date.equals(prefDate);
	}

	/**
	 * Looks at the version number of the bundled suggestion
	 * database and the one in internal storage (if it exists)
	 * and determines the more up-to-date one.
	 * @return the index of the most recent suggestion database.
	 */
	private int mostRecentLocalDatabase() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		double localVersion = prefs.getFloat(LOCAL_VERSION_PREF, -1.0f);
		double assetVersion = prefs.getFloat(ASSET_VERSION_PREF, -1.0f);
		if (localVersion > assetVersion && localVersion >= 0.0) {
			return LOCAL_INDEX;
		} else {
			return ASSET_INDEX;
		}
	}

	/**
	 * TODO temp function that deletes prefs and deletes internal storage file
	 */
	public void clearData() {
		SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, 0).edit();
		editor.clear();
		editor.commit();
		context.deleteFile("suggestion_database.json");
	}

	/**
	 * Checks for suggestion database in a separate thread
	 * TODO need to understand the params in AsyncTask< >
	 */
	private class SuggestionDBTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			if (!hasCheckedOnlineToday()) {
				checkSuggestionDb();
				// TODO ensure that all cases are handled, such as first time opening the app (no internal storage db), app has been updated (new assets/ db), local file gets corrupted but the prefs tell the app to use it, causing the app to crash when it tries to parse the file
			} else {
				readDatabase();
			}

			return null;
		}
	}
}
