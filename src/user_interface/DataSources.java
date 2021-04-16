package user_interface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.java.tuple.Tuple2;

import utilities.Configuration;
import utilities.Log;

/**
 * 
 * @author Jasper Havenhand
 *
 */
final class DataSources {
	
	private static DataSources instance = null;
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "general_log";
	
	/**
	 * Lazy initialisation of DataSources.
	 * @return instance of DataSources.
	 */
	static synchronized DataSources getInstance() {
		if (instance == null) {
			instance = new DataSources();
		}
		return (instance);
	}
	
	private DataSources() {
	}
	
	/**
	 * Retrieves the absolute path of the data source with the specified name.
	 * @param name The name of the data source to fetch.
	 * @return The absolute path of the requested data source or null if it doesn't exist.
	 */
	String get(String name) {
		try {
			File dataFolder = new File(Configuration.getInstance().getProperty("dataFolder"));
			File[] subFolders = dataFolder.listFiles();
			for (File file: subFolders) {
				if (file.getName().equals(name)) {
					return file.getAbsolutePath();
				}
			}
			return null;
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @return A list of all the data source names paired with their absolute paths
	 *  in the format (name,path).
	 */
	List<Tuple2<String,String>> getAll() {
		try {
			List<Tuple2<String,String>> list = new ArrayList<Tuple2<String,String>>();
			File dataFolder = new File(Configuration.getInstance().getProperty("dataFolder"));
			File[] subFolders = dataFolder.listFiles();
			for (File file: subFolders) {
				list.add(Tuple2.of(file.getName(), file.getAbsolutePath()));
			}
			return list;
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}
	
}

