package graph.temporal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.flink.model.api.functions.TransformationFunction;
import org.gradoop.temporal.model.impl.TemporalGraph;
import org.gradoop.temporal.model.impl.TemporalGraphCollection;
import org.gradoop.temporal.model.impl.pojo.TemporalEdge;
import org.gradoop.temporal.model.impl.pojo.TemporalVertex;

import utilities.Log;

class Connectivity {
	/** The name of the log file that will be used by this class. */
	private static String LOG_NAME = "graphs_log";
	
	/**
	 * Finds the reachability sets of the given temporal graph.
	 * @param graph
	 * @return Each vertex of the given graph, paired with a list of the vertices temporarily reachable from it.
	 */
	static List<Tuple2<TemporalVertex, List<TemporalVertex>>> reachabilitySetsOf(TemporalGraph graph) {
		try {
			List<Tuple2<TemporalVertex, List<TemporalVertex>>> sets = new ArrayList<Tuple2<TemporalVertex, List<TemporalVertex>>>();
			List<TemporalVertex> vertices = graph.getVertices().collect();
			for (TemporalVertex vertex: vertices) {
				List<TemporalVertex> results = findReachableVertices(graph, vertex);
				sets.add(new Tuple2<TemporalVertex, List<TemporalVertex>>(vertex,results));
			}
			return sets;
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}
	
	static List<TemporalVertex> findReachableVertices(TemporalGraph graph, TemporalVertex source) {
		return findReachableVertices(graph, source, null, null);
	}
	
	private static List<TemporalVertex> findReachableVertices(TemporalGraph graph, TemporalVertex source, 
			List<TemporalVertex> visited, Long lastTime) {
		try {
			String query;
			if (lastTime == null) {
				query = "MATCH (v1)-[e]->(v2) WHERE v1.name = \""+source.getPropertyValue("name")+"\"";
			} else {
				query = "MATCH (v1)-[e]->(v2) WHERE v1.name = \""+source.getPropertyValue("name")+
						"\" AND e.validFrom <= "+lastTime+" AND e.validTo >= "+lastTime;
			}
			if (visited == null) {
				visited = new ArrayList<TemporalVertex>();
			}
			visited.add(source);
			TemporalGraphCollection collection = graph.query(query);
			List<TemporalVertex> vertices = collection.getVertices().collect();
			vertices.remove(source);
			List<TemporalEdge> edges = collection.getEdges().collect();

			List<TemporalVertex> result = new ArrayList<TemporalVertex>();
			result.add(source);
			for (TemporalVertex v: vertices) {
				if (!visited.contains(v)) {
					Long newTime = null;
					for (TemporalEdge e: edges) {
						if (e.getSourceId() == source.getId() &&
								e.getTargetId() == v.getId()) {
							newTime = e.getValidFrom();
							break;
						}
					}
					result.addAll(findReachableVertices(graph,v,visited,newTime));
				}	
			}
			return result;
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
				public boolean filter(TemporalEdge edge) throws Exception {
					return (edge.getSourceId().compareTo(v1.getId()) == 0 &
							edge.getTargetId().compareTo(v2.getId()) == 0);
				}
			}).count();
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}
	
	static TemporalGraph mergeEdges(TemporalGraph graph, Long startTime, Long duration) {
		try {	
			TemporalGraph newGraph = graph.transform(
					// Keep the graph heads.
					TransformationFunction.keep(),
					// Keep the vertices.
					TransformationFunction.keep(), 
					// Merge the edges.
					(e1, e2) -> {
						if (e1.getValidFrom().compareTo(startTime) < 0) {
							e1.setValidFrom(startTime);
							e1.setTxFrom(startTime);
							e1.setValidTo(startTime+duration);
							e1.setTxTo(startTime+duration);
						}
						return e1;
					}
			);
			//Removing any identical edges created during the merging.
			DataSet<TemporalEdge> distinctEdges = newGraph.getEdges().distinct("label",
					"sourceId","targetId","transactionTime","validTime");
			
			return newGraph.getFactory().fromDataSets(newGraph.getVertices(), distinctEdges);
			
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}
	
	static TemporalGraph delayEdges(TemporalGraph graph, Long time) {
		TemporalGraph newGraph = graph.transform(
				// Keep the graph heads.
				TransformationFunction.keep(),
				// Keep the vertices.
				TransformationFunction.keep(), 
				// Delay the edges.
				(e1, e2) -> {
					e1.setValidFrom(e1.getValidFrom()+time);
					e1.setTxFrom(e1.getTxFrom()+time);
					e1.setValidTo(e1.getValidTo()+time);
					e1.setTxTo(e1.getTxTo()+time);
					return e1;
				}
		);
		return newGraph;
	}
}
