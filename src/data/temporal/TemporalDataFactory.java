package data.temporal;
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
	private static String LOG_NAME = "data_sources_log";
	
	public static TemporalCSVDataSource loadCSVDataSource(String path) throws NullPointerException, IOException {
		if (path == null) {
			throw new NullPointerException("The path to the CSVDataSource is required.");
		}
		if (path.length() == 0) {
			throw new IOException("The path cannot be blank.");
		}
		// Checking that the given directory contains the four necessary CSV files.
		File dir = new File(path);
		Boolean graphs, vertices, edges, metadata;
		graphs = vertices = edges = metadata = false;
		for (File file: dir.listFiles()) {
			if (file.getName().equals("graphs.csv")) {
				graphs = true;
			}
			if (file.getName().equals("vertices.csv")) {
				vertices = true;
			}
			if (file.getName().equals("edges.csv")) {
				edges = true;
			}
			if (file.getName().equals("metadata.csv")) {
				metadata = true;
			}
		}
		// Throws an IOException if at least one of the CSV files is missing.
		if (!(graphs && vertices && edges && metadata)) {
			String msg = "";
			msg += (graphs) ? "" : "graphs.csv, ";
			msg += (vertices) ? "" : "vertices.csv, ";
			msg += (edges) ? "" : "edges.csv, ";
			msg += (metadata) ? "" : "metadata.csv, ";
			msg = ("Missing files: " + msg);
			msg = msg.substring(0, msg.length()-2);
			throw new IOException(msg);
		}
		
		return getCSVDataSource(path);
	}
	
	/**
	 * Returns a new Gradoop TemporalCSVDataSource created from the given data.
	 * @param inputPath The root directory for the files containing the source data.
	 * @param inputType The format of the source data.
	 * @param sourceName The name to be given to the folder that will be created for the new DataSource.
	 * @return TemporalCSVDataSource
	 */
	public static TemporalCSVDataSource createCSVDataSource(String inputPath, inputType inputType, String sourceName) throws NullPointerException {
		
		if (inputPath == null || inputPath.length() == 0) {
			throw new NullPointerException("The input path is required.");
		} 
		if (inputType == null) {
			throw new NullPointerException("The input type is required.");
		}
		if (sourceName == null || sourceName.length() == 0) {
			throw new NullPointerException("The source name is required.");
		}
		
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
			new File(dataFolder).mkdirs();
			
			createCSVFile(dataFolder, "graphs", temporalData.getGraphs());
			createCSVFile(dataFolder, "vertices", temporalData.getVertices());
			createCSVFile(dataFolder, "edges", temporalData.getEdges());
			createCSVFile(dataFolder, "metadata", temporalData.getMetadata());
	
			return getCSVDataSource(dataFolder);
		}
		
		return null;
	}
	
	private static TemporalCSVDataSource getCSVDataSource(String path) {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		TemporalGradoopConfig config = TemporalGradoopConfig.createConfig(env);

		TemporalCSVDataSource CSVDataSource = new TemporalCSVDataSource(path, config);
		return CSVDataSource;
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
				
			} catch (Exception e) {
				Log.getLog(LOG_NAME).writeException(e);
				e.printStackTrace();
			}
		
	}
	
}
