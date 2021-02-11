package graph.temporal;

import org.apache.flink.api.common.operators.Order;
import org.gradoop.temporal.model.impl.TemporalGraph;
import org.gradoop.temporal.model.impl.functions.predicates.AsOf;

import utilities.Log;

public final class TemporalGraphHandler {

	private TemporalGraph completeGraph;
	private TemporalGraph currentGraph;
	private int transmissionRisk;
	private Long currentTimestamp;
	private long timeIncrement;
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "graphs_log";
	
	public TemporalGraphHandler(TemporalGraph graph, int transmissionRisk) {
		try {
			completeGraph = graph;
			this.transmissionRisk = transmissionRisk;
			
			currentTimestamp = completeGraph.getEdges()
					.sortPartition("transactionTime", Order.ASCENDING)
					.collect().get(0).getTxFrom();
			
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
