/**
 *
 */
package swt.textify.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
public class PreferenceProvider {

	private static final Logger LOGGER = LogManager.getLogger(PreferenceProvider.class);
	private Properties props;
	private String prefsPath;

	/**
	 * Constructor.
	 */
	public PreferenceProvider() {
		this.props = new Properties();
		init();
	}

	public String getProperty(String key, String defaultValue) {
		return this.props.getProperty(key, defaultValue);
	}

	private void init() {
		try {
			final String name = System.getenv().get("SUDO_USER") != null ? System.getenv().get("SUDO_USER")
					: System.getProperty("user.name");

			final String folderLocation = "/home/" + name + "/.local/share/textify/";
			final File folder = new File(folderLocation);
			if (!folder.exists()) {
				folder.mkdir();
			}
			prefsPath = folderLocation + "textify.properties";
			final File file = new File(prefsPath);
			if (!file.exists()) {
				boolean propsCreated = file.createNewFile();
				if (!propsCreated) {
					throw new IllegalArgumentException("An error occurred creating file.");
				}
			} else {
				try (InputStream in = new FileInputStream(file)) {
					this.props.load(in);
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.ERROR, "Preferences are disabled ({0}).", prefsPath, e);
		}
	}

	public void save() {
		try (Writer writer = new FileWriter(prefsPath)) {
			this.props.store(writer, null);
		} catch (IOException e) {
			LOGGER.log(Level.ERROR, "Error saving preferences.", e);
		}
	}

	public void setProperty(String key, String value) {
		this.props.setProperty(key, value);
	}
}
