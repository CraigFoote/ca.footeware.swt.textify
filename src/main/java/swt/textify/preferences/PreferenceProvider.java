/**
 *
 */
package swt.textify.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public class PreferenceProvider {

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
				file.createNewFile();
			} else {
				this.props.load(new FileInputStream(file));
			}
		} catch (IOException e) {
			System.err.println("Preferences are disabled (" + prefsPath + "): " + e.getMessage());
		}
	}

	public void save() {
		try {
			this.props.store(new FileWriter(prefsPath), null);
		} catch (IOException e) {
			System.err.println("Error saving preferences: " + e.getMessage());
		}
	}

	public void setProperty(String key, String value) {
		this.props.setProperty(key, value);
	}
}
