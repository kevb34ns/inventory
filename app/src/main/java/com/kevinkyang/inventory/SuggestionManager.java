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
import java.lang.reflect.Array;
import java.net.MalformedURLException;
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
	 * @param localVersion the most recent version of the
	 *                     suggestion database currently
	 *                     on the device.
	 */
	private void checkSuggestionDb(double localVersion) {
		double latestVersion = -1.0;

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
							/** TODO need to handle success and failure?
							 */
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							e.printStackTrace();
						}
					});
		}

	}

	private void readDatabase(int location) {
		JsonReader reader = null;
		try {
			if (location == ASSET_INDEX) {
				reader = new JsonReader(
						new InputStreamReader(
								context.getAssets().open("suggestion_database.json")));
			} else if (location == LOCAL_INDEX) {
				File localFile = new File(context.getFilesDir(), "suggestion_database.json");
				reader = new JsonReader(
						new FileReader(localFile));
			} else {
				return;
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
			JsonReader localReader = new JsonReader(
					new FileReader(localFile));
			double localVersion = getVersionFromFile(localReader);
			if (localVersion >= 0.0) {
				editor.putFloat(LOCAL_VERSION_PREF, (float) getVersionFromFile(localReader));
			}
			localReader.close();
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
					break;
				}
			}
			reader.endObject();
		}
		reader.endObject();

		return version;
	}

	/**
	 * Checks for suggestion database in a separate thread
	 * TODO need to understand the params in AsyncTask< >
	 */
	private class SuggestionDBTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor prefsEditor = prefs.edit();
			String date = TimeManager.getDateTimeLocal();
			String prefDate = prefs.getString(DATE_PREF, "none");
			double localVersion = prefs.getFloat(LOCAL_VERSION_PREF, -1.0f);
			double assetVersion = prefs.getFloat(ASSET_VERSION_PREF, -1.0f);
			if (prefDate.equals(date)) {
				// already checked today
				if (localVersion > assetVersion && localVersion >= 0.0) {
					readDatabase(LOCAL_INDEX);
				} else {
					readDatabase(ASSET_INDEX);
				}
			} else {
				prefsEditor.putString(DATE_PREF, date);
				if (localVersion > assetVersion && localVersion >= 0.0) {
					checkSuggestionDb(localVersion);
					readDatabase(LOCAL_INDEX);
				} else {
					checkSuggestionDb(assetVersion);
					readDatabase(ASSET_INDEX);
				}
				updatePrefs(prefsEditor);
				// TODO lots of repeated method calls inside these methods (ie reading from the same files a bunch), see if you can fix
			}

			prefsEditor.apply();
			return null;
		}
	}
}
