package match.api.pack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lucene.api.pack.LuceneAPI;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import neo4j.construction.GraphConstruction;
import neo4j.construction.GraphSearch;
import neo4j.construction.datastruct.GraphDBInitial;
import neo4j.construction.index.GraphIndexCreation;

import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;


public class GraphMatch  implements GraphDBInitial{
	
	private static Connection conn = null;
	GraphSearch graphsearch;
	static GraphDatabaseService graphDb;
	public GraphMatch(GraphDatabaseService graphDb) {
		super();
		this.setGraphDB(graphDb);
		this.graphsearch = new GraphSearch(graphDb);
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
	
	public ResultSet queryRuntimeGraph(String query) {
		// Get connection
		conn = getConnection();
		ResultSet rs = null;	
		//String query = "MATCH (:Movie {title:{1}})<-[:ACTED_IN]-(a:Person) RETURN a.name as actor";
		//String query2 = "MATCH (nineties:Movie) WHERE nineties.released > 1990 AND nineties.released < 2000 RETURN nineties.title as actor";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {	
		    stmt.setString(1,"The Matrix");	
		    rs = stmt.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
		


	public GraphMatch(GraphSearch graphsearch) {
		super();
		this.graphsearch = graphsearch;
	}

	public static float computeSimilarity(Statement pattern, Statement candidate) {
		float similarity = 0;
		similarity = (getEditDistance(pattern.getSubject().toString(), candidate.getSubject().toString()) + 
				getEditDistance(pattern.getPredicate().toString(), candidate.getPredicate().toString()) + 
				getEditDistance(pattern.getObject().toString(), candidate.getObject().toString())) / 3;
		return similarity;
	}
	
	public static float getEditDistance(String srcStr, String dstStr) {//Alain Connes@en
		String prefix = "http://dbpedia.org/";
		if(srcStr.contains(prefix)) {
			srcStr = srcStr.substring(28);		// 28 is the length of "http://dbpedia.org/resource/" or "http://dbpedia.org/property/" or "http://dbpedia.org/ontology/"
		}
		if(dstStr.contains(prefix)) {
			dstStr = dstStr.substring(28);
		}
		int srcLen = srcStr.length();
		int desLen = dstStr.length();
		//use one dimension array to represent two dimension
		int[] distance = new int[(srcLen + 1) * (desLen + 1)];
		//Initializing...
		for(int i = 0;i <= srcLen;i++)
			distance[i * (desLen + 1)] = i;		//assign the value of the first column
		for(int i = 0;i <= desLen;i++) {
			distance[i] = i;					//assign the value of the first row
		}
		//Dynamic Programming...
		for(int i = 1;i <= srcLen;i++) {
			for(int j = 1; j <= desLen; j++) {
				int modifyDis = distance[(i - 1) * (desLen + 1) + (j - 1)] + (srcStr.charAt(i - 1) == dstStr.charAt(j - 1) ? 0 : 1);
				int addDis = distance[(i - 1) * (desLen + 1) + j] + 1;
				int deleteDis = distance[i * (desLen + 1) + (j - 1)] + 1;
				int tempDis = modifyDis < addDis ? modifyDis : addDis;
				distance[i * (desLen + 1) + j] = tempDis < deleteDis ? tempDis : deleteDis;
			}
		}
		float similarity = 1 - (float) distance[srcLen * (desLen + 1) + desLen] / Math.max(srcLen, desLen); 
		return similarity;
	}
	
	public ArrayList<TriplePair> matchQuery(ArrayList<Statement> queryPatterns, Node startNode) {
		
		ArrayList<Statement> candidates = graphsearch.generateCandidates(startNode.getProperty("uri").toString());
		ArrayList<TriplePair> matchTriples = new ArrayList<TriplePair>();
		for(Statement pattern : queryPatterns) {
			float score = 0;
			int index = 0, currentIndex = 0, patternsNum = queryPatterns.size();
			for(Statement candidate : candidates) {
				float simScore = computeSimilarity(pattern, candidate);
				if(simScore > score) {
					score = simScore;
					index = currentIndex;
				}
				currentIndex++;
			}
			TriplePair pair = new TriplePair(pattern, candidates.get(index), score / patternsNum);
			matchTriples.add(pair);
		}	
		return matchTriples;
	}
	
	public TriplePair getMatchPairInMaxScore(ArrayList<Statement> queryPatterns, ArrayList<Statement> candidates) {
		ArrayList<TriplePair> matchTriples = new ArrayList<TriplePair>();
		float maxScore = 0;
		int maxScoreIndex = 0, currentMaxScoreIndex = 0;
		for(Statement pattern : queryPatterns) {
			float score = 0;
			int index = 0, currentIndex = 0;
			for(Statement candidate : candidates) {
				float simScore = computeSimilarity(pattern, candidate);
				if(simScore > score) {
					score = simScore;
					index = currentIndex;
				}
				currentIndex++;
			}
			TriplePair pair = new TriplePair(pattern, candidates.get(index), score);
			matchTriples.add(pair);
			if(pair.getSimilarity() > maxScore) {
				maxScore = pair.getSimilarity();
				maxScoreIndex = currentMaxScoreIndex;
			}
			currentMaxScoreIndex++;
		}
		return matchTriples.get(maxScoreIndex);
	}
	
	public ArrayList<TriplePair> getAnswerGraph(ArrayList<Statement> patternsCollection, String startNodeURI) {
		ArrayList<TriplePair> answerGraph = new ArrayList<TriplePair>();
		ArrayList<Statement> queryPatterns = new ArrayList<Statement>();
		for(Statement stmt : patternsCollection) {
			queryPatterns.add(stmt);
		}
		if(!graphsearch.nodeExistInGraph(startNodeURI)) {
			System.out.println("Can not match.");
			//graphsearch.shutDown();
			return answerGraph;
		}
		ArrayList<Statement> candidates = graphsearch.generateCandidates(startNodeURI);
		while(!queryPatterns.isEmpty()) {
			//1. select a query pattern to match and generate subgraph
			TriplePair matchPair = getMatchPairInMaxScore(queryPatterns, candidates);
			answerGraph.add(matchPair);
			//2. expand candidates set and select a new pattern to match
			if(!startNodeURI.equals(matchPair.getTarget().getSubject().toString())) {
				ArrayList<Statement> newCandidates = graphsearch.generateCandidates(matchPair.getTarget().getSubject().toString());
				for(Statement stmt : newCandidates) {
					if(!candidates.contains(stmt)) {
						candidates.add(stmt);
					}
				}
			}
			if(matchPair.getTarget().getObject().isResource() && !(startNodeURI.equals(matchPair.getTarget().getObject().toString()))) {
				ArrayList<Statement> newCandidates = graphsearch.generateCandidates(matchPair.getTarget().getObject().toString());
				for(Statement stmt : newCandidates) {
					if(!candidates.contains(stmt)) {
						candidates.add(stmt);
					}
				}
			}
			queryPatterns.remove(matchPair.getSource());
			candidates.remove(matchPair.getTarget());
		}
		//graphsearch.shutDown();
		return answerGraph;
	}

	public ArrayList<TriplePair> matchQueryWithOrder(ArrayList<Statement> queryPatterns, String rootNodeURI, int depth) {
		ArrayList<TriplePair> subGraph = new ArrayList<TriplePair>();	
	
		ArrayList<Statement> candidates = graphsearch.getNodeTree(rootNodeURI, depth);
		if(candidates.isEmpty()) {
			System.out.println("Tree of node {" + rootNodeURI + "} is empty.");
			return subGraph;
		}
		for(Statement pattern : queryPatterns) {
			float maxScore = 0;
			int index = 0, currentIndex = 0, patternsNum = queryPatterns.size();
			for(Statement candidate : candidates) {
				float score = computeSimilarity(pattern, candidate);
				if(score > maxScore) {
					maxScore = score;
					index = currentIndex;
				}
				currentIndex++;
			}
			TriplePair pair = new TriplePair(pattern, candidates.get(index), maxScore / patternsNum);
			subGraph.add(pair);
			//candidates.remove(index);
		}
		return subGraph;
	}
	
	public ArrayList<Statement> constructWhyNotQuery(ArrayList<TriplePair> answerGraph) {
		ArrayList<Statement> whynotQuery = new ArrayList<Statement>();
		for(TriplePair pair : answerGraph) {
			Statement queryPattern = pair.getSource();
			Statement matchPattern = pair.getTarget();
			Statement newPattern = null;
			if(queryPattern.getSubject().toString().startsWith("?") && queryPattern.getObject().toString().startsWith("?")) {
				newPattern = ResourceFactory.createStatement(queryPattern.getSubject(), matchPattern.getPredicate(), queryPattern.getObject());				
			} else if(queryPattern.getSubject().toString().startsWith("?")) {
				newPattern = ResourceFactory.createStatement(queryPattern.getSubject(), matchPattern.getPredicate(), matchPattern.getObject());
				
			} else if(queryPattern.getObject().toString().startsWith("?")) {
				newPattern = ResourceFactory.createStatement(matchPattern.getSubject(), matchPattern.getPredicate(), queryPattern.getObject());
			}
			whynotQuery.add(newPattern);
		}
		return whynotQuery;
	}
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		
		String sparql = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbpr: <http://dbpedia.org/resource/> SELECT ?var1 WHERE {	?var1 dbpp:era dbpr:Medieval_philosophy .	?var2 dbpp:influenced ?var1 .	?var2 dbpp:nationality dbpr:Persian_people .}";	//\"Avicenna\"@en
		//String query = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri WHERE { ?uri rdf:type dbo:Animal . ?uri dbo:conservationStatus \"EX\" . }";
		System.out.println("========================================================");
		
		String keyword = "aristotle";
		String indexPath = "D:/workspace/WhyNot/index_label_lucene3";
		LuceneAPI lucene = new LuceneAPI(indexPath);
		List<String> uriList = lucene.SearchIndex(keyword, "object");
		long endTime0 = System.currentTimeMillis();
		System.out.println("Pre process costs time : " + (endTime0 - startTime) + "ms.");
		System.out.println("========================================================");				
		
		
		GraphConstruction graphConstruction = new GraphConstruction(graphDb);
		graphConstruction.construct(keyword, sparql);
		long endTime1 = System.currentTimeMillis();
		System.out.println("Construct run-time graph costs time : " + (endTime1 - startTime) + "ms.");
		
		GraphIndexCreation indexCreation = new GraphIndexCreation(graphDb);			
		indexCreation.createIndex();
		long endTime2 = System.currentTimeMillis();
		System.out.println("Create node index costs time : " + (endTime2 - endTime1) + "ms.");
		
		
		UserQuery userQuery = new UserQuery();
		ArrayList<Statement> queryPatterns = userQuery.triplesStorage(sparql);
		GraphMatch graphMatch = new GraphMatch(graphDb);
		for(String possibleEntityURI : uriList) {
			System.out.println(possibleEntityURI);
			ArrayList<TriplePair> answerGraph = graphMatch.getAnswerGraph(queryPatterns, possibleEntityURI);
			ArrayList<Statement> whynotQuery = graphMatch.constructWhyNotQuery(answerGraph);
			float score = 0;
			for(TriplePair pair : answerGraph) {
				System.out.println(pair.getTarget());
				score += pair.getSimilarity();
			}
			System.out.println("match score : " + score);
			System.out.println("why not query : ");
			for(Statement stmt : whynotQuery) {
				System.out.println(stmt);
			}
			//ArrayList<Statement> candidates = graphMatch.generateCandidates(startNodes.get(0).getProperty("uri").toString());
			//ArrayList<Statement> answerGraph = graphMatch.matchQueryWithOrder(queryPatterns, candidates);
		}
		long endTime3 = System.currentTimeMillis();
		System.out.println("Get answer costs time : " + (endTime3 - startTime) + "ms.");
		System.out.println("========================================================");
		
		ArrayList<Statement> queryPatternsInOrder = userQuery.getQueryPatternsInOrder(sparql, graphDb);
		int depth = userQuery.variablesExtract(sparql).size();
		for(String possibleEntityURI : uriList) {
			System.out.println(possibleEntityURI);
			ArrayList<TriplePair> answerGraphInOrder = graphMatch.matchQueryWithOrder(queryPatternsInOrder, possibleEntityURI, depth);
			float score = 0;
			for(TriplePair pair : answerGraphInOrder) {
				System.out.println(pair.getTarget());
				score += pair.getSimilarity();
			}
			System.out.println("match score : " + score);
			//ArrayList<Statement> candidates = graphMatch.generateCandidates(startNodes.get(0).getProperty("uri").toString());
			//ArrayList<Statement> answerGraph = graphMatch.matchQueryWithOrder(queryPatterns, candidates);
		}
		long endTime4 = System.currentTimeMillis();
		System.out.println("Get answer (with order) costs time : " + (endTime4 - endTime3) + "ms.");
		System.out.println("Total process cost time : " + (endTime4 - startTime) + "ms.");
	}

	@Override
	public void setGraphDB(GraphDatabaseService db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDownDB() {
		 System.out.println();
		    System.out.println( "Shutting down database ..." );
		    // START SNIPPET: shutdownServer
		    graphDb.shutdown();
		    // END SNIPPET: shutdownServer
		
	}

}
