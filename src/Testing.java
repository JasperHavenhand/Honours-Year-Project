import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.flink.model.impl.operators.statistics.ConnectedComponentsDistribution;
import org.gradoop.temporal.io.impl.csv.TemporalCSVDataSource;
import org.gradoop.temporal.model.impl.TemporalGraph;
import org.gradoop.temporal.model.impl.pojo.TemporalEdge;
import org.gradoop.temporal.model.impl.pojo.TemporalVertex;

import data.temporal.TemporalDataFactory;
import graph.temporal.TemporalGraphHandler;
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
			TemporalGraphHandler handler = new TemporalGraphHandler(graph, "infected", 0.5, 60000L);			
			
			handler.mergeEdges(1474113600000L, 600000L);
			
//			List<Triple<GradoopId, GradoopId, Long>> temporalities = handler.getTemporalities();
//			for (Triple<GradoopId, GradoopId, Long> t: temporalities) {
//				System.out.println(t.getLeft()+" "+t.getMiddle()+" "+t.getRight());
//			}
			
//			List<Tuple2<TemporalVertex, List<TemporalVertex>>> reachabilitySets = handler.getReachabilitySets();
//			for (Tuple2<TemporalVertex, List<TemporalVertex>> set: reachabilitySets) {
//				System.out.print(set.f0.getPropertyValue("name") + "-");
//				for (TemporalVertex v: set.f1) {
//					System.out.print(v.getPropertyValue("name") +" ");
//				}
//				System.out.println();
//			}
			
//			for (int i = 0; i < 60; i++) {
//				System.out.println("timestep " + i);
//				handler.nextTimeStep();
//			}
//			handler.getCompleteGraph().getVertices().print();
				
		} catch (Exception e) {
			Log.getLog("data_sources_log").writeException(e);
			e.printStackTrace();
		}
		
	}
}
