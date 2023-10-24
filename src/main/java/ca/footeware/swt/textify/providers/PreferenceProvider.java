/**
 *
 */
package ca.footeware.swt.textify.providers;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Display;

import ca.footeware.swt.textify.Constants;
import ca.footeware.swt.textify.preferences.CursorLInePainterPreferencePage;
import ca.footeware.swt.textify.preferences.FontPreferencePage;
import ca.footeware.swt.textify.preferences.LineNumberPreferencePage;
import ca.footeware.swt.textify.preferences.WrapPreferencePage;

/**
 * Provides...let me check...preferences!
 */
public class PreferenceProvider {

	private static final Logger LOGGER = LogManager.getLogger(PreferenceProvider.class);
	private boolean configured = false;
	private PreferenceManager preferenceManager;
	private PreferenceStore preferenceStore;

	/**
	 * Configure the manager with all the preference nodes, one per preference
	 * property.
	 */
	private void addPreferenceNodes() {
		PreferenceNode currentLineBackgroundNode = new PreferenceNode(Constants.CURSOR_LINE_PAINTER_PROPERTY_NAME,
				"Current Line", null, CursorLInePainterPreferencePage.class.getName());
		preferenceManager.addToRoot(currentLineBackgroundNode);

		PreferenceNode fontNode = new PreferenceNode(Constants.FONT_PROPERTY_NAME, "Font", null,
				FontPreferencePage.class.getName());
		preferenceManager.addToRoot(fontNode);

		PreferenceNode lineNumberNode = new PreferenceNode(Constants.LINE_NUMBER_PROPERTY_NAME, "Line Numbers", null,
				LineNumberPreferencePage.class.getName());
		preferenceManager.addToRoot(lineNumberNode);

		PreferenceNode wrapNode = new PreferenceNode(Constants.WRAP_PROPERTY_NAME, "Wrap", null,
				WrapPreferencePage.class.getName());
		preferenceManager.addToRoot(wrapNode);
	}

	/**
	 * Get the configured preference manager.
	 *
	 * @return {@link PreferenceManager}
	 */
	public PreferenceManager getPreferenceManager() {
		init();
		return preferenceManager;
	}

	/**
	 * Get the configured preference store.
	 *
	 * @return {@link PreferenceStore}
	 */
	public PreferenceStore getPreferenceStore() {
		init();
		return preferenceStore;
	}

	/**
	 * Specifies where the preference properties are stored.
	 *
	 * @return {@link String}
	 */
	private String getStorePath() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(System.getProperty("user.home"));
		stringBuilder.append(File.separator);
		stringBuilder.append(".local");
		stringBuilder.append(File.separator);
		stringBuilder.append("share");
		stringBuilder.append(File.separator);
		stringBuilder.append(Constants.APP_NAME);
		stringBuilder.append(File.separator);
		stringBuilder.append("textify.properties");
		return stringBuilder.toString();
	}

	/**
	 * Configure the manager and store.
	 */
	private void init() {
		if (configured) {
			return;
		}
		preferenceManager = new PreferenceManager();
		addPreferenceNodes();
		preferenceStore = new PreferenceStore(getStorePath());
		setDefaults();
		// load prefs from file
		try {
			preferenceStore.load();
			configured = true;
		} catch (IOException e) {
			LOGGER.log(Level.INFO, "Preferences did not load. It is likely preferences are yet to be saved.", e);
		}
	}

	/**
	 * Set the default preference values.
	 */
	private void setDefaults() {
		preferenceStore.setDefault(Constants.CURSOR_LINE_PAINTER_PROPERTY_NAME, true);
		preferenceStore.setDefault(Constants.HIGHLIGHT_PROPERTY_NAME, false);
		preferenceStore.setDefault(Constants.FONT_PROPERTY_NAME,
				Display.getDefault().getSystemFont().getFontData()[0].toString());
		preferenceStore.setDefault(Constants.LINE_NUMBER_PROPERTY_NAME, true);
		preferenceStore.setDefault(Constants.WRAP_PROPERTY_NAME, true);
	}
}
