package neo4j.construction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import neo4j.construction.datastruct.GraphDBInitial;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

@SuppressWarnings("deprecation")
public class GraphSearch implements GraphDBInitial{

	private GraphDatabaseService graphDb;
		
	public GraphSearch(GraphDatabaseService graphDb) {
		
		this.setGraphDB(graphDb);
		registerShutdownHook(graphDb);
	}	
	
	public GraphSearch(String db_path) {
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( db_path ); 
		registerShutdownHook( graphDb );
	}

	private static void registerShutdownHook( final GraphDatabaseService graphDb ) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
	    Runtime.getRuntime().addShutdownHook( new Thread() {
	        @Override
	        public void run(){
	            graphDb.shutdown();
	        }
	    });
	}
	
	public void shutDown() {
	    System.out.println();
	    System.out.println( "Shutting down database ..." );
	    // START SNIPPET: shutdownServer
	    graphDb.shutdown();
	    // END SNIPPET: shutdownServer
	}
	  
	/**
	 * @param key查询属性
	 * @param value查询值
	 * @param indexName 索引名称
	 * @return
	 */
	public ArrayList<Node> searchNode(String key, String value, String indexName){
		ArrayList<Node> result = new ArrayList<Node>();
		Transaction tx = graphDb.beginTx(); 
		if(!this.graphDb.index().existsForNodes(indexName)){
			System.out.println("指定名称为：" + indexName + "的索引不存在");
			return null;
		}
		Index<Node> index = this.graphDb.index().forNodes(indexName);
		ResourceIterator<Node> iterator = index.get(key, value).iterator();
		while(iterator.hasNext()){
			Node cur = iterator.next();
			result.add(cur);
			System.out.println("找到节点" + cur + " 对应 value为" + cur.getProperty(key));
		}
		tx.success();
		tx.finish();
		if(result.size() == 0)
			System.out.println("满足条件到结果为空！");
		return result;
	}
	
	public boolean nodeExistInGraph(String nodeURI) {
		boolean exist = false;
		ArrayList<Node> nodes = searchNode("uri", nodeURI, "nodes");
		if(nodes.size() > 0) {
			exist = true;
		}
		return exist;
	}
	
	public int getEdgeOccurrence(String edgeURI) {
		Transaction tx = graphDb.beginTx();
		Index<Node> props = this.graphDb.index().forNodes("properties");
		int occurrence = props.get("property", edgeURI).size();
		
		Index<Relationship> relations = this.graphDb.index().forRelationships("edges");
		occurrence += relations.get("relation_label", edgeURI).size();
		
		tx.success();
		tx.finish();		
		return occurrence;
	}
	
	public ArrayList<Statement> generateCandidates(String nodeURI) {
		ArrayList<Statement> candidates = new ArrayList<Statement>();
		Resource nodeRes = ResourceFactory.createResource(nodeURI);
		ArrayList<Node> result = searchNode("uri", nodeURI, "nodes");
		Transaction tx = graphDb.beginTx() ; 
		//ATTENTION: Here we suppose the first node is the initial node without ambiguation
		Node start = result.get(0);
		// Add data properties
		Map<String, Object> properties = start.getAllProperties();
		for(String relation : properties.keySet()) {
			if(!relation.equals("uri")) {
				Property predicate = ResourceFactory.createProperty(relation);
				Literal object = ResourceFactory.createPlainLiteral(properties.get(relation).toString());
				Statement triple = ResourceFactory.createStatement(nodeRes, predicate, object);
				candidates.add(triple);
			}
		}		
		// Add object properties
		Traverser neighborInPaths = getLinkedPaths(start, Direction.INCOMING, 1);
		for(Path path : neighborInPaths) {
			String pathName = path.lastRelationship().getProperties("relation_label").get("relation_label").toString();
			String neighbor = path.endNode().getProperty("uri").toString();
			Resource subject = ResourceFactory.createResource(neighbor);
			Property predicate = ResourceFactory.createProperty(pathName);
			Statement triple = ResourceFactory.createStatement(subject, predicate, nodeRes);
			candidates.add(triple);
		}
		Traverser neighborOutPaths = getLinkedPaths(start, Direction.OUTGOING, 1);
		for(Path path : neighborOutPaths) {
			String pathName = path.lastRelationship().getProperties("relation_label").get("relation_label").toString();
			String neighbor = path.endNode().getProperty("uri").toString();
			Property predicate = ResourceFactory.createProperty(pathName);
			Resource object = ResourceFactory.createResource(neighbor);
			Statement triple = ResourceFactory.createStatement(nodeRes, predicate, object);
			candidates.add(triple);
		}
		tx.success();
		tx.finish();
		return candidates;
	}
	
	public ArrayList<Statement> getNodeTree(String rootNodeURI, int depth) {
		ArrayList<Statement> nodeTree = new ArrayList<Statement>();
		ArrayList<Node> result = searchNode("uri", rootNodeURI, "nodes");
		if(result.isEmpty()) {
			System.out.println("Node does not exist.");
			//shutDown();
			return nodeTree;
		}
		Transaction tx = graphDb.beginTx() ; 
		//ATTENTION: Here we suppose the first node is the initial node without ambiguation
		Node start = result.get(0);
		Traverser traverser = getLinkedPaths(start, Direction.BOTH, depth);
		for(Path path : traverser) {
			// Add object properties
			Iterable<Relationship> relationships = path.relationships();
			Iterator<Relationship> relationsIter = relationships.iterator();
			while(relationsIter.hasNext()) {
				Relationship relationship = relationsIter.next();
				String startNode = relationship.getStartNode().getProperty("uri").toString();
				Resource subject = ResourceFactory.createResource(startNode);
				String relation = relationship.getProperty("relation_label").toString();
				Property predicate = ResourceFactory.createProperty(relation);
				String endNode = relationship.getEndNode().getProperty("uri").toString();
				Resource object = ResourceFactory.createResource(endNode);
				Statement triple = ResourceFactory.createStatement(subject, predicate, object);
				if(!nodeTree.contains(triple)) {
					nodeTree.add(triple);
				}
			}
			// Add data properties
			Iterable<Node> nodes = path.nodes();
			Iterator<Node> nodesIter = nodes.iterator();
			while(nodesIter.hasNext()) {
				Node node = nodesIter.next();
				Resource nodeRes = ResourceFactory.createResource(node.getProperty("uri").toString());
				Map<String, Object> properties = node.getAllProperties();
				for(String relation : properties.keySet()) {
					if(!relation.equals("uri")) {
						Property predicate = ResourceFactory.createProperty(relation);
						Literal object = ResourceFactory.createPlainLiteral(properties.get(relation).toString());
						Statement triple = ResourceFactory.createStatement(nodeRes, predicate, object);
						if(!nodeTree.contains(triple)) {
							nodeTree.add(triple);
						}
					}
				}		
			}
		}
		tx.success();
		tx.finish();
		//shutDown();
		return nodeTree;
	}

	//从某个节点进行 遍历
	public Traverser getLinkedPaths(final Node start, Direction direction, int depth) {
	    TraversalDescription td = Traversal.description().breadthFirst().
	    relationships(RelTypes.RELATION, direction).evaluator(Evaluators.excludeStartPosition()).evaluator(Evaluators.toDepth(depth));
	    return td.traverse(start);
	}
	
	//从某个节点进行 遍历
	public Traverser getInNeighbors(final Node person) {
	    TraversalDescription td = Traversal.description().breadthFirst().relationships(RelTypes.RELATION, Direction.INCOMING).evaluator(Evaluators.excludeStartPosition());
	    return td.traverse(person);
	}
  
	// 打印从某个节点遍历到的节点
	public void printNodeFriends(Node node, String properyName) {
		Transaction tx = graphDb.beginTx() ;  
	    int friendsNumbers = 0;
	    System.out.println(node.getProperty(properyName) + "'s friends:");
	    for(Path friendPath : getLinkedPaths(node, Direction.BOTH, 3)) {
	        System.out.println("At depth " + friendPath.length() + " => " + friendPath.endNode().getProperty(properyName));
	        friendsNumbers++;
	    }
	    System.out.println("Number of friends found: " + friendsNumbers);
	    tx.success();
	    tx.finish();
	}
	
	//最短路径,给定最大长度 h
	public Iterable<Path> findShortestPath(Node node1, Node node2, int h) {
	    PathFinder<Path> finder = GraphAlgoFactory.shortestPath(Traversal.expanderForTypes(RelTypes.RELATION, Direction.BOTH), h);
	    Iterable<Path> paths = finder.findAllPaths(node1, node2);
	    return paths;
	}
	
	//打印某两个节点之间的最短路径
	public void printShortestPaths(Node node1, Node node2, int maxLen) {
		Transaction tx = graphDb.beginTx() ;  
	    for(Path shortestPath: findShortestPath(node1, node2, maxLen)) {
	        System.out.println(shortestPath.toString());
	    }
	    tx.success();
	    tx.finish();
	}
	
	public static void main(String[] args) {
		String db_path = "C:\\Users\\huangchao\\Documents\\Neo4j\\whynot.runtime.graphdb";
		String labeltag = "uri";	//索引建立在label属性上
		String indexName = "nodes";		//已经建立的索引名称
		int h = 5;	//最大搜索长度
		GraphSearch graphsearch = new GraphSearch(db_path);
		
		//查询满足条件的节点
		String searchkey = "http://dbpedia.org/resource/Avicenna";
		ArrayList<Node> result = graphsearch.searchNode(labeltag, searchkey, indexName);		
		//打印每个节点相邻路节点
		for(Node node : result) {
			graphsearch.printNodeFriends(node, labeltag);
		}
		
		//满足条件的节点之间的最短路径	
		String s1 = "http://dbpedia.org/resource/Avicenna", s2 = "http://dbpedia.org/resource/Nicholas_of_Cusa";
		Node n1 = graphsearch.searchNode(labeltag, s1, indexName).get(0);
		Node n2 = graphsearch.searchNode(labeltag, s2, indexName).get(0);
		graphsearch.printShortestPaths(n1, n2, h);
		
		graphsearch.shutDown();
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
