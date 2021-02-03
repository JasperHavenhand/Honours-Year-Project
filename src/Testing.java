import org.apache.flink.api.java.DataSet;
import org.gradoop.temporal.io.impl.csv.TemporalCSVDataSource;
import org.gradoop.temporal.model.impl.TemporalGraph;
import org.gradoop.temporal.model.impl.pojo.TemporalEdge;
import org.gradoop.temporal.model.impl.pojo.TemporalGraphHead;
import org.gradoop.temporal.model.impl.pojo.TemporalVertex;

import temporalData.TemporalDataFactory;
import temporalData.TemporalDataFactory.inputType;

public class Testing {
	
	public static void main (String[] args) {
		TemporalCSVDataSource data = TemporalDataFactory.createCSVDataSource(
				"C:\\Users\\Student\\Documents\\Fourth Year\\COMP390 - Honours Year Project\\NSense_Traces_Set2_CRAWDAD",
				inputType.NSENSE,"NSense_test");
		
		TemporalGraph graph = data.getTemporalGraph();
		try {
			for (TemporalGraphHead head: graph.getGraphHead().collect()) {
				System.out.println(head.getPropertyValue("name"));
			}
			for (TemporalVertex vertex: graph.getVertices().collect()) {
				System.out.println(vertex.getPropertyValue("name"));
			}
			for (TemporalEdge edge: graph.getEdges().collect()) {
				System.out.println(edge.getId());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
