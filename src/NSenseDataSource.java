import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * @author Jasper Havenhand
 *
 */
public final class NSenseDataSource extends TemporalDataSource {
	
	private String rootURI;
	private ArrayList<String> graphs, vertices, edges, metaData;
	
	public NSenseDataSource(String inputURI) {
		rootURI = inputURI;
		extractData();
	}
	
	private void extractData() {
			
			File rootFolder = new File(rootURI);
			String[] paths = rootFolder.list();
			
			if (paths != null) { 
				// Iterating through the folders for each vertex (i.e. each recording device).
				for (String path: paths) {
					File vertexFolder = new File(path);
					if (vertexFolder.isDirectory()) {
						
						// Adds the current vertex to the array,
						// using the format expected the Gradoop vertices.csv format.
						//vertices.add(vertexFolder.getName());
						
						// Locating the SocialStrength.csv file for the current vertex.
						String [] dataFiles = vertexFolder.list();
						for (String fileName: dataFiles) {
							if (fileName.toLowerCase().equals("socialstrength")) {
								// Reading the csv file, line by line.
								try {
									BufferedReader br = new BufferedReader(new FileReader(fileName));
									String line = br.readLine();
									while (line != null) {
										
										line = br.readLine();
									}
									br.close();
								} catch (FileNotFoundException e) {
									// log error
									e.printStackTrace();
								} catch (IOException e) {
									// log error
									e.printStackTrace();
								}
								break;
								
							}
						}
						
					}
				}
			}
			
	}
	
	public ArrayList<String> getGraphs() {
		return graphs;
	}

	public ArrayList<String> getVertices() {
		return vertices;
	}

	public ArrayList<String> getEdges() {
		return edges;
	}

	public ArrayList<String> getMetadata() {
		return metaData;
	}

	private void setGraphs() {

	}

	private void setVertices() {
		
	}

	private void setEdges() {
	
	}

	private void setMetadata() {
	
	}

}
