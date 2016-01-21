package match.api.pack;

import java.util.*;

import org.neo4j.graphdb.GraphDatabaseService;

import neo4j.construction.GraphSearch;
import jena.api.pack.JenaAPI;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

public class UserQuery {
	
	private final String datasetpath = "D:\\workspace\\WhyNot\\tdb_onttype_ontinfobox_infoprop";
	
	//store the triples in a sparql string
	public ArrayList<Statement> triplesStorage(String sparql) {
		ArrayList<Statement> queryStmts = new ArrayList<Statement>();	//store the query triples in statement format
        Query query = QueryFactory.create(sparql);		//create a query
        List<Element> elements = ((ElementGroup)query.getQueryPattern()).getElements();		//get all triples, a triples is an element here
        for (Element elem : elements){
            if(elem instanceof ElementPathBlock){
                ElementPathBlock pathBlock = (ElementPathBlock) elem;
                for(TriplePath triple : pathBlock.getPattern().getList()){
        		    Resource subject = ResourceFactory.createResource(triple.getSubject().toString());		//format the subject of triples to Resource
        		    Property predicate= ResourceFactory.createProperty(triple.getPredicate().toString());	//format the property of triples to Predicate     
        		    String objStr = triple.getObject().toString();
        		    RDFNode object;
        		    if(objStr.startsWith("\"")) {
            		    //object = ResourceFactory.createPlainLiteral(objStr.substring(objStr.indexOf("\"") + 1, objStr.indexOf("\"", 1)));			//format the object of triples to RDFNode
        		    	object = ResourceFactory.createPlainLiteral(objStr.toString());
        		    }
        		    else {
        		    	object = ResourceFactory.createResource(triple.getObject().toString());			//format the object of triples to RDFNode
        		    }
        			Statement item = ResourceFactory.createStatement(subject, predicate, object);			//create a statement with the formatted triple nodes
        			queryStmts.add(item);		//store the statement
               }
            }
        }
		return queryStmts;
	}
	
	public int getResultSize(String query) {
		JenaAPI jena = new JenaAPI(datasetpath);
		ResultSet result = jena.QueryTDB(query);
		int size = 0;
		while(result.hasNext()) {
		    QuerySolution row= result.nextSolution();
		    String rowNumber = row.get("?count").toString();
		    size += Integer.parseInt(rowNumber.substring(0, rowNumber.indexOf("^")));
		}
		return size;
	}
	
	public Map<String, Integer> getPredicateOccurrence(String sparql) {
		Map<String, Integer> predicateOccurrenceMap = new HashMap<String, Integer>();
		ArrayList<Statement> triples = triplesStorage(sparql);
		for(Statement stmt : triples) {
			String predicate = stmt.getPredicate().toString();
			String query = "select (count(?s) as ?count) where { ?s <" + predicate + "> ?o . }";
			//System.out.println(query);
			int occurrence = getResultSize(query);
			if(!predicateOccurrenceMap.containsKey(predicate)) {
				predicateOccurrenceMap.put(predicate, occurrence);
				System.out.println(predicate + " : " + occurrence);
			}
		}
		return predicateOccurrenceMap;
	}
	
	public Map<String, Integer> getPredicateRuntimeOccurrence(String sparql, GraphDatabaseService graphDb) {
		Map<String, Integer> predicateOccurrenceMap = new HashMap<String, Integer>();
		ArrayList<Statement> triples = triplesStorage(sparql);
		GraphSearch graphsearch = new GraphSearch(graphDb);
		for(Statement stmt : triples) {
			String predicate = stmt.getPredicate().toString();
			int occurrence = graphsearch.getEdgeOccurrence(predicate);
			if(!predicateOccurrenceMap.containsKey(predicate)) {
				predicateOccurrenceMap.put(predicate, occurrence);
				System.out.println(predicate + " : " + occurrence);
			}
		}
		return predicateOccurrenceMap;
	}
	
