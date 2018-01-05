package com.github.feroult.gapi;

import com.google.api.services.drive.Drive;

import java.util.Arrays;
import java.util.List;

public class DriveAPI {

	public static List<String> SCOPES = Arrays.asList("https://docs.google.com/feeds");

	private Drive driveService;

	public DriveAPI(Drive driveService) {
		this.driveService = driveService;
	}

	public String createSpreadsheet() {
		try {
			com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();

			file.setTitle("simple test");
			file.setMimeType("application/vnd.google-apps.spreadsheet");

			file = driveService.files().insert(file).execute();

			return file.getId();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void delete(String key) {
		try {
			driveService.files().delete(key).execute();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
