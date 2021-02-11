package graph.temporal;

import org.apache.flink.api.common.operators.Order;
import org.gradoop.temporal.model.impl.TemporalGraph;
import org.gradoop.temporal.model.impl.functions.predicates.AsOf;

import utilities.Log;

public final class TemporalGraphHandler {

	private TemporalGraph completeGraph;
	private TemporalGraph currentGraph;
	private String tokenPropName;
	private int tokenTransProb;
	private Long currentTimestamp;
	private long timeIncrement;
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "graphs_log";
	
	/**
	 * 
	 * @param graph The temporalGraph to be handled.
	 * @param tokenPropName The name of the vertex property that represents the token that will be disseminated.
	 * @param tokenTransProb The probability of the token being passed over an active connection.
	 */
	public TemporalGraphHandler(TemporalGraph graph, String tokenPropName, int tokenTransProb) {
		try {
			completeGraph = graph;
			this.tokenTransProb = tokenTransProb;
			this.tokenPropName = tokenPropName;
			
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
		
		currentGraph = completeGraph.snapshot(new AsOf(currentTimestamp));
	}
}
