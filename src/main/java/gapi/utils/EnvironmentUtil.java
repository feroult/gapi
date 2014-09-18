package gapi.utils;

import com.google.appengine.api.utils.SystemProperty;

public class EnvironmentUtil {
	public static boolean isProduction() {
		return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
	}
	
	public static boolean isDevelopment() {
		return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
	}
	
	public static boolean isGae() {
		return isProduction() || isDevelopment();
	}
}
