package com.kevinkyang.inventory;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
	private void checkSuggestionDb() {
		/** TODO
		 * For now, just downloads from Firebase Storage
		 *
		 * Will have to check for locally downloaded Db first, then
		 * check for version in assets/, take newest version
		 * and compare to Firebase site version file,
		 * then download from Firebase Storage if needed.
		 */

		StorageReference dbStorageRef =
				FirebaseStorage.getInstance().getReference().child("db.json");
		try {
			final File tempFile = File.createTempFile("suggestion_database", "json");
			dbStorageRef.getFile(tempFile)
					.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
						@Override
						public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
							File localFile = new File(context.getFilesDir(), "suggestion_database.json");

							/** TODO need internet permissions,
							 * need to validate that this code works
							 */
							FileChannel in = null;
							FileChannel out = null;
							try {
								in = new FileInputStream(tempFile).getChannel();
								out = new FileOutputStream(localFile).getChannel();
								in.transferTo(0, in.size(), out);
							} catch (IOException e) {
								e.printStackTrace();
							} finally {
								try {
									if (in != null) {
										in.close();
									}
									if (out != null) {
										out.close();
									}
								} catch (IOException e2) {
									e2.printStackTrace();
								}
							}
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {

						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
