import org.apache.flink.api.common.operators.Order;
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
			TemporalCSVDataSource data = TemporalDataFactory.createCSVDataSource(
				"C:\\Users\\Student\\Documents\\Fourth Year\\COMP390 - Honours Year Project\\NSense_Traces_Set2_CRAWDAD",
				inputType.NSENSE,"NSense_test");
			
//			TemporalCSVDataSource data = TemporalDataFactory
//					.loadCSVDataSource("C:\\Users\\Student\\eclipse-workspace\\Honours-Year-Project\\data\\NSense_test");
//			
//			TemporalGraph graph = data.getTemporalGraph();
//			System.out.println(graph.getEdges().count());
//			
//			TemporalEdge edge = graph.getEdges().sortPartition("validTime", Order.ASCENDING).collect().get(0);
//			long timestamp = edge.getValidFrom();
//			System.out.println(timestamp);
//			
//			System.out.println(graph.snapshot(new AsOf(timestamp)).getEdges().count());
			
//			String query = "MATCH (v1)-[]->(v2) WHERE v1.infected = false AND v2.infected = true";
//			
//			TemporalGraphCollection newInfections = graph.query(query);
////			newInfections.print();
//			for (TemporalVertex vertex: newInfections.getVertices().collect()) {
//				System.out.println(vertex.getPropertyValue("name"));
//			}
			
		}catch (Exception e) {
			Log.getLog("data_sources_log").writeException(e);
			e.printStackTrace();
		}
		
	}
}
