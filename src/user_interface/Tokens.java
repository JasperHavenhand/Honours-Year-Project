package user_interface;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.flink.api.java.tuple.Tuple2;

import utilities.Configuration;
import utilities.Log;

/**
 * 
 * @author Jasper Havenhand
 *
 */
final class Tokens {
	
	private static Tokens instance = null;
	private static final String TOKEN_FILE_NAME = Configuration.getInstance().getProperty("tokensFile");
	private static Properties properties;
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "general_log";
	
	/**
	 * Lazy initialisation of Tokens.
	 * @return instance of Tokens.
	 */
	public static synchronized Tokens getInstance() {
		if (instance == null) {
			instance = new Tokens();
		}
		return (instance);
	}
	
	private Tokens() {
		loadTokens();
	}
	
	private void loadTokens() {
		properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(TOKEN_FILE_NAME);
			properties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			// Do nothing.
		} catch (IOException e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Retrieves the token with the specified name.
	 * @param name The name of the token to fetch.
	 * @return The transfer probability of the requested property or null if it doesn't exist.
	 */
	Double get(String name) {
		try {
			return Double.parseDouble(properties.getProperty(name));
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @return A list of all the token names paired with their transfer probabilities.
	 */
	List<Tuple2<String,Double>> getAll() {
		try {
			List<Tuple2<String,Double>> list = new ArrayList<Tuple2<String,Double>>();
			Set<Entry<Object, Object>> entries = properties.entrySet();
			for (Entry<Object, Object> entry: entries) {
				list.add(Tuple2.of((String)entry.getKey(), Double.parseDouble((String)entry.getValue())));
			}
			return list;
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates or updates the specified token with the specified transfer probability.
	 * @param name The name of the token.
	 * @param value The transfer probability corresponding to the name.
	 */
	void set(String name, Double transferProb) {
		try {
			properties.setProperty(name, Double.toString(transferProb));
			saveTokens();
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			// Should name,transferProb be removed from properties if it can't be stored, to maintain atomicity?
			e.printStackTrace();
		}
	}
	
	/** Writes the current version of the properties to the tokens properties file. */
	private void saveTokens() throws Exception {
		FileOutputStream out = new FileOutputStream(TOKEN_FILE_NAME);
		properties.store(out, null);
	}
	
}

