import java.util.ArrayList;
import java.util.Comparator;

import org.apache.flink.api.common.operators.Order;
import org.gradoop.flink.model.impl.operators.matching.common.statistics.GraphStatistics;
import org.gradoop.temporal.io.impl.csv.TemporalCSVDataSource;
import org.gradoop.temporal.model.impl.TemporalGraph;
import org.gradoop.temporal.model.impl.TemporalGraphCollection;
import org.gradoop.temporal.model.impl.functions.predicates.AsOf;
import org.gradoop.temporal.model.impl.pojo.TemporalEdge;
import org.gradoop.temporal.model.impl.pojo.TemporalVertex;

import data.temporal.TemporalDataFactory;
import data.temporal.TemporalDataFactory.inputType;
import utilities.Log;

public class Testing {
	
	public static void main (String[] args) {
		try {
//			TemporalCSVDataSource data = TemporalDataFactory.createCSVDataSource(
//				"C:\\Users\\Student\\Documents\\Fourth Year\\COMP390 - Honours Year Project\\NSense_Traces_Set2_CRAWDAD",
//				inputType.NSENSE,"NSense_test");
			
			TemporalCSVDataSource data = TemporalDataFactory
					.loadCSVDataSource("C:\\Users\\Student\\eclipse-workspace\\Honours-Year-Project\\data\\NSense_test");
			
			TemporalGraph graph = data.getTemporalGraph();
			
//			TemporalEdge edge = graph.getEdges().sortPartition("validTime.f0", Order.ASCENDING).setParallelism(1).collect().get(0);
//			long timestamp = edge.getValidFrom();
//			System.out.println(timestamp);
//			
//			TemporalGraph graph2 = graph.snapshot(new AsOf(timestamp));
//			System.out.println(graph2.getEdges().count());
//			graph2.print();
			
//			String query = "MATCH (v1)-[]->(v2) WHERE v1.infected = false AND v2.infected = true";
//			
//			long verticesCount = graph.getVertices().count();
//			GraphStatistics graphStats = new GraphStatistics(verticesCount,
//					graph.getEdges().count(), verticesCount, verticesCount);
//			
//			TemporalGraphCollection newInfections = graph.query(query, graphStats);
//			newInfections.print();
//			for (TemporalVertex vertex: newInfections.getVertices().collect()) {
//				if (!vertex.getPropertyValue("infected").getBoolean()) {
//					vertex.setProperty("infected", true);
//				}
//			}
			
			System.out.println(graph.query("MATCH (v1) WHERE v1.infected = true").getVertices().count());
			ArrayList<TemporalVertex> vertices = new ArrayList<TemporalVertex>();
			for (TemporalVertex vertex: graph.getVertices().collect()) {
				if (!vertex.getPropertyValue("infected").getBoolean()) {
					vertices.add(vertex);
				}
			}
			graph = graph.transformVertices((TemporalVertex v, TemporalVertex v2) -> {
				if (vertices.contains(v)) {
					v.setProperty("infected", true);
				}
				return v;
			});
			System.out.println(graph.query("MATCH (v1) WHERE v1.infected = true").getVertices().count());
			
			
		} catch (Exception e) {
			Log.getLog("data_sources_log").writeException(e);
			e.printStackTrace();
		}
		
	}
}
