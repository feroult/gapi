package com.github.feroult.gapi;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import com.github.feroult.gapi.spreadsheet.SpreadsheetAPIFactory;
import com.github.feroult.gapi.utils.Setup;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential.Builder;
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
	
	private static final String APPLICATION_NAME = "gapi";

	private DriveAPI drive;
	private SpreadsheetAPI spreadsheet;
	private DirectoryAPI group;

	public DriveAPI drive() {
		if (drive == null) {
			drive = new DriveAPI(driveService());
		}
		return drive;
	}

	public SpreadsheetAPI spreadsheet(String key) {
		if (spreadsheet == null) {
			spreadsheet = SpreadsheetAPIFactory.create(spreadsheetService());
		}
		return spreadsheet.key(key);
	}

	public DirectoryAPI directory() {
		if (group == null) {
			group = new DirectoryAPI(directoryService());
		}
		return group;
	}

	private Credential createCredential(List<String> scopes, boolean userServiceAccountUser) {
		boolean isCredentialGenerationEnabled = false; // FIXME review this
		if (isCredentialGenerationEnabled && Setup.isAppEngineProduction()) {
			return generateCredentialForGae(scopes);
		}
		return generateCredentialWithP12(scopes, userServiceAccountUser);
	}

	private GoogleCredential generateCredentialWithP12(List<String> scopes, boolean useServiceAccountUser) {
		try {
			Builder builder = new GoogleCredential.Builder().setTransport(getTransport()).setJsonFactory(getJsonFactory())
			        .setServiceAccountId(Setup.getServiceAccountEmail()).setServiceAccountScopes(scopes)
			        .setServiceAccountPrivateKeyFromP12File(Setup.getServiceAccountKeyFile());

			if (useServiceAccountUser) {
				builder.setServiceAccountUser(Setup.getServiceAccountUser());
			}

			return builder.build();
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Credential generateCredentialForGae(List<String> scopes) {
		AppIdentityCredential appIdentityCredential = new AppIdentityCredential.Builder(scopes).build();
		AppIdentityService appIdentityService = appIdentityCredential.getAppIdentityService();
		GetAccessTokenResult accessTokenResult = appIdentityService.getAccessToken(scopes);

		GoogleCredential credential = new GoogleCredential();
		credential.setAccessToken(accessTokenResult.getAccessToken());
		credential.setExpirationTimeMilliseconds(accessTokenResult.getExpirationTime().getTime());

		return credential;
	}

	private SpreadsheetService spreadsheetService() {
		SpreadsheetService service = new SpreadsheetService(APPLICATION_NAME);
		service.setOAuth2Credentials(createCredential(SpreadsheetAPI.SCOPES, false));
		service.setConnectTimeout(120 * 1000);
		return service;
	}

	private Drive driveService() {
		Credential credential = createCredential(DriveAPI.SCOPES, false);
		return new Drive.Builder(getTransport(), getJsonFactory(), credential).setApplicationName("gapi").build();
	}

	private Directory directoryService() {
		Directory.Builder builder = new Directory.Builder(getTransport(), getJsonFactory(), createCredential(DirectoryAPI.SCOPES, true));
		builder.setApplicationName(APPLICATION_NAME);
		return builder.build();
	}

	private HttpTransport getTransport() {
		return new NetHttpTransport();
	}

	private JsonFactory getJsonFactory() {
		return new GsonFactory();
	}

}
