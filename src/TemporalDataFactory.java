import java.io.File;

import org.gradoop.flink.io.api.DataSource;

/**
 * 
 * @author Jasper Havenhand
 *
 */
public class TemporalDataFactory {
	
	/**
	 * 
	 * @param inputURI
	 * @param inputType
	 * @param sourceName
	 * @return org.gradoop.flink.io.api.DataSource
	 */
	public static DataSource createCSVDataSource(String inputURI, String inputType, String sourceName) {
		
		DataSource dataSource = null;
		File DataFolder = null;
		
		switch(inputType.toLowerCase()) {
			case "nsense":
				
				break;
				
			default:
				
				break;
		}
		
		return dataSource;
		
	}
	
}