	public ArrayList<Statement> getQueryPatternsInOrder(String sparql, GraphDatabaseService graphDb) {
		ArrayList<Statement> queryPatternsInOrder = new ArrayList<Statement>();
		// find select variable and related patterns, iteratively find other variables
		ArrayList<Statement> queryPatterns = triplesStorage(sparql);
		String initialVar = getSelectVar(sparql);
		ArrayList<String> varsQueue = new ArrayList<String>();
		varsQueue.add(initialVar);
		//Map<String, Integer> predicateOccurrenceMap = getPredicateOccurrence(sparql);
		Map<String, Integer> predicateOccurrenceMap = getPredicateRuntimeOccurrence(sparql, graphDb);
		while(!varsQueue.isEmpty()) {
			String variable = varsQueue.get(0);	
			varsQueue.remove(0);
			ArrayList<Statement> varConditions = new ArrayList<Statement>();
			for(Statement stmt : queryPatterns) {
				if(stmt.getSubject().toString().equals(variable) || stmt.getObject().toString().equals(variable)) {
					varConditions.add(stmt);
				}
			}
			while(!varConditions.isEmpty()) {
				int minOccur = 1000000000, index = 0, currentIndex = 0;
				for(Statement condition : varConditions) {
					int occurrence = predicateOccurrenceMap.get(condition.getPredicate().toString());
					if(occurrence < minOccur) {
						minOccur = occurrence;
						index = currentIndex;
					}
					currentIndex++;
				}
				queryPatternsInOrder.add(varConditions.get(index));
				// If the statement contains a new variable, then add it into the queue.
				String subject = varConditions.get(index).getSubject().toString();
				if(subject.startsWith("?") && !subject.equals(variable)) {
					varsQueue.add(subject);
				}
				String object = varConditions.get(index).getObject().toString();
				if(object.startsWith("?") && !object.equals(variable)) {
					varsQueue.add(object);
				}
				queryPatterns.remove(varConditions.get(index));
				varConditions.remove(index);
			}
			
		}
		return queryPatternsInOrder;
	}
	
	public String constructWhyNotSparql(ArrayList<Statement> whynotQuery) {
		//String whynotSparql = originQuery.substring(0, originQuery.indexOf("{") + 1);
		String whynotSparql = "select * where {";
		String prefix = "http://dbpedia.org/";
		for(Statement stmt : whynotQuery) {
			if(stmt.getSubject().toString().contains(prefix)) {
				whynotSparql += "<" + stmt.getSubject().toString() + "> ";
			} else {
				whynotSparql += stmt.getSubject().toString() + " ";
			}
			whynotSparql += "<" + stmt.getPredicate().toString() + "> ";
			if(stmt.getObject().toString().contains(prefix)) {
				whynotSparql += "<" + stmt.getObject().toString() + ">. ";
			} else {
				whynotSparql += stmt.getObject().toString() + ". ";
			}
		}
		whynotSparql += "}";
		return whynotSparql;
	}
	
	public String getSelectVar(String sparql) {
		String selectVar = "";
		String[] select = sparql.substring(sparql.indexOf("SELECT"), sparql.indexOf("WHERE")).split(" ");
		for(String str : select) {
			if(str.startsWith("?")) {
				selectVar = str;
				break;
			}
		}
		return selectVar;
	}
	
	//extract the predicates (edge labels)
	public ArrayList<String> getPredicates(String sparql) {
		ArrayList<String> predicates = new ArrayList<String>();
        Query query = QueryFactory.create(sparql);		//create a query
        List<Element> elements = ((ElementGroup)query.getQueryPattern()).getElements();		//get all triples, a triples is an element here
        for (Element elem : elements){
            if(elem instanceof ElementPathBlock){
                ElementPathBlock pathBlock = (ElementPathBlock) elem;
                for(TriplePath triple : pathBlock.getPattern().getList()){
        		    predicates.add(triple.getPredicate().toString());
                }
            }
        }
		return predicates;
	}
	
	//extract the variables in the select section (only extract the variables returned as results)
	public ArrayList<String> varsExtract(String sparql) {
		ArrayList<String> variables = new ArrayList<String>();		//store the variables to be returned in the sparql query
		Query query = QueryFactory.create(sparql) ;		//create a query
		List<Var> vars = query.getProjectVars();		//get the variables
		for(Var var : vars) {
			variables.add(var.toString());
		}
		return variables;
	}

	//extract the variables in the select section (in the way that parse the query string, can extract all the variables in triples)
	public ArrayList<String> variablesExtract(String sparql) {
		ArrayList<String> variables = new ArrayList<String>();
		ArrayList<Statement> queryTriples = triplesStorage(sparql);
		for(int i = 0; i < queryTriples.size(); i++) {
			String sub = queryTriples.get(i).getSubject().toString();		//get the subject of the statement
			if(sub.startsWith("?") && !variables.contains(sub)) 			//if the subject is a variable and hasn't stored yet, then store
				variables.add(sub);
			String prop = queryTriples.get(i).getPredicate().toString();	//get the predict of the statement
			if(prop.startsWith("?") && !variables.contains(prop))  			//if the predict is a variable and hasn't stored yet, then store
				variables.add(prop);
			String obj = queryTriples.get(i).getObject().toString();		//get the object of the statement
			if(obj.startsWith("?") && !variables.contains(obj))  			//if the object is a variable and hasn't stored yet, then store
				variables.add(obj);
		}
		return variables;
	}
	
