package graph.temporal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.temporal.model.impl.TemporalGraph;
import org.gradoop.temporal.model.impl.TemporalGraphCollection;
import org.gradoop.temporal.model.impl.pojo.TemporalEdge;
import org.gradoop.temporal.model.impl.pojo.TemporalVertex;

import utilities.Log;

class Connectivity {
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "graphs_log";
	
	static List<Tuple2<TemporalVertex, List<TemporalVertex>>> reachabilitySetsOf(TemporalGraph graph) {
		try {
			List<Tuple2<TemporalVertex, List<TemporalVertex>>> sets = new ArrayList<Tuple2<TemporalVertex, List<TemporalVertex>>>();
			List<TemporalVertex> vertices = graph.getVertices().collect();
			for (TemporalVertex vertex: vertices) {
				System.out.println(vertex.getPropertyValue("name"));
				String query = "MATCH (v1)-[*]->(v2) WHERE v1.name = "+vertex.getPropertyValue("name");
				List<TemporalVertex> results = graph.query(query).getVertices().collect();
				results.remove(vertex);
				sets.add(new Tuple2<TemporalVertex, List<TemporalVertex>>(vertex,results));
			}
			return sets;
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}
	
	static List<TemporalVertex> findReachableVertices(TemporalGraph graph, TemporalVertex source, 
			List<TemporalVertex> visited, Long lastTime) {
		try {
			String query = "MATCH (v1)-[e:]->(v2) WHERE v1.name = "+source.getPropertyValue("name")+
					"AND e.validFrom <= "+lastTime+" AND e.validTo >= "+lastTime;
			List<TemporalVertex> next = graph.query(query).getVertices().collect();
			List<TemporalVertex> result = new ArrayList<TemporalVertex>();
			if (!visited.equals(null)) {
				next.removeAll(visited);
			}
			if (next.isEmpty()) {
				result.add(source);
				return result;
			} else {
				for (TemporalVertex n: next) {
					List<TemporalVertex> newVisited = visited;
					newVisited.add(n);
					Long newTime;
					result.addAll(findReachableVertices(graph,n,newVisited,newTime));
				}
				return result;
			}
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}
	
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
