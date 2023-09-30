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
			String folderLocation = "/home/" + System.getProperty("user.name") + "/.local/share/textify/";
			File folder = new File(folderLocation);
			if (!folder.exists()) {
				folder.mkdir();
			}
			this.prefsPath = folderLocation + "textify.properties";
			File file = new File(prefsPath);
			if (!file.exists()) {
				file.createNewFile();
			} else {
				this.props.load(new FileInputStream(file));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			this.props.store(new FileWriter(prefsPath), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setProperty(String key, String value) {
		this.props.setProperty(key, value);
	}
}
