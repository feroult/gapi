package com.github.feroult.gapi.utils;

import com.google.appengine.api.utils.SystemProperty;

import java.io.File;
import java.net.URL;

public class Setup {

	private static final String GAPI_SERVICE_ACCOUNT_EMAIL = "gapi.service.account.email";
	private static final String GAPI_SERVICE_ACCOUNT_KEY = "gapi.service.account.key";
	private static final String GAPI_SERVICE_ACCOUNT_USER = "gapi.service.account.user";
	private static final String GAPI_USE_APPENGINE_CREDENTIALS = "gapi.use.appengine.credentials";

	private static final String GAPI_SERVICE_ACCOUNT_EMAIL_ENV = "GAPI_SERVICE_ACCOUNT_EMAIL";
	private static final String GAPI_SERVICE_ACCOUNT_KEY_ENV = "GAPI_SERVICE_ACCOUNT_KEY";
	private static final String GAPI_SERVICE_ACCOUNT_USER_ENV = "GAPI_SERVICE_ACCOUNT_USER";
	private static final String GAPI_USE_APPENGINE_CREDENTIALS_ENV = "GAPI_USE_APPENGINE_CREDENTIALS";

	public static String getServiceAccountEmail() {
		return getProperty(GAPI_SERVICE_ACCOUNT_EMAIL, GAPI_SERVICE_ACCOUNT_EMAIL_ENV);
	}

	public static String getServiceAccountUser() {
		return getProperty(GAPI_SERVICE_ACCOUNT_USER, GAPI_SERVICE_ACCOUNT_USER_ENV);
	}

	private static String getServiceAccountKey() {
		return getProperty(GAPI_SERVICE_ACCOUNT_KEY, GAPI_SERVICE_ACCOUNT_KEY_ENV);
	}

	private static String getUseAppengineCredentials() {
		return getProperty(GAPI_USE_APPENGINE_CREDENTIALS, GAPI_USE_APPENGINE_CREDENTIALS_ENV);
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

	public static boolean isUsingAppengineCredentials() {
		String use = getUseAppengineCredentials();
		return isAppEngineProduction() && "true".equalsIgnoreCase(use);
	}
}
