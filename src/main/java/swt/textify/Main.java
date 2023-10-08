/**
 *
 */
package swt.textify;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
public class Main {

	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	/**
	 * @param args {@link String}[]
	 */
	public static void main(String[] args) {
		LOGGER.log(Level.DEBUG, "Starting Textify...");
		new Textify(args);
	}
}
