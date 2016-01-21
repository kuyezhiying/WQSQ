package data.process;

import generation.api.pack.WhyNotTriples;

import java.util.ArrayList;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

import neo4j.construction.GraphSearch;
import lucene.api.pack.LuceneAPI;
import match.api.pack.UserQuery;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

public class QuadrupleGeneration {
	
	private String indexPath = "D:/workspace/WhyNot/index_ooi_name_label";
	private String global_db_path = "C:\\Users\\huangchao\\Documents\\Neo4j\\dbpedia.global.graphdb";
	
	public ArrayList<String> getInstances(String keyword, String query) {
		//Get type-1 instances
		LuceneAPI lucene = new LuceneAPI(indexPath);
		ArrayList<String> uriList = lucene.SearchIndex(keyword, "object");
		//Get type-2 instances
		UserQuery userQuery = new UserQuery();
		ArrayList<String> instances = userQuery.getVariableInstances(query);
		for(String uri : instances) {
			if(!uriList.contains(uri)) {
				uriList.add(uri);
			}
		}
/*		//Find linked path between two kinds of instances and add the linked instances
		GraphSearch graphsearch = new GraphSearch(global_db_path);
		int h = 3;
		for(String instance : instances) {
			Node instanceNode = graphsearch.searchNode("uri", instance, "nodes").get(0);
			for(String uri : uriList) {
				Node keywordNode = graphsearch.searchNode("uri", uri, "nodes").get(0);
				Iterable<Path> paths = graphsearch.findShortestPath(instanceNode, keywordNode, h);
				for(Path path : paths) {
					if(path.length() > 1) {
						Iterable<Relationship> relationships = path.relationships();
						for(Relationship relationship : relationships) {
							Node[] relationNodes = relationship.getNodes();
							for(Node node : relationNodes) {
								String linkedInstance = node.getProperties("uri").toString();
								if(!uriList.contains(linkedInstance)) {
									uriList.add(linkedInstance);
								}
							}
						}
					}
				}
			}
		}
*/		System.out.println("Runtime instances : " + uriList.size());
		return uriList;
	}
	
	public ArrayList<Quadruple> getQuardruples(String keyword, String query) {
		ArrayList<Quadruple> quadruples = new ArrayList<Quadruple>();
		ArrayList<String> instances = getInstances(keyword, query);
		WhyNotTriples whynotTriples = new WhyNotTriples();
		for(String instance : instances) {
			ArrayList<Statement> statements = whynotTriples.GenerateTriples(instance);
			for(Statement stmt : statements) {
				Quadruple quadruple = new Quadruple();
				quadruple.setSubject(stmt.getSubject().toString());
				quadruple.setPredicate(stmt.getPredicate().toString());
				quadruple.setObject(stmt.getObject().toString());
				RDFNode object = stmt.getObject();
				if(object.isResource()) {
					quadruple.setQuadrupleType(QuadrupleType.RELATIOIN);
				} else {
					quadruple.setQuadrupleType(QuadrupleType.ATTRIBUTE);
				}
				if(!quadruples.contains(quadruple)) {
					quadruples.add(quadruple);
				}
			}
		}
		System.out.println("Number of Quadruples(contain duplicate edges) : " + quadruples.size());
		return quadruples;
	}
	
	public ArrayList<Quadruple> removeUselessQuads(ArrayList<Quadruple> quadruples, String query) {
		ArrayList<Quadruple> runtimeQuads = new ArrayList<Quadruple>();
		UserQuery userQuery = new UserQuery();
		ArrayList<String> edgeLabels = userQuery.getPredicates(query);
		for(Quadruple quad : quadruples) {
			String predicate = quad.getPredicate();  
			for(String label : edgeLabels) {
				if(predicate.equals(label)) {
					//quad.print();
					runtimeQuads.add(quad);
					break;
				}
			}
		}
		System.out.println("Number of Quadruples(remove useless edges) : " + runtimeQuads.size());
		return runtimeQuads;
	}
	
	public ArrayList<Quadruple> generateQuardruples(String keyword, String sparql) {
		ArrayList<Quadruple> quadruples = new ArrayList<Quadruple>();
		WhyNotTriples whynotTriples = new WhyNotTriples();
		//Get type-1 instances
		LuceneAPI lucene = new LuceneAPI(indexPath);
		ArrayList<String> uriList = lucene.SearchIndex(keyword, "object");
		for(String uri : uriList) {
			ArrayList<Statement> statements = whynotTriples.GenerateTriples(uri);
			for(Statement stmt : statements) {
				Quadruple quadruple;
				RDFNode object = stmt.getObject();
				if(object.isResource()) {
					quadruple = new Quadruple(stmt.getSubject().toString(), stmt.getPredicate().toString(), stmt.getObject().toString(), QuadrupleType.RELATIOIN);
				} else {
					quadruple = new Quadruple(stmt.getSubject().toString(), stmt.getPredicate().toString(), stmt.getObject().toString(), QuadrupleType.ATTRIBUTE);
				}
				quadruples.add(quadruple);
			}
		}
		//Get type-2 instances
		int type2InsCount = 0;
		UserQuery userQuery = new UserQuery();
		ArrayList<String> instances = userQuery.getVariableInstances(sparql);
		ArrayList<String> edgeLabels = userQuery.getPredicates(sparql);
		for(String instance : instances) {
			ArrayList<Statement> statements = whynotTriples.GenerateTriples(instance);
			for(Statement stmt : statements) {
				String predicate = stmt.getPredicate().toString();
				if(edgeLabels.contains(predicate)) {
					Quadruple quadruple;
					RDFNode object = stmt.getObject();
					if(object.isResource()) {
						quadruple = new Quadruple(stmt.getSubject().toString(), stmt.getPredicate().toString(), stmt.getObject().toString(), QuadrupleType.RELATIOIN);
					} else {
						quadruple = new Quadruple(stmt.getSubject().toString(), stmt.getPredicate().toString(), stmt.getObject().toString(), QuadrupleType.ATTRIBUTE);
					}
					if(!quadruples.contains(quadruple)) {
						quadruples.add(quadruple);
						type2InsCount++;
					}
				}
			}
		}
		System.out.println("Runtime instances : " + (uriList.size() + instances.size()));
		System.out.println("Number of Quadruples(remove useless edges of type-2 instances) : " + quadruples.size() + " (including [" + type2InsCount + "] edges of type-2 instances).");
		return quadruples;
	}

	public static void main(String args[]) {
		String keyword = "aristotle";
		String sparql = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbpr: <http://dbpedia.org/resource/> SELECT * WHERE {	?var1 dbpp:era dbpr:Medieval_philosophy .	?var2 dbpp:influenced ?var1 .	?var2 dbpp:name \"Avicenna\"@en .}";
		QuadrupleGeneration quadGeneration = new QuadrupleGeneration();
		ArrayList<Quadruple> quadruples = quadGeneration.getQuardruples(keyword, sparql);
		ArrayList<Quadruple> runtimeQuads = quadGeneration.removeUselessQuads(quadruples, sparql);
		for(Quadruple quad : runtimeQuads) {
			quad.print();
		}
		//ArrayList<Quadruple> runtimeQuads = quadGeneration.generateQuardruples(keyword, sparql);		
	}

}