/**
 * 
 */
package ca.footeware.swt.textify.preferences;

import org.eclipse.swt.graphics.RGB;

/**
 * Utility class to deal with {@link RGB} and their hexidecimal representation.
 */
public class ColorUtils {

	/**
	 * Hidden constructor, use static methods.
	 */
	private ColorUtils() {
	}

	/**
	 * Convert an <code>RGB</code> to a hexidecimal string.
	 * 
	 * @param rgb {@link RGB}
	 * @return {@link String}
	 */
	public static String convertToHexCode(RGB rgb) {
		int red = rgb.red;
		int green = rgb.green;
		int blue = rgb.blue;
		return String.format("#%02x%02x%02x", red, green, blue);
	}

	/**
	 * Convert a hexidecimal string to a <code>RGB</code>.
	 * 
	 * @param hexCode {@link String}
	 * @return {@link RGB}
	 */
	public static RGB convertToRGB(String hexCode) {
		int red = Integer.valueOf(hexCode.substring(1, 3), 16);
		int green = Integer.valueOf(hexCode.substring(3, 5), 16);
		int blue = Integer.valueOf(hexCode.substring(5, 7), 16);
		return new RGB(red, green, blue);
	}
}
