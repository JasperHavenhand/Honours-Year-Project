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
			
			TemporalGraphHandler handler = new TemporalGraphHandler(graph, "infected", 0.5, 1);
			Boolean end = handler.nextTimeStep();
			while (!end) {
	
				end = handler.nextTimeStep();
				
			}
			handler.getCompleteGraph().print();
			
			
		} catch (Exception e) {
			Log.getLog("data_sources_log").writeException(e);
			e.printStackTrace();
		}
		
	}
}
