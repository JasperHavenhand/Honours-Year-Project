package temporalData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author Jasper Havenhand
 *
 */
public final class NSenseDataSource extends TemporalDataSource {
	
	private String inputURI;
	private File[] inputContent;
	private ArrayList<String> graphs, vertices, edges, metaData;
	private HashMap<String, String> vertexIDs;
	/** This constant is created, based on the assumption that there will
	    be one graph per data source. */
	private static final String GRAPH_ID = "000000000000000000000000";
	/*  The following labels will be used to associate the graph, vertices
	    and labels with the descriptions of their properties that will be
	    given in the metadata.csv file. */
	private static final String GRAPHS_LABEL = "G";
	private static final String VERTICES_LABEL = "V";
	private static final String EDGES_LABEL = "E";
	
	public NSenseDataSource(String inputURI) {
		this.inputURI = inputURI;
		inputContent = (new File(inputURI)).listFiles();
		if (inputContent != null) {
			extractData();
		} else {
			//error
		}
	}
	
	private void extractData() {
		setGraphs();
		setVertices();
		setEdges();
		setMetadata();
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
		String graphsEntry = GRAPH_ID + ";" + GRAPHS_LABEL 
				+ ";" +(new File(inputURI)).getName();
		graphs.add(graphsEntry);
	}

	private void setVertices() {
		int vertexID = 0x0;
		String vertexIDHex, vertexName, vertexEntry;
		// Iterating through the folders for each vertex (i.e. each recording device).
		for (File vertexFolder: inputContent) {
			if (vertexFolder.isDirectory()) {
				/* Adds the current vertex to the vertices array,
				   in the expected format for the Gradoop vertices.csv file.*/
				
				// Creating the vertex ID in the form of a 12 bytes hexadecimal string.
				vertexIDHex = Integer.toHexString(vertexID);
				vertexIDHex = "0".repeat(12-vertexIDHex.length()) + vertexIDHex;
				
				vertexName = vertexFolder.getName();
				
				vertexEntry = vertexIDHex + ";[" + GRAPH_ID + "];" + VERTICES_LABEL + ";" + vertexName;

				vertices.add(vertexEntry);
				// Mapping the vector's name to its Hexadecimal ID.
				// The vertexIDs HashMap will be needed when creating
				// the edges.csv file.
				vertexIDs.put(vertexName,vertexIDHex);
				
				vertexID++;
			}
		}
	}

	private void setEdges() {

		File[] dataFiles;
		int edgeID = 0x0;
		String edgeIDhex;
		// Iterating through the folders for each vertex (i.e. each recording device).
		for (File vertexFolder: inputContent) {
			if (vertexFolder.isDirectory()) {
				dataFiles = vertexFolder.listFiles();
				// Locating the SocialStrength.csv file for the current vertex.
				// This file records encounters with other recording devices.
				// The data in this file will be used to create the edges.
				for (File file: dataFiles) {
					if (file.getName().toLowerCase().equals("socialstrength")) {
						try {
							BufferedReader br = new BufferedReader(new FileReader(file));
							String line = br.readLine();
							String[] attributes;
							// Reading the CSV file, line by line.
							while (line != null) {
								attributes = line.split("\\s+");
								if (attributes.length == 6) {
									// Checking that this recorded encounter has a non-zero duration.
									if (!attributes[2].equals("0.0")) {
										
										edgeID++;
									}
								} else {
									//log error about incorrect number of columns in SocialStrength entry.
								}
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

	private void setMetadata() {
		String metaEntry = "name:string";
		metaData.add("g;"+GRAPHS_LABEL+";"+metaEntry);
		metaData.add("v;"+VERTICES_LABEL+";"+metaEntry);
		metaData.add("e;"+EDGES_LABEL+";"+metaEntry);
	}

}
