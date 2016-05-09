package com.github.feroult.gapi.utils;

import java.io.File;
import java.net.URL;

import com.google.appengine.api.utils.SystemProperty;

public class Setup {

	private static final String GAPI_SERVICE_ACCOUNT_EMAIL = "gapi.service.account.email";
	private static final String GAPI_SERVICE_ACCOUNT_KEY = "gapi.service.account.key";
	private static final String GAPI_SERVICE_ACCOUNT_USER = "gapi.service.account.user";

	private static final String GAPI_SERVICE_ACCOUNT_EMAIL_ENV = "GAPI_SERVICE_ACCOUNT_EMAIL";
	private static final String GAPI_SERVICE_ACCOUNT_KEY_ENV = "GAPI_SERVICE_ACCOUNT_KEY";
	private static final String GAPI_SERVICE_ACCOUNT_USER_ENV = "GAPI_SERVICE_ACCOUNT_USER";

	public static String getServiceAccountEmail() {
		return getProperty(GAPI_SERVICE_ACCOUNT_EMAIL, GAPI_SERVICE_ACCOUNT_EMAIL_ENV);
	}

	public static String getServiceAccountUser() {
		return getProperty(GAPI_SERVICE_ACCOUNT_USER, GAPI_SERVICE_ACCOUNT_USER_ENV);
	}

	private static String getServiceAccountKey() {
		return getProperty(GAPI_SERVICE_ACCOUNT_KEY, GAPI_SERVICE_ACCOUNT_KEY_ENV);
	}

	private static String getProperty(String propertyKey, String environmentKey) {
		String property = System.getProperty(propertyKey);

		if (property == null) {
			return System.getenv(environmentKey);
		}

		return property;
	}

	public static File getServiceAccountKeyFile() {
		String key = getServiceAccountKey();
		
		URL resource = Setup.class.getResource(key);
		if (resource != null) {
			return new File(resource.getFile());
		}

		return new File(key);
	}

	public static boolean isAppEngineProduction() {
		return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
	}

	public static boolean isAppengineDevelopment() {
		return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
	}

	public static boolean isAppengine() {
		return isAppEngineProduction() || isAppengineDevelopment();
	}
}
