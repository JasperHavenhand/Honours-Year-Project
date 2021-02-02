package temporalData;
import java.util.ArrayList;

/**
 * 
 * @author Jasper Havenhand
 *
 */
public abstract class TemporalDataSource {
	
	public abstract ArrayList<String> getGraphs();
	
	public abstract ArrayList<String> getVertices();
	
	public abstract ArrayList<String> getEdges();
	
	public abstract ArrayList<String> getMetadata();
}
