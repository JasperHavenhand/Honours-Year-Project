package graph.temporal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.flink.model.api.functions.TransformationFunction;
import org.gradoop.temporal.model.impl.TemporalGraph;
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
	static List<Tuple2<String, List<String>>> reachabilitySetsOf(TemporalGraph graph) {
		try {
			List<Tuple2<String, List<String>>> sets = new ArrayList<Tuple2<String, List<String>>>();
			List<TemporalVertex> vertices = graph.getVertices().collect();
			for (TemporalVertex vertex: vertices) {
				List<GradoopId> results = findReachableVertices(graph, vertex.getId());
				List<String> strResults = new ArrayList<String>();
				for (GradoopId id: results) {
					strResults.add(id.toString());
				}
				sets.add(new Tuple2<String, List<String>>(vertex.getId().toString(),strResults));
			}
			return sets;
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}
	
	private static List<GradoopId> findReachableVertices(TemporalGraph graph, GradoopId source) {
		List<GradoopId> visited = new ArrayList<GradoopId>();
		visited.add(source);
		return findReachableVertices(graph, source, visited, null);
	}
	
	private static List<GradoopId> findReachableVertices(
			TemporalGraph graph, GradoopId source, List<GradoopId> visited, Long lastTime) {
		try {
			// Find all edges connected the source vertex which are active at lastTime.
			List<TemporalEdge> edges = graph.getEdges().filter(new FilterFunction<TemporalEdge>() {
				private static final long serialVersionUID = -3946849681069559284L;
				@Override
				public boolean filter(TemporalEdge edge) throws Exception {
					/* Edges are directed in Gradoop but are considered undirected in this usage
					 * so both the source and target vertices have to be considered. */
					Boolean b1 = edge.getSourceId().equals(source) ||
							edge.getTargetId().equals(source);
					Boolean b2 = true;
					if (lastTime != null) {
						b2 = edge.getValidTo() > lastTime;
					}
					return (b1 && b2);
				}
			}).collect();
			
			List<GradoopId> result = new ArrayList<GradoopId>();
			result.add(source);
			
			// Recursive calls for each vertex which could possibly be visited next.
			for (TemporalEdge edge: edges) {
				List<GradoopId> newVisited = visited;
				// Again, both the source and target vertices are considered.
				if (!visited.contains(edge.getSourceId())) {
					newVisited.add(edge.getSourceId());
					result.addAll(findReachableVertices(graph, edge.getSourceId(), newVisited, edge.getValidTo()));
				} 
				else if (!visited.contains(edge.getTargetId())) {
					newVisited.add(edge.getTargetId());
					result.addAll(findReachableVertices(graph, edge.getTargetId(), newVisited, edge.getValidTo()));
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
	 * Finds the temporality of the edges in the given graph.
	 * @param graph A TemporalGraph
	 * @return A list of triples, each containing the IDs of a pair of
	 *  connected vertices and the temporality of the edge between them.
	 */
	static List<Triple<String,String,Long>> temporalitiesOf(TemporalGraph graph) {
		try {
			List<Triple<String,String,Long>> temporalities = new ArrayList<Triple<String,String,Long>>();
			List<TemporalVertex> vertices = graph.getVertices().collect();
			
			for (TemporalVertex v1: vertices) {
				for (TemporalVertex v2: vertices) {
					if (v1.getId().compareTo(v2.getId()) < 0) {
						Long count = countEdgesBetween(graph, v1, v2) + countEdgesBetween(graph, v2, v1);
						if (count > 0) {
							temporalities.add(Triple.of(v1.getId().toString(), v2.getId().toString(), count));
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
					return (edge.getSourceId().compareTo(v1.getId()) == 0 &&
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
			DataSet<TemporalEdge> distinctEdges = newGraph.getEdges().distinct(
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
	
	/**
	 * Limits the temporality of the given graph by grouping the edges by their target and source vertices,
	 * and then randomly selecting up to the specified number of edges (the limit) from each group until the groups are all no 
	 * larger than the specified limit.
	 * @param graph The TemporalGraph to operate on.
	 * @param limit The maximum temporality of an edge between any two vertices.
	 * @return The updated TemporalGraph.
	 */
	static TemporalGraph limitTemporality(TemporalGraph graph, int limit) {
		try {
			DataSet<TemporalEdge> oldEdgeSet = graph.getEdges();
			List<TemporalEdge> newEdgeSet = new ArrayList<TemporalEdge>();
			List<TemporalVertex> vertices = graph.getVertices().collect();
			Random rdm = new Random();
			
			for (TemporalVertex v1: vertices) {
				for (TemporalVertex v2: vertices) {
					if (v1.getId().compareTo(v2.getId()) < 0) {
						// Finding the edges that exist between the current pair of vertices.
						List<TemporalEdge> filteredEdges = 
								oldEdgeSet.filter(new FilterFunction<TemporalEdge>() {
									private static final long serialVersionUID = -5742127640296074846L;
									@Override
									public boolean filter(TemporalEdge edge) throws Exception {
										return ((edge.getSourceId().equals(v1.getId()) &&
												edge.getTargetId().equals(v2.getId())) 
												||
												(edge.getSourceId().equals(v2.getId()) &&
												edge.getTargetId().equals(v1.getId())));
									}
								}).collect();
						
						if (filteredEdges.size() > limit) {
							/*Selecting [limit] random integers in the inclusive range 0-(filteredEdges.size()-1) 
							 * and getting the edges at those indexes.*/
							List<Integer> rdmIndexes = new ArrayList<Integer>(limit);
							while (rdmIndexes.size() < limit) {
								int i = rdm.nextInt(limit);
								if (!rdmIndexes.contains(i)) {
									rdmIndexes.add(i);
								}
							}
							List<TemporalEdge> selectedEdges = new ArrayList<TemporalEdge>();
							for (int index: rdmIndexes) {
								selectedEdges.add(filteredEdges.get(index));
							}
							newEdgeSet.addAll(selectedEdges);
						} else {
							// If the number of edges doesn't exceed the limit then none of them are dropped.
							newEdgeSet.addAll(filteredEdges);
						}
					}	
				}	
			}
			return graph.getFactory().fromCollections(graph.getVertices().collect(), newEdgeSet);
		} catch (Exception e) {
			Log.getLog(LOG_NAME).writeException(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Deletes the edge between a specified pair of vertices in the given graph.
	 * @param graph The graph containing the pair of vertices.
	 * @param vertex1 The id of the first vertex.
	 * @param vertex2 The id of the second vertex.
	 * @return The updated TemporalGraph.
	 */
	static TemporalGraph deleteEdgeBetween(TemporalGraph graph, String vertex1, String vertex2) {
		DataSet<TemporalEdge> filteredEdges = 
				graph.getEdges().filter(new FilterFunction<TemporalEdge>() {
					private static final long serialVersionUID = -8621601493928872890L;
					@Override
					public boolean filter(TemporalEdge edge) throws Exception {
						Boolean v1toV2 = edge.getSourceId().toString().equals(vertex1) &&
								 edge.getTargetId().toString().equals(vertex2);
						Boolean v2toV1 = edge.getSourceId().toString().equals(vertex2) &&
								 edge.getTargetId().toString().equals(vertex1);
						return !(v1toV2 || v2toV1);
					}
				});
		return graph.getFactory().fromDataSets(graph.getVertices(), filteredEdges);
	}
}

