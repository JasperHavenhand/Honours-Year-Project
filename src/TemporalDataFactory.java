import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.csv.CSVDataSource;

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
	public DataSource createCSVDataSource(String inputURI, String inputType, String sourceName) {
		
		TemporalDataSource temporalData = null;
		switch(inputType.toLowerCase()) {
			case "nsense":
				temporalData = new NSenseDataSource(inputURI);
				break;
				
			default:
				// error: input type not found.
				break;
		}
		
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
	
	/**
	 * Used to create the CSV files necessary for a CSVDataSource.
	 * @param parentURI
	 * @param fileName
	 * @param data
	 */
	private void createCSVFile(URI parentURI, String fileName, String[] data) {
			
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
