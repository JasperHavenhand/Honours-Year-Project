package data.temporal;
import java.util.ArrayList;

abstract class TemporalDataSource {
	
	abstract ArrayList<String> getGraphs();
	
	abstract ArrayList<String> getVertices();
	
	abstract ArrayList<String> getEdges();
	
	abstract ArrayList<String> getMetadata();
}
