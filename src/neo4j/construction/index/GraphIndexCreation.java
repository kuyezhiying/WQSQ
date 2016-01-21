package neo4j.construction.index;

import java.util.Iterator;
import java.util.Map;

import neo4j.construction.datastruct.GraphDBInitial;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.RelationshipIndex;

public class GraphIndexCreation  implements GraphDBInitial{
	
	private GraphDatabaseService graphDb;
	private static final String nodeIndexName = "nodes";
	private static final String nodeLabelTag = "uri";
	private static final String nodePropIndexName = "properties";
	private static final String nodePropLabelTag = "property";
	private static final String edgeIndexName = "edges";
	private static final String edgeLabelTag = "relation_label";
	
	@SuppressWarnings("deprecation")
	public GraphIndexCreation(String DB_PATH){
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );   
        
        registerShutdownHook( graphDb );
	}
	
	public GraphIndexCreation(GraphDatabaseService graphDb) {
	
		this.setGraphDB(graphDb);
		 registerShutdownHook( graphDb );
	}

	private static void registerShutdownHook( final GraphDatabaseService graphDb ) {
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook( new Thread() {
	        @Override
	        public void run() {
	            graphDb.shutdown();
	        }
	    } );
	}
	
	@SuppressWarnings("deprecation")
	public void createIndex() {
		Transaction tx = graphDb.beginTx();  
    	Iterable<Node> nodes = graphDb.getAllNodes();
    	
    	Index<Node> nodeIndex = graphDb.index().forNodes(nodeIndexName);
    	Index<Node> nodePropIndex = graphDb.index().forNodes(nodePropIndexName);
    	RelationshipIndex edgeIndex = graphDb.index().forRelationships(edgeIndexName);
    	for(Node n : nodes) {
    		try {
    			// create node index with node uri
    			nodeIndex.add(n, nodeLabelTag, n.getProperty(nodeLabelTag));
    			// create node index with node property name
    			Map<String, Object> properties = n.getAllProperties();
    			for(String prop : properties.keySet()) {
    				nodePropIndex.add(n, nodePropLabelTag, prop);
    			}
    			// create relationship index with relationship label
    			Iterator<Relationship> relationships = n.getRelationships().iterator();
    			while(relationships.hasNext()) {
    				Relationship relationship = relationships.next();
    				edgeIndex.add(relationship, edgeLabelTag, relationship.getProperty(edgeLabelTag));
    			}
    		} catch(Exception e) {
    			//如果节点不存在属性 propertyName忽略
    			continue;
    		}
    	}
    	tx.success();
    	tx.finish();
    	//shutDown();
	}
	
	@SuppressWarnings("deprecation")
	public void showGraph() {
    	Transaction tx = graphDb.beginTx() ;  
    	Iterable<Node> nodes = graphDb.getAllNodes();
    	for(Node n : nodes) {
    		System.out.println(n);
    	}
    	tx.success();
    	tx.finish();
    }
	  
	void shutDown() {
        System.out.println();
        System.out.println( "Node index constructing successed, shutting down database ..." );
		// START SNIPPET: shutdownServer
		graphDb.shutdown();
		// END SNIPPET: shutdownServer
	}
	   
	public static void main(String[] args) {     
		String DB_PATH = "C:\\Users\\huangchao\\Documents\\Neo4j\\whynot.runtime.graphdb";
		GraphIndexCreation indexCreation = new GraphIndexCreation(DB_PATH);			
		//对属性 labeltag 创建名称为 indexName的索引
		indexCreation.createIndex();	
		//indexCreation.showGraph();
	}

	@Override
	public void setGraphDB(GraphDatabaseService db) {
		this.graphDb = db;		
	}

	@Override
	public void shutDownDB() {
		// TODO Auto-generated method stub
		shutDown();
	}


}
