package RDFGraph.api.pack;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jena.api.pack.JenaAPI;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class RDFGraphConstruction {
	
	private static final String graphDBPath = "C:\\Users\\huangchao\\Documents\\Neo4j\\DBpedia.RDFGraphDB";
	private static final String jenaTDBPath = "D:\\workspace\\WhyNot\\tdb_onttype_ontinfobox_infoprop";
	private static Connection conn = null;
	//private static Statement stmt;
	GraphDatabaseService graphDb;
	
	@SuppressWarnings("deprecation")
	public RDFGraphConstruction() {
	 	try {
			FileUtils.deleteRecursively(new File(graphDBPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // START SNIPPET: startDb		
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(graphDBPath);
        // graphDb = new GraphDatabaseFactory().
        registerShutdownHook(graphDb);
	}
	
	public void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it shuts down
        // nicely when the VM exits (even if you "Ctrl-C" the running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
	}
	
    // END SNIPPET: shutdownHook
    void shutDown() {
        System.out.println();
        System.out.println( "Shutting down database ..." );
        // START SNIPPET: shutdownServer
        graphDb.shutdown();
        // END SNIPPET: shutdownServer
    }

	public static Connection getConnection() {
		Connection connection = null;
		try {
			Class.forName("org.neo4j.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:neo4j://localhost:7474/", "neo4j", "123456");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;
	}
	
    @SuppressWarnings("deprecation")
	public void constructRDFGraph() {
		JenaAPI jena = new JenaAPI(jenaTDBPath);
    	Transaction tx = graphDb.beginTx() ;  
    	GraphDBUtil graphDBUtil = new GraphDBUtil();
    	conn = getConnection();
    	try { 
    		String sparql = "select * where { ?s ?p ?o .}";
    		ResultSet resultset = jena.QueryTDB(sparql);
    		System.out.println("Has next result : " + resultset.hasNext());		//true if the resultset is not empty
    		while (resultset.hasNext()) {
    		    QuerySolution row= resultset.nextSolution();
    		    Resource subject = ResourceFactory.createResource(row.get("s").toString());
    		    Property predicate= ResourceFactory.createProperty(row.get("p").toString());
    		    RDFNode object= row.get("o");
    		    if(object.isResource()) {
    		    	graphDBUtil.createUniqueNode(subject.toString());
    		    	graphDBUtil.createUniqueNode(object.toString());
    		    	graphDBUtil.createRelationship(subject.toString(), predicate.toString(), object.toString());
    		    } else {
    		    	if(graphDBUtil.nodeExist(subject.toString())) {
        		    	//String setNodeAttrQuery = "MATCH (node { uri: '" + subject.toString() + "' }) SET node += { " + predicate.toString() + ": '" + object.toString() + "' }";
    		    	} else {
    		    		//String createNodeQuery = "";
    		    	}
    		    }
    			//Statement triple = ResourceFactory.createStatement(subject, predicate, object);
    			String query = "MATCH (:Movie {title:{1}})<-[:ACTED_IN]-(a:Person) RETURN a.name as actor";
    			PreparedStatement stmt = conn.prepareStatement(query);
    			stmt.setString(1,"The Matrix");
    			stmt.executeQuery();
    		}
    		tx.success();
			conn.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    	} finally {
			tx.finish();
    	}
	}
}
