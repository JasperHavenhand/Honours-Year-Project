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
	private String tokenName;
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
	 * @param tokenName The name of the vertex property that represents the token that will be disseminated.
	 * @param tokenTransferProb The probability of the token being transferred over an active edge (0.0 to 1.0).
	 * @param timeIncrement The time increment to be used between each timestep (in milliseconds). 
	 */
	public TemporalGraphHandler(TemporalGraph graph, String tokenName, double tokenTransferProb, long timeIncrement) {
		try {
			completeGraph = graph;
			this.tokenTransferProb = tokenTransferProb;
			this.tokenName = tokenName;
			
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
	 */
	public List<Triple<GradoopId,GradoopId,Long>> getTemporalities() {
		return Connectivity.temporalitiesOf(completeGraph);
	}

	/**
	 * @return Each vertex in the graph paired with a list of the vertices temporarily reachable from it.
	 */
	public List<Tuple2<TemporalVertex, List<TemporalVertex>>> getReachabilitySets() {
		return Connectivity.reachabilitySetsOf(completeGraph);
	}
	
	public void mergeEdges(long startTime, long duration) {
		completeGraph = Connectivity.mergeEdges(completeGraph, startTime, duration);
		currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
	}
	
	public void delayEdges(long time) {
		completeGraph = Connectivity.delayEdges(completeGraph, time);
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
			
			String query = "MATCH (v1)-[]->(v2) WHERE (v1."+tokenName+" = false AND v2."+tokenName+" = true)"
					+ "OR (v1."+tokenName+" = true AND v2."+tokenName+" = false)";
			
			List<TemporalVertex> tokenNeighbours = currentGraph.query(query).getVertices().collect();
			List<String> neighbourIDs = new ArrayList<>();
			for (TemporalVertex v: tokenNeighbours) {
				neighbourIDs.add(v.getId().toString());
			}
			completeGraph = disseminate(completeGraph, tokenName, tokenTransferProb, neighbourIDs);
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
	 * @param tokenName The name of the vertex property being used as a token.
	 * @param tokenTransferProb The probability of each possible token transfer occurring.
	 * @param vertices The IDs of the vertices in the current version of the graph which either have
	 *  the token or have an immediate neighbour that does (i.e. the ones that might next receive the token).
	 * @return Updated TemporalGraph.
	 */
	TemporalGraph disseminate(TemporalGraph graph, String tokenName, double tokenTransferProb, List<String> vertices) {
		Random random = new Random();
		TemporalGraph newGraph = graph.transformVertices((TemporalVertex v, TemporalVertex v2) -> {
			if (vertices.contains(v.getId().toString()) && !v.getPropertyValue(tokenName).getBoolean() 
					&& (random.nextDouble() < tokenTransferProb)) {
				v.setProperty(tokenName, true);
			}
			return v;
		});
		return newGraph;
	}
	
}
