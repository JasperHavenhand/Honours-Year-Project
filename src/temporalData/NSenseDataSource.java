package temporalData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author Jasper Havenhand
 *
 */
final class NSenseDataSource extends TemporalDataSource {
	
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
	/** according to the NSense ReadMe file, "The experiment 
	 *  was conducted for the period of 12 days from 
	 *  12th September to 23rd September 2016".
	 *  This is not provided in the NSense timestamps 
	 *  but is needed for the Gradoop timestamps.*/
	private static final int DATA_YEAR = 2016; 
	
	NSenseDataSource(String inputURI) {
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
	
	ArrayList<String> getGraphs() {
		return graphs;
	}

	ArrayList<String> getVertices() {
		return vertices;
	}

	ArrayList<String> getEdges() {
		return edges;
	}

	ArrayList<String> getMetadata() {
		return metaData;
	}

	private void setGraphs() {
		graphs = new ArrayList<String>();
		String graphsEntry = GRAPH_ID + ";" + GRAPHS_LABEL 
				+ ";" +(new File(inputURI)).getName();
		graphs.add(graphsEntry);
	}

	private void setVertices() {
		vertices = new ArrayList<String>();
		vertexIDs = new HashMap<String, String>();
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
		edges = new ArrayList<String>();
		// Iterating through the folders for each vertex (i.e. each recording device).
		for (File vertexFolder: inputContent) {
			if (vertexFolder.isDirectory()) {
				String srcVertex = vertexIDs.get(vertexFolder.getName());
				if (!srcVertex.equals(null)) {
					File[] dataFiles = vertexFolder.listFiles()[0].listFiles();
					// Locating the SocialStrength.csv file for the current vertex.
					// This file records encounters with other recording devices.
					// The data in this file will be used to create the edges.
					for (File file: dataFiles) {
						if (file.getName().toLowerCase().equals("socialstrength")) {
							try {
								BufferedReader br = new BufferedReader(new FileReader(file));
								String line = br.readLine();
								int edgeID = 0x0;
								// Reading the CSV file, line by line.
								while (line != null) {
									String[] attributes = line.split("\\s+");
									if (attributes.length == 6) {
										// Checking that this recorded encounter has a non-zero duration.
										if (!attributes[2].equals("0.0")) {
											// Checking that the other device on this edge has a hexadecimal ID.
											String tgtVertex = vertexIDs.get(attributes[1]);
											if (!tgtVertex.equals(null)) {
												String edgeIDHex = Integer.toHexString(edgeID);
												
												/* NSense timestamps are in the format of:
												   dd/mm-HH:mm:ss.SSS 
												   This splits it on any non-digit characters
												   and converts the strings to integers.*/
												String[] timeStrings = attributes[0].split("\\D");
												int[] timeInts = new int[timeStrings.length];
												for (int i = 0; i < timeStrings.length; i++) {
													timeInts[i] = Integer.parseInt(timeStrings[i]);
												}

												String timeLabel = Long.toString(LocalDateTime.of(DATA_YEAR,timeInts[1],timeInts[0],timeInts[2],
																		timeInts[3],timeInts[4],timeInts[5])
																		.toInstant(ZoneOffset.UTC).toEpochMilli());
												
												String edgeEntry = edgeIDHex + ";[" + GRAPH_ID + "];" 
														+ srcVertex + ";" + tgtVertex + ";" + EDGES_LABEL
														+ ";;(" + timeLabel + "," + timeLabel + "),(" 
														+ timeLabel + "," + timeLabel + ")";
												
												edges.add(edgeEntry);
												
												edgeID++;
											} else {
												//log error about unknown vertex.
											}
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
				} else {
					//log error about unknown vertex.
				}
			}
		}
		
	}

	private void setMetadata() {
		metaData = new ArrayList<String>();
		String nameProp = "name:string";
		metaData.add("g;"+GRAPHS_LABEL+";"+nameProp);
		metaData.add("v;"+VERTICES_LABEL+";"+nameProp);
	}

}
