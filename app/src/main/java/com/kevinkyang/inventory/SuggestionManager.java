package com.kevinkyang.inventory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Kevin on 2/9/2017.
 */

public class SuggestionManager {
	private Context context;

	public SuggestionManager(Context context) {
		this.context = context;
	}

	/**
	 * Checks if there is a newer version of the
	 * suggestion database online, and downloads it.
	 */
	public void checkSuggestionDb() {
		/** TODO
		 * For now, just downloads from Firebase Storage
		 *
		 * Will have to check for locally downloaded Db first, then
		 * check for version in assets/, take newest version
		 * and compare to Firebase site version file,
		 * then download from Firebase Storage if needed.
		 */

		StorageReference dbStorageRef =
				FirebaseStorage.getInstance().getReference().child("database.json");
		final File localFile = new File(context.getFilesDir(), "suggestion_database.json");
		dbStorageRef.getFile(localFile)
			.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

					/** TODO need to handle success and failure
					 */
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {

				}
			});

	}
}
