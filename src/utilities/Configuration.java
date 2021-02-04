package utilities;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author Jasper Havenhand
 *
 */
public final class Configuration {
	
	private static Configuration instance = null;
	private static final String CONFIG_FILE_NAME = "config.properties";
	private static Properties properties;
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "General_log";
	
	/**
	 * Lazy initialisation of Configuration.
	 * @return instance of Configuration.
	 */
	public static synchronized Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
		}
		return (instance);
	}
	
	private Configuration() {
		loadConfiguration();
	}
	
	private void loadConfiguration() {
		properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(CONFIG_FILE_NAME);
			properties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			setDefaultConfiguration();
			e.printStackTrace();
		} catch (IOException e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This is used to set and save the default configuration properties
	 * if the configuration file is not found.
	 */
	private void setDefaultConfiguration() {
		try {
			properties.setProperty("dataFolder", System.getProperty("user.dir")+"\\data");
			properties.setProperty("logsFolder", System.getProperty("user.dir")+"\\logs");
			saveConfiguration();
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			// Should key,value pairs be removed from properties if it can't be stored, to maintain atomicity?
		}	
	}
	
	/**
	 * Retrieves the configuration property with the specified name.
	 * @param name The name of the property to fetch.
	 * @return The value of the requested property or null if it doesn't exist.
	 */
	public String getProperty(String name) {
		return properties.getProperty(name);
	}

	/**
	 * Creates or updates the specified configuration property with the specified value.
	 * @param name The name of the configuration property.
	 * @param value The value corresponding to the name.
	 */
	public void setProperty(String name, String value) {
		try {
			properties.setProperty(name, value);
			saveConfiguration();
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			// Should name,value be removed from properties if it can't be stored, to maintain atomicity?
			e.printStackTrace();
		}
	}
	
	/** Writes the current version of the properties to the configuration properties file. */
	private void saveConfiguration() throws Exception {
		FileOutputStream out = new FileOutputStream(CONFIG_FILE_NAME);
		properties.store(out, null);
	}
	
}
