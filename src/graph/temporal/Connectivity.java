package graph.temporal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.flink.api.common.functions.FilterFunction;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.temporal.model.impl.TemporalGraph;
import org.gradoop.temporal.model.impl.pojo.TemporalEdge;
import org.gradoop.temporal.model.impl.pojo.TemporalVertex;

import utilities.Log;

class Connectivity {
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "graphs_log";
	
	/**
	 * Finds the temporality of each distinct edge in the given graph.
	 * @param graph A TemporalGraph
	 * @return A list of triples containing the source vertex Id, 
	 * target vertex Id and temporality of each distinct edge.
	 */
	static List<Triple<GradoopId,GradoopId,Long>> temporalitiesOf(TemporalGraph graph) {
		try {
			List<Triple<GradoopId,GradoopId,Long>> temporalities = new ArrayList<Triple<GradoopId,GradoopId,Long>>();
			List<TemporalVertex> vertices = graph.getVertices().collect();
			
			for (TemporalVertex v1: vertices) {
				for (TemporalVertex v2: vertices) {
					if (v1.getId().compareTo(v2.getId()) < 0) {
						Long count = countEdgesBetween(graph, v1, v2);
						if (count > 0) {
							temporalities.add(Triple.of(v1.getId(), v2.getId(), count));
						}
					}
				}				
			}
			
			return temporalities;
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Counts the number of edges from the vertex v1 to the vertex v2 in the given graph.
	 * @param graph The TemporalGraph containing v1 and v2.
	 * @param v1 The source vertex to filter the edges by.
	 * @param v2 The target vertex to filter the edges by.
	 * @return The number of edges.
	 */
	private static Long countEdgesBetween(TemporalGraph graph, TemporalVertex v1, TemporalVertex v2) {
		try {
			return graph.getEdges().filter(new FilterFunction<TemporalEdge>() {
				private static final long serialVersionUID = -7608048626367438469L;
				@Override
				public boolean filter(TemporalEdge value) throws Exception {
					return (value.getSourceId().compareTo(v1.getId()) == 0 &
							value.getTargetId().compareTo(v2.getId()) == 0);
				}
			}).count();
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}
}
