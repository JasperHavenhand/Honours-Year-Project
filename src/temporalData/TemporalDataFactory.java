package temporalData;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.temporal.io.impl.csv.TemporalCSVDataSource;
import org.gradoop.temporal.util.TemporalGradoopConfig;

import utilities.Configuration;
import utilities.Log;

/**
 * The façade class for the temporalData package.
 * @author Jasper Havenhand
 *
 */
public final class TemporalDataFactory {
	
	public static enum inputType {NSENSE};
	
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "Data_Sources_log";
	
	/**
	 * Returns a new Gradoop TemporalCSVDataSource created from the given data.
	 * @param inputPath The root directory for the files containing the source data.
	 * @param inputType The format of the source data.
	 * @param sourceName The name to be given to the folder that will be created for the new DataSource.
	 * @return TemporalCSVDataSource
	 */
	public static TemporalCSVDataSource createCSVDataSource(String inputPath, inputType inputType, String sourceName) {
		
		TemporalDataSource temporalData = null;
		switch(inputType) {
			case NSENSE:
				temporalData = new NSenseDataSource(inputPath);
				break;
				
			default:
				Log.getLog(LOG_NAME).writeError("Data input type not known:" +inputType);
				break;
		}
		
		if (temporalData != null) {
			// Creating the folder for the CSV files that will be used for the CSVDataSource.
			String dataFolder = Configuration.getInstance().getProperty("dataFolder") + File.separator + sourceName;
			(new File(dataFolder)).mkdirs();
			
			createCSVFile(dataFolder, "graphs", temporalData.getGraphs());
			createCSVFile(dataFolder, "vertices", temporalData.getVertices());
			createCSVFile(dataFolder, "edges", temporalData.getEdges());
			createCSVFile(dataFolder, "metadata", temporalData.getMetadata());
	
			ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
			TemporalGradoopConfig config = TemporalGradoopConfig.createConfig(env);

			TemporalCSVDataSource CSVDataSource = new TemporalCSVDataSource(dataFolder, config);
			return CSVDataSource;
		}
		
		return null;
	}
	
	/**
	 * Used to create the CSV files necessary for a CSVDataSource.
	 * @param parentPath The directory to create the CSV file within.
	 * @param fileName The name that will be given to the CSV file.
	 * @param data The data to be inserted into the CSV file.
	 */
	private static void createCSVFile(String parentPath, String fileName, ArrayList<String> data) {
			
			try {
				File file = new File(parentPath + File.separator + fileName+".csv");
				FileWriter writer = new FileWriter(file);
				
				for (String line: data) {
					writer.append(line+"\n");
				}
				
				writer.flush();
				writer.close();
				
			} catch (IOException e) {
				Log.getLog(LOG_NAME).writeException(e);
				e.printStackTrace();
			}
		
	}
	
}
