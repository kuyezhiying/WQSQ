package neo4j.construction.datastruct;

import org.neo4j.graphdb.GraphDatabaseService;

public interface GraphDBInitial {

	public void setGraphDB(GraphDatabaseService db);
	
	public void shutDownDB();
}
