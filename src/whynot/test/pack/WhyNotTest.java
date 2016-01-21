package whynot.test.pack;

import generation.api.pack.TriplesGeneration;
import generation.api.pack.WhyNotTriples;
import jena.api.pack.JenaAPI;
import lucene.api.pack.LuceneAPI;
import match.api.pack.TriplesMatch;
import match.api.pack.UserQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.hp.hpl.jena.rdf.model.Statement;

public class WhyNotTest {
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		//path of TDB
		String datasetpath ="D:\\workspace\\JenaTest\\tdb";
		//path of index
		String indexpath ="D:\\workspace\\JenaTest\\index";
		//path of ontlogy file
		String filePath = "D:\\workspace\\JenaTest\\data\\info_2m.nt";
		//why-not keyword
		String keyword = "Chalcis";	//Aristotle
		String keyword1 = "aristotle";
		String keyword2 = "ANSI";
		String keyword3 = "Anaximander";//Socrates
		String keyword4 = "George Washington";
		String keyword5 = "Bayerische Motoren Werke AG";
		String keyword6 = "Alan Turing";
		String keyword7 = "Akira Kurosawa";
		String keyword8 = "Aristotle";
		//user query
		String sparql = "PREFIX dbpedia-owl: <http://dbpedia.org/resource/> SELECT * WHERE { ?p <http://dbpedia.org/property/mainInterests> dbpedia-owl:Natural_philosophy .}";	//Andorra     <http://dbpedia.org/ontology/grammyAward>
		String sparql1 = "PREFIX dbpedia-owl: <http://dbpedia.org/resource/> SELECT * WHERE { ?p <http://dbpedia.org/property/mainInterests> dbpedia-owl:Natural_philosophy .}";
		String sparql2 = "PREFIX dbpr: <http://dbpedia.org/resource/> PREFIX dbpp: <http://dbpedia.org/property/> SELECT ?var WHERE { ?var dbpp:headquarters dbpr:New_York_City . }";
		String sparql3 = "PREFIX dbpr: <http://dbpedia.org/resource/> PREFIX dbpp: <http://dbpedia.org/property/> SELECT ?var WHERE { ?var dbpp:influences dbpr:Arthur_Schopenhauer . }";
		String sparql4 = "PREFIX dbpr: <http://dbpedia.org/resource/> PREFIX dbpp: <http://dbpedia.org/property/> SELECT ?var WHERE { ?var dbpp:office \"President of the United States\"@en . }";
		String sparql5 = "PREFIX dbpr: <http://dbpedia.org/resource/> PREFIX dbpp: <http://dbpedia.org/property/> SELECT ?var WHERE { ?var  <http://dbpedia.org/property/products> \"Luxury cars\"@en . \n ?var <http://dbpedia.org/property/locationCountry> \"Germany\"@en . }";
		String sparql6 = "PREFIX dbpr: <http://dbpedia.org/resource/> PREFIX dbpp: <http://dbpedia.org/property/> SELECT ?var WHERE { ?var dbpp:occupation dbpr:Engineer.	?var dbpp:occupation dbpr:Mathematician.}";
		String sparql7 = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbpr: <http://dbpedia.org/resource/> SELECT ?var WHERE { ?var dbpp:occupation dbpr:Film_director.	?var dbpp:awards dbpr:Elliott_Cresson_Medal.}";
		String sparql8 = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbpr: <http://dbpedia.org/resource/> SELECT * WHERE {	?var1 dbpp:era dbpr:Medieval_philosophy .	?var2 dbpp:influenced ?var1 .	?var2 dbpp:name \"Avicenna\"@en .}";
		
		long startTime = System.currentTimeMillis();
		//1.Create TDB dataset
		//JenaAPI jena = new JenaAPI(datasetpath);
		//jena.CreateTDB(filePath);
		//jena.QueryTDB(sparql);
		
		//2.Create index
		//HashMap<Resource, RDFNode> map = jena.ListStatement("http://dbpedia.org/property/name");			
		//LuceneAPI lucene = new LuceneAPI(indexpath);
		//lucene.BuildIndex(map);
		
		//3.Find the instances of why-not keyword in object field and obtain all related triples
		WhyNotTriples whynotTriples = new WhyNotTriples();
		List<String> uris = whynotTriples.ObtainSubUri(keyword);
		for(String uri : uris) {
			System.out.println("uri of the keyword:");
			System.out.println(uri);
			ArrayList<Statement> questionTriples = whynotTriples.GenerateTriples(uri);
			//printStatement(questionTriples);
			
			//4.Generalize the triples with RDFS rules
			TriplesGeneration triplesGeneration = new TriplesGeneration();
			ArrayList<Statement> generalizedTriples = triplesGeneration.generalizeTriplesWithPellet(questionTriples); 	
			System.out.println("\nAfter Generation, the triples are :");
			printStatement(generalizedTriples);		
			
			//5.Get the query patterns of user query
			UserQuery userQuery = new UserQuery();
			ArrayList<Statement> queryTriples = userQuery.triplesStorage(sparql);
			System.out.println("\nThe triples in user query are :");
			printStatement(queryTriples);
			
			//6.Math the query patterns in generalized triples collection, find the most similar triples
			TriplesMatch triplesMatch = new TriplesMatch();
			System.out.println("\nMatching...");
			Map<Statement, String> map = triplesMatch.match(queryTriples, generalizedTriples);
			System.out.println("\nAfter matching, the most similar triples pair are :");
			printMap(map);
			
			//7.Construct why-not query with conjunction of the matched triples
			long endTime = System.currentTimeMillis();
			System.out.println("Time : " + (endTime - startTime) + "ms");
		}
	}
	
	public static void printStatement(ArrayList<Statement> stmtArray) {
		for(Statement stmt : stmtArray) {
			System.out.println(stmt);
		}
	}
	
	public static void printMap(Map<Statement, String> map) {
		Iterator<Statement> it = map.keySet().iterator();
		while(it.hasNext()) {
			Statement key = (Statement)it.next();
			String value = map.get(key);
			System.out.println(key + "\t" + value);
		}
	}

}
