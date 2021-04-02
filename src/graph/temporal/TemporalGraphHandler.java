package graph.temporal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.tuple.Tuple2;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.temporal.model.impl.TemporalGraph;
import org.gradoop.temporal.model.impl.functions.predicates.AsOf;
import org.gradoop.temporal.model.impl.pojo.TemporalVertex;

import utilities.Log;

public final class TemporalGraphHandler {

	private TemporalGraph completeGraph;
	private TemporalGraph currentGraph;
	private static String TOKEN_NAME = "infected";
	/** The probability of a vertex with the token passing it to its neighbours. */
	private double tokenTransferProb;
	private long timeIncrement;
	private long currentTimestamp;
	private long lastTimestamp;
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "graphs_log";
	
	/**
	 * Used to handle the dissemination of the given token over the given temporal graph, timestep by timestep.
	 * @param graph The temporalGraph to be handled.
	 * @param tokenTransferProb The probability of the token being transferred over an active edge (0.0 to 1.0).
	 * @param timeIncrement The time increment to be used between each timestep (in milliseconds). 
	 */
	public TemporalGraphHandler(TemporalGraph graph, double tokenTransferProb, long timeIncrement) {
		try {
			completeGraph = graph;
			this.tokenTransferProb = tokenTransferProb;
			
			currentTimestamp = completeGraph.getEdges().sortPartition("validTime.f0", Order.ASCENDING)
					.setParallelism(1).collect().get(0).getValidFrom();
			lastTimestamp = completeGraph.getEdges().sortPartition("validTime.f1", Order.DESCENDING)
					.setParallelism(1).collect().get(0).getValidTo();
			
			currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	/** @return The complete temporal graph with every edge. */
	public TemporalGraph getCompleteGraph() {
		return completeGraph;
	}
	
	/** @return A snapshot of the complete temporal graph at 
	 * the current timestep of the {@code TemporalGraphHandler}. */
	public TemporalGraph getCurrentGraph() {
		return currentGraph;
	}
	
	/**
	 * @return The temporalities for each edge in the complete graph. 
	 * Each edge is represented by the IDs of the vertices it intercepts with.
	 */
	public List<Triple<String,String,Long>> getTemporalities() {
		return Connectivity.temporalitiesOf(completeGraph);
	}

	/**
	 * @return The Id of each vertex in the graph paired with a list of the Ids of the vertices temporarily reachable from it.
	 */
	public List<Tuple2<String, List<String>>> getReachabilitySets() {
		return Connectivity.reachabilitySetsOf(completeGraph);
	}
	
	/**
	 * Merges all edges active before the specified startTime, so that they are active from startTime to startTime+duration.
	 * @param startTime A timestamp, in epoch milliseconds.
	 * @param duration The duration of the merged edges, in milliseconds.
	 */
	public void mergeEdges(long startTime, long duration) {
		completeGraph = Connectivity.mergeEdges(completeGraph, startTime, duration);
		currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
	}
	
	/**
	 * Delays all edges by the specified amount of time.
	 * @param time The time to delay by, in milliseconds.
	 */
	public void delayEdges(long time) {
		completeGraph = Connectivity.delayEdges(completeGraph, time);
		currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
	}
	
	/**
	 * Set a limit on the temporality, i.e., the number of times an edge between two vertices can be active.
	 * Edges will be randomly dropped to meet this new limit.
	 * @param limit an integer value.
	 */
	public void limitTemporality(int limit) {
		completeGraph = Connectivity.limitTemporality(completeGraph, limit);
		currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
	}
	
	/**
	 * Deletes the edge between the specified pair of vertices.
	 * @param graph The graph containing the pair of vertices.
	 * @param vertex1 The id of the first vertex.
	 * @param vertex2 The id of the second vertex.
	 */
	public void deleteEdgeBetween(GradoopId vertex1, GradoopId vertex2) {
		completeGraph = Connectivity.deleteEdgeBetween(completeGraph, vertex1, vertex2);
		currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
	}
	
	/** Updates the current graph to a snapshot of the complete graph at the next timestep.
	 * The token transfer probability is then used determine which vertices without the token 
	 * will receive it from a neighbour that does have it. 
	 * The graphs are then updated to reflect the result of this operation. 
	 * @return False if the next timestep is greater than the greatest timestamp of all the 
	 * edges in the complete graph, or if an exception occurred. Otherwise, returns true. */
	public Boolean nextTimeStep() {
		try {
			currentTimestamp += timeIncrement;
			if (currentTimestamp > lastTimestamp) {
				return false;
			}
			currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
			
			String query = "MATCH (v1)-[]->(v2) WHERE (v1."+TOKEN_NAME+" = false AND v2."+TOKEN_NAME+" = true)"
					+ "OR (v1."+TOKEN_NAME+" = true AND v2."+TOKEN_NAME+" = false)";
			
			List<TemporalVertex> tokenNeighbours = currentGraph.query(query).getVertices().collect();
			List<String> neighbourIDs = new ArrayList<>();
			for (TemporalVertex v: tokenNeighbours) {
				neighbourIDs.add(v.getId().toString());
			}
			completeGraph = disseminate(completeGraph, tokenTransferProb, neighbourIDs);
			currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
			
			return true;
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * A method for disseminating a token within a graph.
	 * @param graph The graph to operate on.
	 * @param tokenTransferProb The probability of each possible token transfer occurring.
	 * @param vertices The IDs of the vertices in the current version of the graph which either have
	 *  the token or have an immediate neighbour that does (i.e. the ones that might next receive the token).
	 * @return Updated TemporalGraph.
	 */
	private TemporalGraph disseminate(TemporalGraph graph, double tokenTransferProb, List<String> vertices) {
		Random random = new Random();
		TemporalGraph newGraph = graph.transformVertices((TemporalVertex v, TemporalVertex v2) -> {
			if (vertices.contains(v.getId().toString()) && !v.getPropertyValue(TOKEN_NAME).getBoolean() 
					&& (random.nextDouble() < tokenTransferProb)) {
				v.setProperty(TOKEN_NAME, true);
			}
			return v;
		});
		return newGraph;
	}
	
	/**
	 * Sets the token to true for the vertices with the given IDs.
	 * @param vertices The IDs of the target vertices.
	 */
	public void giveTokenTo(List<String> vertices) {
		completeGraph = giveTokenTo(completeGraph, vertices);
		currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
	}
	
	private TemporalGraph giveTokenTo(TemporalGraph graph, List<String> vertices) {
		TemporalGraph newGraph = graph.transformVertices((TemporalVertex v, TemporalVertex v2) -> {
			if (vertices.contains(v.getId().toString())) {
				v.setProperty(TOKEN_NAME, true);
			}
			return v;
		});
		return newGraph;
	}
	
}
