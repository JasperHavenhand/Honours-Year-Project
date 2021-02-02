package temporalData;
import java.util.ArrayList;

/**
 * 
 * @author Jasper Havenhand
 *
 */
abstract class TemporalDataSource {
	
	abstract ArrayList<String> getGraphs();
	
	abstract ArrayList<String> getVertices();
	
	abstract ArrayList<String> getEdges();
	
	abstract ArrayList<String> getMetadata();
}
