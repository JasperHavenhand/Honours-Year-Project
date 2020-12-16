/**
 * 
 * @author Jasper Havenhand
 *
 */
public final class NSenseDataSource implements ITemporalDataSource {
	
	private String rootURI;
	String[] graphs, vertices, edges, metaData;
	
	public NSenseDataSource(String inputURI) {
		rootURI = inputURI;
	}
	
	public String[] getGraphs() {
		return graphs;
	}

	public String[] getVertices() {
		return vertices;
	}

	public String[] getEdges() {
		return edges;
	}

	public String[] getMetaData() {
		return metaData;
	}

	private void setGraphs() {

	}

	private void setVertices() {

	}

	private void setEdges() {
	
	}

	private void setMetaData() {
	
	}

}
