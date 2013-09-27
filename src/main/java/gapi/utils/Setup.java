package gapi.utils;

public class Setup {

	public static String getServiceAccountEmail() {
		return System.getenv("QS_SERVICE_ACCOUNT_EMAIL");
	}

	public static String getServiceAccountKeyPath() {
		return System.getenv("QS_SERVICE_ACCOUNT_KEY_PATH");
	}

	public static String getJdbcUrl() {
		return String.format("jdbc:postgresql://%s:%s/%s", System.getenv("QS_HOST"), System.getenv("QS_PORT"),
				System.getenv("QS_DBNAME"));
	}

	public static String getUser() {
		return System.getenv("QS_USER");
	}
	
	public static String getPassword() {
		return System.getenv("QS_PASSWORD");
	}

	public static String getSetupSpreadsheetKey() {
		return System.getenv("QS_SETUP_SPREADSHEET_KEY");
	}
}