	public ArrayList<String> getVariableInstances(String sparql) {
		ArrayList<String> varsInstances = new ArrayList<String>();
		ArrayList<String> variables = variablesExtract(sparql);
		ArrayList<Statement> queryTriples = triplesStorage(sparql);		
		String prefix = "http://dbpedia.org/";
		JenaAPI jena = new JenaAPI(datasetpath);
		for(String var : variables) {
			ArrayList<Statement> varConditions = new ArrayList<Statement>();
			for(Statement stmt : queryTriples) {
				if(stmt.getSubject().toString().equals(var) || stmt.getObject().toString().equals(var)) {
					varConditions.add(stmt);
				}
			}
			//construct query of single variable
			String query = "select distinct " + var + " where { \n";
			for(Statement condition : varConditions) {
				String subject = condition.getSubject().toString();
				if(subject.contains(prefix)) {
					query += "<" + condition.getSubject().toString() + "> ";
					if(!varsInstances.contains(subject)) {
						varsInstances.add(subject);
					}
				} else {
					query += subject + " ";
				}
				if(condition.getPredicate().isURIResource()) {
					query += "<" + condition.getPredicate().toString() + "> ";
				} else {
					query += condition.getPredicate().toString() + " ";
				}
				RDFNode object = condition.getObject();
				if(object.isResource() && object.toString().contains(prefix)) {
					query += "<" + object.toString() + "> ";
					if(!varsInstances.contains(object.toString())) {
						varsInstances.add(object.toString());
					}
				} else {
					query += object.toString() + " ";
				}
				query += ". \n";
				//query += condition.getSubject().toString() + " " + condition.getPredicate().toString() + " " + condition.getObject().toString() + " . \n";
			}
			query += "}";
			//System.out.println(query);
			ResultSet rs = jena.QueryTDB(query);
			while (rs.hasNext()) {
			    QuerySolution row= rs.nextSolution();
			    String ins = row.get(var).toString();
			    if(!varsInstances.contains(ins)) {
			    	varsInstances.add(row.get(var).toString());
			    }
			}
		}
		return varsInstances;
	}
	
	public static void main(String[] args){
		//String filePath = "file:data/ontology.owl";
	    //user query
		String sparql = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
				    + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"
				    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
					+ "SELECT ?name (COUNT(?friend) AS ?count)"
					+ "WHERE {" 
					+ "    ?person dbpedia-owl:name ?name .\n" 
					+ "    ?name rdfs:label \"huangchao\" .\n" 
					+ "    ?person dbpedia-owl:type dbpedia-owl:Thing .\n" 
					+ "    ?person dbpedia-owl:knownFor ?friend .\n" 
					+ "} GROUP BY ?person ?name";
		String sparql4 = "SELECT * WHERE { 	?var <http://dbpedia.org/property/nationality> \"French\"@en .	?var <http://dbpedia.org/property/workplaces> <http://dbpedia.org/resource/Institut_des_Hautes_%C3%89tudes_Scientifiques> .	?var <http://dbpedia.org/property/fields> <http://dbpedia.org/resource/Mathematics> }";
		String sparql6 = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbpr: <http://dbpedia.org/resource/> SELECT ?var1 WHERE {	?var2 dbpp:era dbpr:Medieval_philosophy .	?var2 dbpp:influenced ?var1 .	?var2 dbpp:nationality dbpr:Persian_people .}";	//\"Avicenna\"@en
		UserQuery userQuery = new UserQuery();
		ArrayList<Statement> queryTriples = userQuery.triplesStorage(sparql);
		for(Statement stmt : queryTriples) {
			System.out.println(stmt);
		}
		
		ArrayList<String> variables = userQuery.variablesExtract(sparql4);
		for(int i = 0; i < variables.size(); i++) {
			System.out.println(variables.get(i));
		}
		
		ArrayList<String> instances = userQuery.getVariableInstances(sparql6);
		for(String instance : instances) {
			System.out.println(instance);
		}
		
		Map<String, Integer> predicateOccurrenceMap = userQuery.getPredicateOccurrence(sparql6);
		System.out.println("predicate occurrence : ");
		for(String key : predicateOccurrenceMap.keySet()) {
			System.out.println(key + " : " + predicateOccurrenceMap.get(key));
		}
		
/*		ArrayList<Statement> queryPatterns = userQuery.getQueryPatternsInOrder(sparql6);
		for(Statement pattern : queryPatterns) {
			System.out.println(pattern);
		}
*/	}

}
