package gapi.utils;

public class Setup {

	public static String getServiceAccountEmail() {
		return System.getenv("GAPI_SERVICE_ACCOUNT_EMAIL");
	}

	public static String getServiceAccountKeyPath() {
		return System.getenv("GAPI_SERVICE_ACCOUNT_KEY_PATH");
	}

}
