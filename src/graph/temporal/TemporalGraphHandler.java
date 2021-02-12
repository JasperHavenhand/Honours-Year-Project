package graph.temporal;

import java.util.List;
import java.util.Random;

import org.apache.flink.api.common.operators.Order;
import org.gradoop.temporal.model.impl.TemporalGraph;
import org.gradoop.temporal.model.impl.TemporalGraphCollection;
import org.gradoop.temporal.model.impl.functions.predicates.AsOf;
import org.gradoop.temporal.model.impl.pojo.TemporalVertex;

import utilities.Log;

public final class TemporalGraphHandler {

	private TemporalGraph completeGraph;
	private TemporalGraph currentGraph;
	private String tokenName;
	/** The probability of a vertex with the token passing it to its neighbours. */
	private double tokenTransferProb;
	private Long currentTimestamp;
	private long timeIncrement;
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "graphs_log";
	
	/**
	 * 
	 * @param graph The temporalGraph to be handled.
	 * @param tokenName The name of the vertex property that represents the token that will be disseminated.
	 * @param tokenTransferProb The probability of the token being transferred over an active edge (0.0 to 1.0).
	 */
	public TemporalGraphHandler(TemporalGraph graph, String tokenName, double tokenTransferProb) {
		try {
			completeGraph = graph;
			this.tokenTransferProb = tokenTransferProb;
			this.tokenName = tokenName;
			
			currentTimestamp = completeGraph.getEdges().sortPartition("validTime.f0", Order.ASCENDING)
					.setParallelism(1).collect().get(0).getValidFrom();
			
			currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
	
	public TemporalGraph getCompleteGraph() {
		return completeGraph;
	}
	
	public TemporalGraph getCurrentGraph() {
		return currentGraph;
	}
	
	public void nextTimeStep() {
		try {
			currentTimestamp += timeIncrement;
			currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
			
			String query = "MATCH (v1)-[]->(v2) WHERE v1."+tokenName+" = false AND v2."+tokenName+" = true";
			
			List<TemporalVertex> tokenNeighbours = currentGraph.query(query).getVertices().collect();
			Random random = new Random();
			completeGraph = completeGraph.transformVertices((TemporalVertex v, TemporalVertex v2) -> {
				if (tokenNeighbours.contains(v) && !v.getPropertyValue(tokenName).getBoolean()&& (random.nextDouble() <= tokenTransferProb)) {
					v.setProperty(tokenName, true);
				}
				return v;
			});
			currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
		}
	}
}
