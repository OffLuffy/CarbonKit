package net.teamcarbon.carbonkit.utils;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class TypeUtils {
	/**
	 * Check if a number is a valid Integer
	 * @param query The query to check
	 * @return true if the query is capable of being casted to an int, false otherwise
	 */
	public static boolean isInteger(String query) {
		try {
			Integer.parseInt(query);
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	/**
	 * Check if a number is a valid Long
	 * @param query The query to check
	 * @return true if the query is capable of being casted to a long, false otherwise
	 */
	public static boolean isLong(String query) {
		try {
			Long.parseLong(query);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Check if a number is a valid Double
	 * @param query The query to check
	 * @return true if the query is capable of being casted to a double, false otherwise
	 */
	public static boolean isDouble(String query) {
		try {
			Double.parseDouble(query);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Check if a number is a valid Float
	 * @param query The query to check
	 * @return true if the query is capable of being casted to a float, false otherwise
	 */
	public static boolean isFloat(String query) {
		try {
			Float.parseFloat(query);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Check if a number is a valid Boolean
	 * @param query The query to check
	 * @return true if the query is capable of being casted to a boolean, false otherwise
	 */
	public static boolean isBoolean(String query) {
		return (isInteger(query) && Integer.parseInt(query) > 0) ||
				MiscUtils.eq(query, "on", "off", "1", "0", "true", "false", "enabled", "disabled", "enable", "disable",
						"allow", "deny", "yes", "no", "y", "n", "agree", "disagree");
	}

	/**
	 * Parses a boolean more generously than Java's Boolean.toBoolean() method
	 * @param query The query to check
	 * @return true if the query is parsed as true, false otherwise or if un-parsable
	 */
	public static boolean toBoolean(String query) {
		return isInteger(query) && Integer.parseInt(query) > 0 ||
				isBoolean(query) && MiscUtils.eq(query, "on", "1", "true", "enabled", "enable", "allow", "yes", "y", "agree");
	}
}
