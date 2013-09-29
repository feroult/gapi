package gapi.utils;

import java.io.File;
import java.net.URL;

public class Setup {

	public static final String GAPI_SERVICE_ACCOUNT_EMAIL = "gapi.service.account.email";
	public static final String GAPI_SERVICE_ACCOUNT_KEY = "gapi.service.account.key";

	public static String getServiceAccountEmail() {
		return System.getProperty(GAPI_SERVICE_ACCOUNT_EMAIL);
	}

	private static String getServiceAccountKey() {
		return System.getProperty(GAPI_SERVICE_ACCOUNT_KEY);
	}

	public static File getServiceAccountKeyFile() {
		URL resource = Setup.class.getResource(getServiceAccountKey());
		if (resource != null) {
			return new File(resource.getFile());
		}

		return new File(Setup.getServiceAccountKey());
	}
}
