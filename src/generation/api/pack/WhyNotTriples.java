package generation.api.pack;

import java.util.*;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;

import jena.api.pack.JenaAPI;
import lucene.api.pack.LuceneAPI;

public class WhyNotTriples {
	//private Config config = new Config();
	private String indexpath = "D:/workspace/WhyNot/index_label";		//index file path
	private String datasetpath = "D:/workspace/WhyNot/tdb_type_infoprop";
	//"D:\\workspace\\WhyNot\\tdb_type_infoprop"//local TDB path

	//obtain the uris of the keyword
	public List<String> ObtainSubUri(String keyword) {
		LuceneAPI lucene = new LuceneAPI(indexpath);
		List<String> uriList = lucene.SearchIndex(keyword, "object");	//search keyword in object field	
		return uriList;		
	}

	//obtain all the statements with uri (subject or object)
	public ArrayList<Statement> GenerateTriples(String uri) {
		ArrayList<Statement> triples = new ArrayList<Statement>();
		JenaAPI jena = new JenaAPI(datasetpath);
		
		String sparql = "select * where {<" + uri +"> ?p ?o.}";		//construct a sparql to find all satisfied statements (uri as subject)
		ResultSet rs = jena.QueryTDB(sparql);	//execute the query
		//System.out.println(rs.hasNext());		//true if the resultset is not empty
		while (rs.hasNext()) {
		    QuerySolution row= rs.nextSolution();
		    Resource subject = ResourceFactory.createResource(uri);							//create the subject with uri
		    Property predicate= ResourceFactory.createProperty(row.get("p").toString());	//obtain the property
		    RDFNode object= row.get("o");		//obtain the object
			Statement item = ResourceFactory.createStatement(subject, predicate, object);
		    triples.add(item);
		}
		
		String sparql1 = "select * where {?s ?p <" + uri +"> .}";		//construct a sparql to find all satisfied statements (uri as object)
		ResultSet rs1 = jena.QueryTDB(sparql1);	//execute the query
		while (rs1.hasNext()) {
		    QuerySolution row= rs1.nextSolution();
		    Resource subject = ResourceFactory.createResource(row.get("s").toString());		//obtain the subject
		    Property predicate= ResourceFactory.createProperty(row.get("p").toString());	//obtain the property
		    RDFNode object= ResourceFactory.createResource(uri);		//create the object with uri
			Statement item = ResourceFactory.createStatement(subject, predicate, object);
		    triples.add(item);
		}
		
		return triples;
	}
	
	public static void main(String[] args) {
		WhyNotTriples whynotTriples = new WhyNotTriples();
		String uri = "http://dbpedia.org/resource/Aristotle";
		ArrayList<Statement> triples = whynotTriples.GenerateTriples(uri);
		for(Statement stmt : triples) {
			System.out.println("(" + stmt.getSubject().toString() + ", " + stmt.getPredicate().toString() + ", " + stmt.getObject().toString() + ")");
		}
	}

}
