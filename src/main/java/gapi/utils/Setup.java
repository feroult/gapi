package gapi.utils;

import java.io.File;
import java.net.URL;

public class Setup {

	public static String getServiceAccountEmail() {
		return System.getenv("GAPI_SERVICE_ACCOUNT_EMAIL");
	}

	private static String getServiceAccountKey() {
		return System.getenv("GAPI_SERVICE_ACCOUNT_KEY");
	}

	public static File getServiceAccountKeyFile() {
		URL resource = Setup.class.getResource(getServiceAccountKey());
		if (resource != null) {
			return new File(resource.getFile());
		}

		return new File(Setup.getServiceAccountKey());
	}
}
