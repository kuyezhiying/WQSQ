package neo4j.construction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import jena.api.pack.JenaAPI;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import neo4j.construction.datastruct.GraphDBInitial;
import neo4j.construction.datastruct.Propery;
import neo4j.construction.datastruct.SemanticRelation;
import neo4j.construction.datastruct.SemanticNode;
import neo4j.construction.index.GraphIndexCreation;
import data.process.*;

public class GraphConstruction implements GraphDBInitial {

	GraphDatabaseService graphDb;
	/**
	 * @param quadruples
	 * @function 从四元组 生成语义节点、语义边
	 * @time 2015/12/11
	 */
	@SuppressWarnings("deprecation")
	public GraphConstruction(String DB_PATH) {
		try {
			FileUtils.deleteRecursively( new File( DB_PATH ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// START SNIPPET: startDb		
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
		registerShutdownHook( graphDb );
	}
	
	public GraphConstruction(GraphDatabaseService graphDb) {
		this.setGraphDB(graphDb);
		registerShutdownHook( graphDb );
	}

	public void computeSemanticNodes(ArrayList<Quadruple> quadruples) {
		HashMap<String, SemanticNode> semanticNodes = new HashMap<String, SemanticNode>();
		ArrayList<SemanticRelation> semanticRelations = new ArrayList<SemanticRelation>();
		for(Quadruple quad : quadruples) {
			switch(quad.quadrupleType){
			case TYPE:
			case ATTRIBUTE:
				SemanticNode currentSemanticNode = null;
				if(semanticNodes.containsKey(quad.getSubject())){
					currentSemanticNode = semanticNodes.get(quad.getSubject());			 
				}else{
					currentSemanticNode = new SemanticNode(quad.getSubject());
					semanticNodes.put(quad.getSubject(), currentSemanticNode);
				}
				Propery p = new Propery(quad.getPredicate(), quad.getObject());
				currentSemanticNode.addpropery(p);
				break;
			case RELATIOIN:
				SemanticNode start = null,end = null;
				if(semanticNodes.containsKey(quad.getSubject())) {
					start = semanticNodes.get(quad.getSubject());			 
				} else {
					start = new SemanticNode(quad.getSubject());
					semanticNodes.put(quad.getSubject(), start);
				}
				if(semanticNodes.containsKey(quad.getObject())) {
					end = semanticNodes.get(quad.getObject());			 
				} else {
					end = new SemanticNode(quad.getObject());
					semanticNodes.put(quad.getObject(), end);
				}
				SemanticRelation semanticRelation = new SemanticRelation(quad.getPredicate(), start, end);
				semanticRelations.add(semanticRelation);
				break;
			default:
				break;	
			}
		 }		
		 //construct neo4j node
		 Collection<SemanticNode> allNodes = semanticNodes.values();
		 System.out.println("Number of semantic nodes : " + allNodes.size());
		
		 //System.out.println("Constructing semantic nodes...");
		 for(SemanticNode semanticNode : allNodes){
			 //System.out.println("创建节点："+semanticNode.getId());
			 
			 semanticNode.InstantialNode(graphDb);
		 }
		 //System.out.println("Finished constructing nodes, start to construct semantic edges...");
		 //construct semantic edges
		 //Collection<SemanticRelation> allRelations = semanticRelations.values();
		 System.out.println("Number of semantic edges : " + semanticRelations.size());
		 for(SemanticRelation relation:semanticRelations){
			// System.out.println("创建边："+relation.getRelationName());
			 relation.InstantialRelation();
		 }
		 //System.out.println("Finished constructing edges.");
	}
	
	// START SNIPPET: shutdownHook
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
    // END SNIPPET: shutdownHook
    
    void shutDown() {
        System.out.println();
        System.out.println( "Neo4j graph constructing successed, shutting down database ..." );
        // START SNIPPET: shutdownServer
        graphDb.shutdown();
        // END SNIPPET: shutdownServer
    }
    
    void removeData() {
        try {
        	Transaction tx = graphDb.beginTx();
            tx.success();  
        }catch(Exception e){
        	e.printStackTrace();
        }

    }
    
    @SuppressWarnings("deprecation")
	public void construct(String keyword, String sparql){
    	Transaction tx = graphDb.beginTx();  
  	    try { 
  			QuadrupleGeneration quadGeneration = new QuadrupleGeneration();
  			//ArrayList<Quadruple> quadruples = quadGeneration.getQuardruples(keyword, sparql);
  			//ArrayList<Quadruple> runtimeQuads = quadGeneration.removeUselessQuads(quadruples, sparql);
  			ArrayList<Quadruple> runtimeQuads = quadGeneration.generateQuardruples(keyword, sparql);
  	    	//System.out.println("Number of quadruples : " + runtimeQuads.size());
  	    	computeSemanticNodes(runtimeQuads);
  	    	tx.success();
  	    }catch (Exception e) {
  	    	e.printStackTrace();
  	    }
  	    finally{
  	    	tx.finish();
  	    }
  	   // shutDown();
    }
    
    @SuppressWarnings("deprecation")
	public void showGraph(){
    	Transaction tx = graphDb.beginTx() ;  
    	Iterable<Node> nodes = graphDb.getAllNodes();
    	for(Node n:nodes){
    	  System.out.println(n);
    	}
    	tx.success();
    	tx.finish();
    }
    
    @SuppressWarnings("deprecation")
	public void constructGlobalGraph(String datasetpath) {
    	JenaAPI jena = new JenaAPI(datasetpath);
    	Model model = jena.getModel();
	    StmtIterator it =  model.listStatements();
	    ArrayList<Quadruple> quadruples = new ArrayList<Quadruple>();
	    while(it.hasNext()) {
	    	Statement stmt = it.nextStatement();
	    	Quadruple quad = null;
	    	if(stmt.getObject().isResource()) {
	    		quad = new Quadruple(stmt.getSubject().toString(), stmt.getPredicate().toString(), stmt.getObject().toString(), QuadrupleType.RELATIOIN);
	    	} else {
	    		quad = new Quadruple(stmt.getSubject().toString(), stmt.getPredicate().toString(), stmt.getObject().toString(), QuadrupleType.ATTRIBUTE);
	    	}
	    	quadruples.add(quad);
	    }
    	Transaction tx = graphDb.beginTx() ;  
  	    try { 
  	    	System.out.println("Number of quadruples : " + quadruples.size());
  	    	computeSemanticNodes(quadruples);
  	    	tx.success();
  	    }catch (Exception e) {
  	    	e.printStackTrace();
  	    }
  	    finally{
  	    	tx.finish();
  	    }
    }
    
	public static void main(String[] args) {
/*		String DB_PATH = "C:\\Users\\huangchao\\Documents\\Neo4j\\whynot.runtime.graphdb";
		String keyword = "aristotle";
		String sparql = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbpr: <http://dbpedia.org/resource/> SELECT * WHERE {	?var1 dbpp:era dbpr:Medieval_philosophy .	?var2 dbpp:influenced ?var1 .	?var2 dbpp:name \"Avicenna\"@en .}";
		GraphConstruction graphConstruction = new GraphConstruction(DB_PATH);
		graphConstruction.construct(keyword, sparql);
*/		
		String Global_DB_PATH = "C:\\Users\\huangchao\\Documents\\Neo4j\\dbpedia.global.graphdb";
		String datasetpath = "D:\\workspace\\WhyNot\\tdb_onttype_ontinfobox_infoprop";
		GraphConstruction graphConstruction = new GraphConstruction(Global_DB_PATH);
		graphConstruction.constructGlobalGraph(datasetpath);
		
		//构建索引
		//String labeltag = "http://www.w3.org/2000/01/rdf-schema#label";
		GraphIndexCreation indexCreation  = new GraphIndexCreation(graphConstruction.graphDb);	
		indexCreation.createIndex();
		
		//查询
		String searchkey = "http://dbpedia.org/resource/Aristotle";
		GraphSearch graphsearch = new GraphSearch(Global_DB_PATH);
		ArrayList<Node> result = graphsearch.searchNode("uri", searchkey, "nodes");
		for(Node node : result) {
			System.out.println(node);
		}
		
		//关闭
		graphConstruction.shutDown();	
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
