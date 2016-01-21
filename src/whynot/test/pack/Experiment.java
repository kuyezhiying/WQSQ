package whynot.test.pack;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import lucene.api.pack.LuceneAPI;
import match.api.pack.GraphMatch;
import match.api.pack.TriplePair;
import match.api.pack.UserQuery;
import neo4j.construction.GraphConstruction;
import neo4j.construction.index.GraphIndexCreation;

import com.hp.hpl.jena.rdf.model.Statement;

public class Experiment {
	
	private final static String db_path = "C:\\Users\\huangchao\\Documents\\Neo4j\\whynot.runtime.graphdb";

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		
		String sparql21 = "PREFIX dbo:  <http://dbpedia.org/ontology/> PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbpr: <http://dbpedia.org/resource/> SELECT ?var1 WHERE {	?var1 dbpp:era dbpr:Medieval_philosophy . ?var2 dbpp:influenced ?var1 .  ?var2 dbpp:name \"Aristotle\"@en . ?var2 dbpp:nationality ?var3. ?var3 dbpp:languages dbpr:Greek_language.}";	
		String sparql20 = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbpr: <http://dbpedia.org/resource/> SELECT ?p WHERE {	?p dbpp:era dbpr:Ancient_philosophy . ?p dbpp:mainInterests dbpr:Natural_philosophy . }";	//\"Avicenna\"@en
		String sparql19 = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbpr: <http://dbpedia.org/resource/> SELECT ?var1 WHERE {	?var1 dbpp:era dbpr:Medieval_philosophy .	?var2 dbpp:influenced ?var1 .	?var2 dbpp:name \"Avicenna\"@en .}";	//\"Avicenna\"@en
		String sparql18 = "PREFIX res: <http://dbpedia.org/resource/> PREFIX dbo: <http://dbpedia.org/ontology/> SELECT DISTINCT ?uri WHERE {         res:The_Three_Dancers dbo:author ?person .         ?person dbo:movement ?uri .}";
		//String sparql = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX res: <http://dbpedia.org/resource/> SELECT DISTINCT ?uri WHERE {         res:The_Three_Dancers dbpp:artist ?person .         ?person dbpp:movement ?uri .}";
		//String sparql = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX dbpp: <http://dbpedia.org/property/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT * WHERE {        ?uri rdf:type dbo:Band .        ?uri dbpp:genre res:Metalcore .       ?uri dbpp:origin ?h .  ?h dbpp:countryCode \"USA\"@en  }";
		String sparql17 = "PREFIX dbpp: <http://dbpedia.org/property/>  PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri WHERE {        ?uri rdf:type dbo:Band .        ?uri dbpp:genre res:Metalcore .       ?uri dbo:hometown ?h .   ?h dbpp:country res:Australia . }";
		String sparql16 = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX res: <http://dbpedia.org/resource/> SELECT DISTINCT ?uri WHERE {        res:Carrot_cake dbpp:ingredient ?uri .}";
		String sparql15 = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> SELECT DISTINCT ?uri WHERE {        ?uri dbpp:spouse res:Tom_Cruise.}";
		String sparql14 = "PREFIX dbp: <http://dbpedia.org/property/> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> SELECT DISTINCT ?uri WHERE {         res:Himalayas dbp:country ?country .          ?country dbo:capital ?uri .}";
		String sparql13 = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri WHERE {        ?uri rdf:type dbo:Actor .        ?uri dbo:birthPlace res:Berlin .}";
		String sparql12 = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX res: <http://dbpedia.org/resource/> SELECT DISTINCT ?uri WHERE {        ?uri dbpp:field res:Oceanography .         ?uri dbpp:birthPlace res:Sweden .        }";
		String sparql11 = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri WHERE {        ?uri rdf:type dbo:PoliticalParty .        ?uri dbo:country res:Netherlands .}";
		String sparql10 = "PREFIX res: <http://dbpedia.org/resource/> PREFIX dbp: <http://dbpedia.org/property/> SELECT DISTINCT ?uri WHERE {        res:Pope_John_Paul_II dbp:successor ?uri .}";
		String sparql9 = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX dbp: <http://dbpedia.org/property/> PREFIX res: <http://dbpedia.org/resource/> SELECT DISTINCT ?uri WHERE {        ?uri dbp:launchPad res:Baikonur_Cosmodrome. }";
		String sparql8 = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri WHERE { ?uri rdf:type dbo:River . ?x dbo:inflow ?uri . ?x rdf:type dbo:Lake . ?x dbo:country res:Germany .}";
		String sparql7 = "PREFIX dbo:  <http://dbpedia.org/ontology/> PREFIX res:  <http://dbpedia.org/resource/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT DISTINCT ?uri WHERE {        ?uri foaf:surname \"Baldwin\"@en .         ?uri dbo:occupation res:Actor . }";
		String sparql6 = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri WHERE {        ?uri rdf:type dbo:Astronaut .        ?uri dbo:nationality res:China .}";
		String sparql5 = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX prop: <http://dbpedia.org/property/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri WHERE {        ?uri rdf:type dbo:Writer .       ?uri prop:awards res:Nobel_Prize_in_Literature . }";
		String sparql4 = "PREFIX dbo: <http://dbpedia.org/property/> SELECT DISTINCT ?uri WHERE {        ?uri dbo:nationality \"Japanese\"@en .         ?uri dbo:deathCause \"stomach cancer\"@en . }";
		String sparql3 = "PREFIX dbo: <http://dbpedia.org/property/> PREFIX res: <http://dbpedia.org/resource/> SELECT DISTINCT ?uri WHERE {        ?uri dbo:currency res:West_African_CFA_franc . }";
		String sparql2 = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX res: <http://dbpedia.org/resource/> SELECT ?uri WHERE {       res:Johann_Sebastian_Bach dbpp:placeOfBirth ?uri .}";
		String sparql = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri WHERE { ?uri rdf:type dbo:Animal . ?uri dbo:conservationStatus \"EX\" . }";
		System.out.println("================================PRE PROCESS======================================");
		
		String keyword = "numbat";	//aristotle Katie Holmes
		String indexPath = "D:/workspace/WhyNot/index_ooi_name_label";
		LuceneAPI lucene = new LuceneAPI(indexPath);
		List<String> uriList = lucene.SearchIndex(keyword, "object");
		
		
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( db_path );
		GraphConstruction graphConstruction = new GraphConstruction(graphDb);			
		long endTime1 = System.currentTimeMillis();
		graphConstruction.construct(keyword, sparql);
		System.out.println("Construct run-time graph costs time : " + (endTime1 - startTime) + "ms.");
		System.out.println("========================Construct Run-time Graph Successfully=====================");
		
		GraphIndexCreation indexCreation = new GraphIndexCreation(graphDb);			
		indexCreation.createIndex();
		long endTime2 = System.currentTimeMillis();
		System.out.println("Create node index costs time : " + (endTime2 - endTime1) + "ms.");
		System.out.println("==========================Create Index Sucessfully================================");
		
		
		UserQuery userQuery = new UserQuery();
		ArrayList<Statement> queryPatterns = userQuery.triplesStorage(sparql);
		ArrayList<Statement> queryPatternsInOrder = userQuery.getQueryPatternsInOrder(sparql, graphDb);
		int depth = userQuery.variablesExtract(sparql).size();
		long endTime3 = System.currentTimeMillis();
		System.out.println("Pre process costs time : " + (endTime3 - startTime) + "ms.");
		System.out.println("===========================PRE PROCESS Finished====================================");				
				
		
		GraphMatch graphMatch = new GraphMatch(graphDb);
		for(String possibleEntityURI : uriList) {
			System.out.println(possibleEntityURI);
			ArrayList<TriplePair> answerGraph = graphMatch.getAnswerGraph(queryPatterns, possibleEntityURI);
			ArrayList<Statement> whynotQuery = graphMatch.constructWhyNotQuery(answerGraph);
			String whynotSparql = userQuery.constructWhyNotSparql(whynotQuery);
			float score = 0;
			for(TriplePair pair : answerGraph) {
				System.out.println(pair.getTarget());
				score += pair.getSimilarity();
			}
			score /= answerGraph.size();
			System.out.println("match score : " + score);
			System.out.println("why not query : ");
			System.out.println(whynotSparql);
			for(Statement stmt : whynotQuery) {
				System.out.println(stmt);
			}
			//ArrayList<Statement> candidates = graphMatch.generateCandidates(startNodes.get(0).getProperty("uri").toString());
			//ArrayList<Statement> answerGraph = graphMatch.matchQueryWithOrder(queryPatterns, candidates);
		}
		long endTime4 = System.currentTimeMillis();
		System.out.println("Get answer costs time : " + (endTime4 - endTime3) + "ms.");
		System.out.println("============================First Algorithm Finished================================");
		
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
		long endTime5 = System.currentTimeMillis();
		//graphMatch.shutDownDB();
		graphDb.shutdown();
		System.out.println("Get answer (with order) costs time : " + (endTime5 - endTime4) + "ms.");
		System.out.println("============================Second Algorithm Finished===============================");
		System.out.println("Total process cost time : " + (endTime5 - startTime) + "ms.");
	}

}
