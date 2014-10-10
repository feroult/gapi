package gapi;

import gapi.utils.Setup;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.drive.Drive;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityService.GetAccessTokenResult;
import com.google.gdata.client.spreadsheet.SpreadsheetService;

public class GoogleAPI {
	private Credential credential;

	private DriveAPI drive;
	private SpreadsheetAPI spreadsheet;
	private GoogleGroupAPI group;

	public GoogleAPI() {
		try {

			List<String> scopes = Arrays.asList("https://spreadsheets.google.com/feeds", "https://docs.google.com/feeds");

//			if (Setup.isAppEngineProduction()) {
//				credential = generateCredentialForGae(scopes);
//				return;
//			}

			credential = generateCredentialWithP12(scopes);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public GoogleAPI(List<String> accountscopes) {
		try {
			credential = generateCredentialWithP12(accountscopes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public SpreadsheetService spreadsheetService() {
		SpreadsheetService service = new SpreadsheetService("gapi");
		service.setOAuth2Credentials(credential);
		service.setConnectTimeout(120 * 1000);
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

	public GoogleGroupAPI group() throws GeneralSecurityException, IOException, URISyntaxException {
		if (group == null) {
			group = new GoogleGroupAPI(directoryService());
		}
		return group;
	}

	private GoogleCredential generateCredentialWithP12(List<String> scopes) throws GeneralSecurityException, IOException {
		return new GoogleCredential.Builder().setTransport(getTransport()).setJsonFactory(getJsonFactory())
				.setServiceAccountId(Setup.getServiceAccountEmail()).setServiceAccountScopes(scopes)
				//.setServiceAccountUser(Setup.getServiceAccountUser())
				.setServiceAccountPrivateKeyFromP12File(Setup.getServiceAccountKeyFile()).build();
	}

	private Credential generateCredentialForGae(List<String> scopes) {
		AppIdentityCredential appIdentityCredential = new AppIdentityCredential.Builder(scopes).build();
		AppIdentityService appIdentityService = appIdentityCredential.getAppIdentityService();
		GetAccessTokenResult accessTokenResult = appIdentityService.getAccessToken(scopes);

		GoogleCredential cred = new GoogleCredential();
		cred.setAccessToken(accessTokenResult.getAccessToken());
		cred.setExpirationTimeMilliseconds(accessTokenResult.getExpirationTime().getTime());
		return cred;
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

	private Directory directoryService() throws GeneralSecurityException, IOException, URISyntaxException {
		return new Directory.Builder(getTransport(), getJsonFactory(), credential).setApplicationName("gapi").build();
	}

}
