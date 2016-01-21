package match.api.pack;

import generation.api.pack.TriplesGeneration;
import generation.api.pack.WhyNotTriples;
import similarity.api.pack.CombinationSimilarity;
import similarity.api.pack.LexiconComputation;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

public class TriplesMatch {
	
	//match user query triple in generalized why not triples collection
	public Map<Statement, String> match(ArrayList<Statement> queryTriples, ArrayList<Statement> questionTriples) {
		Map<Statement, String> matchedMap = new HashMap<Statement, String>();
		Map<String, String> stmtsMap = constructNodeLM(queryTriples, questionTriples);
		//Similarity strucSim = SimilarityFactory.createSimilarityMethod("structure");		//choose the structure similarity method to compute
		//Similarity lexiSim = SimilarityFactory.createSimilarityMethod("lexicon");			//choose the lexicon similarity method to compute
		//Similarity combiSim = SimilarityFactory.createSimilarityMethod("combination");		//choose the combination similarity method to compute
		CombinationSimilarity combiSim = new CombinationSimilarity();
		float maxScore = 0;	
		int flag = 0;
		for(Statement query : queryTriples) {	//for each triple in user query, compute its similarity with all triples in generalized why not triples collection
			for(Statement question : questionTriples) {
				//float score = (float)(lexiSim.computeSimilarity(query, question) + strucSim.computeSimilarity(query, question)) / 2 ;		//compute similarity score
				float score = combiSim.computeSimilarity(query, question, stmtsMap);
				if(score > maxScore) {
					maxScore = score;			//the max similarity score between triples
					flag = questionTriples.indexOf(question);				//flag of the most similar trile
				}
			}
			if(maxScore == 0) {
				//continue;
				matchedMap.put(query, null);		//there are no triples in why not triples collection match the user query triple
			}
			Statement stmt = questionTriples.get(flag);
			matchedMap.put(query, stmt.toString() + "\t" + maxScore);	//correspond the user query triple with its most similar triple
			questionTriples.remove(stmt);	//remove the most similar triple that has matched
			maxScore = 0;	//clear the max score
		}
		return matchedMap;
	}
	
	public Map<String, String> constructNodeLM(ArrayList<Statement> queryTriples, ArrayList<Statement> questionTriples){
		Map<String, String> nodeLMMap = new HashMap<String, String>();
		LexiconComputation lexiconCompu = new LexiconComputation();
		for(Statement stmt : queryTriples) {
			String sub = stmt.getSubject().toString();
			String prop = stmt.getPredicate().toString();
			RDFNode obj = stmt.getObject();
			if(!sub.startsWith("?") && !nodeLMMap.containsKey(sub)) {
				nodeLMMap.put(sub, String.valueOf(lexiconCompu.constructEntityLM(sub)));
			}
			if(!prop.startsWith("?") && !nodeLMMap.containsKey(prop)) {
				nodeLMMap.put(prop, String.valueOf(lexiconCompu.constructRelationLM(prop)));
			}
			if(obj.isResource()) {
				String object = obj.toString();
				if(!object.startsWith("?") && !nodeLMMap.containsKey(obj)) {
					nodeLMMap.put(stmt.getObject().toString(), String.valueOf(lexiconCompu.constructEntityLM(stmt.getObject().toString())));
				}
			}
		}
		for(Statement stmt : questionTriples) {
			String sub = stmt.getSubject().toString();
			String prop = stmt.getPredicate().toString();
			RDFNode obj = stmt.getObject();
			if(!sub.startsWith("?") && !nodeLMMap.containsKey(sub)) {
				nodeLMMap.put(sub, String.valueOf(lexiconCompu.constructEntityLM(sub)));
			}
			if(!prop.startsWith("?") && !nodeLMMap.containsKey(prop)) {
				nodeLMMap.put(prop, String.valueOf(lexiconCompu.constructRelationLM(prop)));
			}
			if(obj.isResource()) {
				String object = obj.toString();
				if(!object.startsWith("?") && !nodeLMMap.containsKey(obj)) {
					nodeLMMap.put(stmt.getObject().toString(), String.valueOf(lexiconCompu.constructEntityLM(stmt.getObject().toString())));
				}
			}
		}
		return nodeLMMap;
	}
	
	//output the map after matching
	public void printMap(Map<Statement, String> map) {
		Iterator<Statement> it = map.keySet().iterator();	//all user query triples
		while(it.hasNext()) {
			Statement key = (Statement)it.next();	//user query triple
			String value = map.get(key);			//the most similar triple in why not triples collection
			System.out.println(key + "\t" + value);
		}
	}
	
	//output the triples collection
	public void printStatement(ArrayList<Statement> array) {
		for(Statement stmt : array) {
			System.out.println(stmt);
		}
	}
	
	public void main(String[] args) {
		String uri = "http://dbpedia.org/resource/Aristotle";		 //http://dbpedia.org/ontology/grammyAward
		WhyNotTriples whynotTriples = new WhyNotTriples();
		ArrayList<Statement> questionTriples = whynotTriples.GenerateTriples(uri);
		//Generation
		TriplesGeneration triplesGeneration = new TriplesGeneration();
		ArrayList<Statement> generalizedTriples = triplesGeneration.generalizeTriplesWithPellet(questionTriples);  //generalize the nodes in the map		
		printStatement(generalizedTriples);

		UserQuery userQuery = new UserQuery();
		String sparql = "PREFIX dbpedia-owl: <http://dbpedia.org/resource/> SELECT * where {dbpedia-owl:Aristotle ?p ?o.}";
		ArrayList<Statement> queryTriples = userQuery.triplesStorage(sparql);
		printStatement(queryTriples);
		
		Map<Statement, String> map = match(queryTriples, generalizedTriples);
		printMap(map);
	}
}
