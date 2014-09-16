package gapi;

import gapi.utils.Setup;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.drive.Drive;
import com.google.gdata.client.spreadsheet.SpreadsheetService;

public class GoogleAPI {

	private Credential credential;

	private DriveAPI drive;

	private SpreadsheetAPI spreadsheet;

	public GoogleAPI(List<String> accountscopes) {
		try {
			credential = new GoogleCredential.Builder()
							.setTransport(getTransport())
							.setJsonFactory(getJsonFactory())
							.setServiceAccountId(Setup.getServiceAccountEmail())
							.setServiceAccountScopes(accountscopes)
							.setServiceAccountUser(Setup.getServiceAccountUser())
							.setServiceAccountPrivateKeyFromP12File(Setup.getServiceAccountKeyFile()).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private HttpTransport getTransport() {
		return new NetHttpTransport();
	}

	private JsonFactory getJsonFactory() {
		return new GsonFactory();
	}

	private Drive driveService() {
		return new Drive.Builder(getTransport(), getJsonFactory(), credential).setApplicationName("gapi").build();
	}

	public SpreadsheetService spreadsheetService() {
		SpreadsheetService service = new SpreadsheetService("gapi");
		service.setOAuth2Credentials(credential);
		return service;
	}

	public DriveAPI drive() {
		if (drive == null) {
			drive = new DriveAPI(driveService());
		}
		return drive;
	}

	public SpreadsheetAPI spreadsheet(String key) {
		if (spreadsheet == null) {
			spreadsheet = new SpreadsheetAPI(spreadsheetService());
		}
		return spreadsheet.key(key);
	}

	public Directory getDirectoryService() throws GeneralSecurityException, IOException, URISyntaxException {
		return new Directory.Builder(getTransport(), getJsonFactory(), credential).setApplicationName("gapi").build();
	}
}
