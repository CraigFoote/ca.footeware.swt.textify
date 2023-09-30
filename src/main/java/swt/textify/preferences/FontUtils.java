/**
 *
 */
package swt.textify.preferences;

import org.eclipse.swt.graphics.FontData;

/**
 *
 */
public class FontUtils {

	public static String getDisplayText(FontData fontData) {
		return fontData.getName() + " " + fontData.getHeight() + " " + getStyleName(fontData.getStyle());
	}

	public static FontData getFontData(String fontProperty) {
		String[] split = fontProperty.split("\\|");
		String name = split[1];
		int height = (int) Double.parseDouble(split[2]);
		int style = Integer.parseInt(split[3]);
		return new FontData(name, height, style);
	}

	public static String getStyleName(int style) {
		switch (style) {
		case 0:
			return "Normal";
		case 1:
			return "Bold";
		case 2:
			return "Italic";
		case 3:
			return "Bold Italic";
		case 32:
			return "Oblique";
		case 33:
			return "Bold Oblique";
		default:
			return "Unknown";
		}
	}
}
