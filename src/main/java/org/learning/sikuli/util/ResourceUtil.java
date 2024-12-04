package org.learning.sikuli.util;

public class ResourceUtil {

	public static String getPathFor(String file) {
		// Using substring for full path to remove first slash.
		// Sikuli bug on Windows
		return ClassLoader.getSystemResource(file).getPath().toString()
				.substring(1);
	}
	
	public static String path(String file) {
		return getPathFor(file);
	}	
}
