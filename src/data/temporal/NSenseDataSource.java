package data.temporal;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;

import utilities.Log;

/**
 * 
 * @author Jasper Havenhand
 *
 */
final class NSenseDataSource extends TemporalDataSource {
	
	private String inputPath;
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
	/** The vertices and graphs need to be defined with time intervals, as well as the edges.
	 *  Since they don't require temporal properties for this task, they are 
	 *  given the interval 01/01/1970-00:00:00 to 31/12/9999-23:59:59 for both.*/
	private static final String DEFAULT_TIME_INTERVALS = "(0,253402300799000),(0,253402300799000)";
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "data_sources_log";
	
	NSenseDataSource(String inputPath) throws NullPointerException {
		if (inputPath == null) {
			throw new NullPointerException("The input path is required.");
		}
		try {
			this.inputPath = inputPath;
			inputContent = (new File(inputPath)).listFiles();
			if (inputContent != null) {
				extractData();
			} else {
				Log.getLog(LOG_NAME).writeError("No NSense files could not be found at: "+inputPath);
			}
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
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
		
		// graph-id;label;value_1|value_2|...|value_n;(tx-from,tx-to),(val-from,val-to)
		String graphsEntry = GRAPH_ID + ";" + GRAPHS_LABEL 
				+ ";" +(new File(inputPath)).getName()
				+ ";" + DEFAULT_TIME_INTERVALS;
		
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
				vertexIDHex = "0".repeat(24-vertexIDHex.length()) + vertexIDHex;
				
				vertexName = vertexFolder.getName();
				
				// vertex-id;[graph-ids];label;name|infected;(tx-from,tx-to),(val-from,val-to)
				vertexEntry = vertexIDHex + ";[" + GRAPH_ID + "];" + VERTICES_LABEL + ";"
								+ vertexName +"|false;" + DEFAULT_TIME_INTERVALS;

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
		int edgeID = 0x0;
		// Iterating through the folders for each vertex (i.e. each recording device).
		for (File vertexFolder: inputContent) {
			if (vertexFolder.isDirectory()) {
				String srcVertex = vertexIDs.get(vertexFolder.getName());
				if (srcVertex != null) {
					File[] dataFiles = vertexFolder.listFiles()[0].listFiles();
					// Locating the SocialStrength.csv file for the current vertex.
					// This file records encounters with other recording devices.
					// The data in this file will be used to create the edges.
					for (File file: dataFiles) {
						if (file.getName().toLowerCase().contains("socialstrength")) {
							try {
								BufferedReader br = new BufferedReader(new FileReader(file));
								String line = br.readLine();
								// Reading the CSV file, line by line.
								while (line != null) {
									String[] attributes = line.trim().split("\\s+");
									if (attributes.length == 6) {
										// Checking that this recorded encounter has a non-zero duration.
										if (!attributes[2].equals("0.0")) {
											// Checking that the other device on this edge has a hexadecimal ID.
											String tgtVertex = vertexIDs.get(attributes[1]);
											if (tgtVertex != null) {
												String edgeIDHex = Integer.toHexString(edgeID);
												edgeIDHex = "0".repeat(24-edgeIDHex.length()) + edgeIDHex;
												
												/* NSense timestamps are in the format of:
												   dd/mm-HH:mm:ss.SSS 
												   This splits it on any non-digit characters
												   and converts the strings to integers.*/
												String[] timeStrings = attributes[0].split("\\D");
												int[] timeInts = new int[timeStrings.length];
												for (int i = 0; i < timeStrings.length-1; i++) {
													timeInts[i] = Integer.parseInt(timeStrings[i]);
												}
												// Converting the fraction of a second to nanoseconds.
												int nanos = (int) (Double.parseDouble("0."+timeStrings[5])* Double.valueOf(1e+9));
												
												LocalDateTime fromTime = LocalDateTime.of(DATA_YEAR,timeInts[1],
														timeInts[0],timeInts[2],
														timeInts[3],timeInts[4],nanos);
												
												// Calculating the interaction end time by adding the duration (in nanoseconds) to the timestamp.
												long duration = Math.round(Double.parseDouble(attributes[2]) * Double.valueOf(1e+9));
												LocalDateTime toTime = fromTime.plusNanos(duration);
												
												String fromTimeStr = Long.toString(fromTime.toInstant(ZoneOffset.UTC).toEpochMilli());
												
												String toTimeStr = Long.toString(toTime.toInstant(ZoneOffset.UTC).toEpochMilli());;
												
												String edgeEntry = edgeIDHex + ";[" + GRAPH_ID + "];" 
														+ srcVertex + ";" + tgtVertex + ";" + EDGES_LABEL
														+ ";;(" + fromTimeStr + "," + toTimeStr + "),(" 
														+ fromTimeStr + "," + toTimeStr + ")";

												edges.add(edgeEntry);
												
												edgeID++;
											} else {
												Log.getLog(LOG_NAME).writeWarning(
														"Unknown target vertex \"" + attributes[1] + "\" in NSense file " + 
														inputPath + File.separator + vertexFolder.getName() + File.separator +
														"SocialStrength.data");
											}
										}
									} else {
										Log.getLog(LOG_NAME).writeWarning(
												"Incorrect number of columns on line \"" + line + "\" in NSense file " +
												inputPath + File.separator + vertexFolder.getName() + File.separator +
												"SocialStrength.data");
									}
									line = br.readLine();
								}
								br.close();
							} catch (Exception e) {
								Log.getLog(LOG_NAME).writeException(e);
								e.printStackTrace();
							}
							break;	
						}
					}
				} else {
					Log.getLog(LOG_NAME).writeWarning(
							"Unknown NSense source vertex: " + 
							vertexFolder.getName());
				}
			}
		}
		
	}

	private void setMetadata() {
		metaData = new ArrayList<String>();
		String nameProp = "name:string";
		String infectedProp = "infected:boolean";
		metaData.add("g;"+GRAPHS_LABEL+";"+nameProp);
		metaData.add("v;"+VERTICES_LABEL+";"+nameProp+","+infectedProp);
	}

}
