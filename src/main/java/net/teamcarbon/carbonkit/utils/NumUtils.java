package net.teamcarbon.carbonkit.utils;

public class NumUtils {
	/**
	 * Forces a value to be within the min or max value, inclusive.
	 * @param val The value to normalize
	 * @param min The value that val must be greater than or equal to
	 * @param max The value that val must be lesser than or equal to
	 * @return Returns the min if value is less than min, returns the max if value is greather than max, returns val otherwise
	 */
	public static int normalizeInt(int val, int min, int max) {
		if (min > max) {
			int temp = min;
			min = max;
			max = temp;
		}
		return ( (val < min) ? min : ( (val > max) ? max : val ) );
	}

	/**
	 * Forces a value to be within the min or max value, inclusive.
	 * @param val The value to normalize
	 * @param min The value that val must be greater than or equal to
	 * @param max The value that val must be lesser than or equal to
	 * @return Returns the min if value is less than min, returns the max if value is greather than max, returns val otherwise
	 */
	public static double normalizeDouble(double val, double min, double max) {
		if (min > max) {
			double temp = min;
			min = max;
			max = temp;
		}
		return ( ( val < min) ? min : ( (val > max) ? max : val ) );
	}

	/**
	 * Forces a value to be within the min or max value, inclusive.
	 * @param val The value to normalize
	 * @param min The value that val must be greater than or equal to
	 * @param max The value that val must be lesser than or equal to
	 * @return Returns the min if value is less than min, returns the max if value is greather than max, returns val otherwise
	 */
	public static float normalizeFloat(float val, float min, float max) {
		if (min > max) {
			float temp = min;
			min = max;
			max = temp;
		}
		return ( ( val < min) ? min : ( (val > max) ? max : val ) );
	}

	/**
	 * Generates a random number
	 * @param min The lowest number allowed
	 * @param max The highest number allowed
	 * @return Returns a random int between the specified boundaries, inclusive
	 */
	public static int rand(int min, int max) { return min + (int)(Math.random() * ((max - min) + 1)); }

	/**
	 * Remaps a value in a range to an equivilent value in another range.
	 * @param clamp Whether to clamp the return value to the new range
	 * @param oldMin The minimum value of the old range
	 * @param oldMax The maximum value of the old range
	 * @param newMin The minimum value of the new range
	 * @param newMax The maximum value of the new range
	 * @param value The value in the old range to be remapped to the new range
	 * @return Returns a double of the old value's equivilent value in the new range
	 */
	public static double remapValue(boolean clamp, double oldMin, double oldMax, double newMin, double newMax, double value) {
		if (clamp) {
			if (value >= oldMax) return newMax;
			if (value <= oldMin) return newMin;
		}
		return (((newMax - newMin) * (value - oldMin)) / (oldMax - oldMin)) + newMin;
	}

	/**
	 * Remaps a value in a range to an equivilent value in another range, clamping the return value to the new range.
	 * @param oldMin The minimum value of the old range
	 * @param oldMax The maximum value of the old range
	 * @param newMin The minimum value of the new range
	 * @param newMax The maximum value of the new range
	 * @param value The value in the old range to be remapped to the new range
	 * @return Returns a double of the old value's equivilent value in the new range, clamping the return value to the new range.
	 */
	public static double remapValue(double oldMin, double oldMax, double newMin, double newMax, double value) {
		return remapValue(true, oldMin, oldMax, newMin, newMax, value);
	}
}
