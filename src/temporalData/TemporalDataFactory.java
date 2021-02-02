package temporalData;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.csv.CSVDataSource;

import utilities.Configuration;

/**
 * The façade class for the temporalData package.
 * @author Jasper Havenhand
 *
 */
public final class TemporalDataFactory {
	
	public static enum inputType {NSENSE};
	
	/**
	 * Returns a new Gradoop DataSource created from the given data.
	 * @param inputURI The root directory of the files containing the source data.
	 * @param inputType The format of the source data.
	 * @param sourceName The name to be given to the folder that will be created for the new DataSource.
	 * @return DataSource
	 */
	public static DataSource createCSVDataSource(String inputURI, inputType inputType, String sourceName) {
		
		TemporalDataSource temporalData = null;
		switch(inputType) {
			case NSENSE:
				temporalData = new NSenseDataSource(inputURI);
				break;
				
			default:
				// error: input type not found.
				break;
		}
		
		if (temporalData != null) {
			// Creating the folder for the CSV files that will be used to create the CSV data source.
			URI dataFolderURI = URI.create(Configuration.getInstance().getProperty("dataFolder")).resolve(sourceName);
			(new File(dataFolderURI)).mkdirs();
			
			createCSVFile(dataFolderURI, "graphs", temporalData.getGraphs());
			createCSVFile(dataFolderURI, "vertices", temporalData.getVertices());
			createCSVFile(dataFolderURI, "edges", temporalData.getEdges());
			createCSVFile(dataFolderURI, "metadata", temporalData.getMetadata());
	
			DataSource CSVDataSource = new CSVDataSource(dataFolderURI.toString(), null);
			
			return CSVDataSource;
		}
		
		return null;
	}
	
	/**
	 * Used to create the CSV files necessary for a CSVDataSource.
	 * @param parentURI The directory to create the CSV file within.
	 * @param fileName The name that will be given to the CSV file.
	 * @param data The data to be inserted into the CSV file.
	 */
	private static void createCSVFile(URI parentURI, String fileName, ArrayList<String> data) {
			
			try {
				File file = new File(parentURI.resolve(fileName+".csv"));
				file.mkdir();
				FileWriter writer;
				writer = new FileWriter(file);
				
				for (String line: data) {
					writer.append(line+"\n");
				}
				
				writer.flush();
				writer.close();
				
			} catch (IOException e) {
				// log error
				e.printStackTrace();
			}
		
	}
	
}
