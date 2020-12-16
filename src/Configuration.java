
public final class Configuration {
	
	private static Configuration instance = null;
	
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
		
	}
	
	/**
	 * Retrieves the property with the specified name.
	 * @param name The name of the property to fetch.
	 * @return The value of the requested property or null if it doesn't exist.
	 */
	public String getProperty(String name) {
		
		return null;
	}

	public Boolean setProperty(String name, String value) {
		
		return true;
	}
}
