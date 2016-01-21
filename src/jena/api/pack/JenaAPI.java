package jena.api.pack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class JenaAPI {

	private String datasetpath;		//TDB local storage path
	private Dataset ds;				//store the ontology data
	private Model model;				//the dataset model

	public Model getModel() {
		return model;
	}

	//initial class member with a local ontology path
	public JenaAPI(String dspath) {
		datasetpath = dspath;
		ds = TDBFactory.createDataset(datasetpath);
		model  = ds.getDefaultModel();
	}
	
	//create the TDB to store data
	public void CreateTDB(String filepath) {
		ds = TDBFactory.createDataset(datasetpath);		//create a dataset to store data
		model = ds.getDefaultModel();
		FileManager.get().readModel(model, filepath);		//read the ontology to model
		model.commit();		//commit to create TDB
		System.out.println("TDB Created Successfully.");
		model.close();
	}
	
	//store the subject-object pair of statements whose object is literal
	public HashMap<Resource, List<RDFNode>> ListStatement() {
		HashMap<Resource, List<RDFNode>> soMap = new HashMap<Resource, List<RDFNode>>();	//map that stores the subject-object pair
		Model m = ds.getDefaultModel();					//model of the TDB dataset
	    StmtIterator it =  m.listStatements();			//all the statements in the dataset
	    while(it.hasNext()) {
	    	Statement stmt = it.nextStatement();
    		Resource subject = stmt.getSubject();
	    	RDFNode object = stmt.getObject();
	    	if(object.isLiteral() && object.toString().contains("@en")) {
				if(soMap.containsKey(subject)) {		//makes the objects shared a common subject stored in a list
					List<RDFNode> list = soMap.get(subject);
					list.add(object);
					soMap.put(subject, list);			//subject corresponds to the key, while the objects which are literals corresponds to the value
				}
				else {
					List<RDFNode> list = new ArrayList<RDFNode>();
					list.add(object);
					soMap.put(subject, list);
				}
	    	}
	    }
	    return soMap;
	}
	
	//store the subject-object pair with a certain property(always a literal property which annotate the class)
	public HashMap<Resource, List<RDFNode>> ListStatement(String property) {
		//OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		//ontModel.read(ontology); 	//read the ontology file and load the model
		HashMap<Resource, List<RDFNode>> soMap = new HashMap<Resource, List<RDFNode>>();	
		//Property prop = ontModel.getProperty(property);
		Property prop = ResourceFactory.createProperty(property);
	    Iterator<Statement> it = model.listStatements(null, prop, (RDFNode)null);	//obtain the statements with certain property
	    while(it.hasNext()) {
	    	Statement stmt = it.next();
	   		Resource subject = stmt.getSubject();	    	
	    	RDFNode object = stmt.getObject();
	    	if(object.isLiteral()) {	//only preserve the literal in English
				if(soMap.containsKey(subject)) {
					List<RDFNode> list = soMap.get(subject);
					list.add(object);
					soMap.put(subject, list);
				}
				else {
					List<RDFNode> list = new ArrayList<RDFNode>();
					list.add(object);
					soMap.put(subject, list);
				}
	    	}
	    }
	    return soMap;
	}

	//query the TDB with a sparql
	public ResultSet QueryTDB(String sparql) {
		Query query = QueryFactory.create(sparql) ;	//create a query with the sparql string
		QueryExecution q = QueryExecutionFactory.create(query,ds);
		ResultSet resultSet = q.execSelect();		//execute the query to return a resutset
		//NOTE: below lines should be commented, otherwise when traverse the resultset, there would occur error(cause the cursor has shifted to the end)
		//ResultSetFormatter.out(resultSet);		//output the result to console with a specialized format
		try {
			//FileOutputStream fo = new FileOutputStream("result.xml");	//output the result to an xml file
			//ResultSetFormatter.outputAsXML(fo, resultSet);			//output the result in xml format
		}catch(Exception e) {
			e.printStackTrace();
		}
        return resultSet;
	}
	
	//query the TDB with a sparql
	public void QueryTDBandFormat(String sparql) {
		Query query = QueryFactory.create(sparql) ;	//create a query with the sparql string
		QueryExecution q = QueryExecutionFactory.create(query,ds);
		ResultSet resultSet = q.execSelect();		//execute the query to return a resutset
		//NOTE: below lines should be commented, otherwise when traverse the resultset, there would occur error(cause the cursor has shifted to the end)
		ResultSetFormatter.out(resultSet);		//output the result to console with a specialized format
	}

	public static void main(String[] args) {
		String datasetpath ="D:\\workspace\\WhyNot\\tdb_onttype_ontinfobox_infoprop";	//tdb_imdb 		tdb_type_infoprop
		JenaAPI jena = new JenaAPI(datasetpath);

		//String filePath = "file:data/imdb.nt";
		//jena.CreateTDB(filePath);

		//String sparql = "PREFIX dbpr: <http://dbpedia.org/resource/> PREFIX dbpp: <http://dbpedia.org/property/> SELECT * WHERE { 	?var dbpp:occupation dbpr:Engineer.	?var dbpp:occupation dbpr:Mathematician.}";//<http://dbpedia.org/resource/BMW> ?var  dbpp:products \"Luxury cars\"@en ; ?var <http://dbpedia.org/property/locationCountry> \"Germany\"@en .  
		//String sparql1 = "PREFIX dbpedia-owl: <http://dbpedia.org/resource/> SELECT * WHERE { ?s <http://dbpedia.org/property/mainInterests> dbpedia-owl:Natural_philosophy .}";
		String test = "select * where {<http://dbpedia.org/resource/Aristotle> ?p ?o.}";	//<http://dbpedia.org/ontology/movie>
		String sparql = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> SELECT DISTINCT ?uri WHERE {        ?uri dbo:spouse res:Tom_Cruise.}";
		String sparql6 = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbpr: <http://dbpedia.org/resource/> SELECT * WHERE {	?var1 dbpp:era dbpr:Medieval_philosophy .	?var2 dbpp:influenced ?var1 .	?var2 dbpp:nationality dbpr:Persian_people .}";	//\"Avicenna\"@en
		String query = "select (count(?o) as ?count) where { <http://dbpedia.org/resource/Avicenna> ?p ?o . }";
		String query1 = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri WHERE { ?uri rdf:type dbo:Animal . ?uri dbo:conservationStatus \"EX\" . }";
		String test1 = "PREFIX dbo:  <http://dbpedia.org/ontology/> PREFIX res:  <http://dbpedia.org/resource/> PREFIX dbpp: <http://dbpedia.org/property/> SELECT DISTINCT ?uri WHERE {        ?uri dbpp:name \"Baldwin\"@en .         ?uri dbpp:occupation res:Actor . }";
		String test2 = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX dbpp: <http://dbpedia.org/property/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT * WHERE {        ?uri rdf:type dbo:Band .        ?uri dbpp:genre res:Metalcore .       ?uri dbpp:origin ?h .  ?h dbpp:countryCode \"USA\"@en  }";
		String sparql16 = "PREFIX dbpp: <http://dbpedia.org/property/> PREFIX res: <http://dbpedia.org/resource/> SELECT DISTINCT * WHERE {        res:Carrot_cake dbpp:mainIngredient ?uri .}";
		String sparql21 = "PREFIX dbo:  <http://dbpedia.org/ontology/> PREFIX dbpp: <http://dbpedia.org/property/> PREFIX dbpr: <http://dbpedia.org/resource/> SELECT * WHERE {	?var1 dbpp:era dbpr:Medieval_philosophy . ?var2 dbo:influenced ?var1 .  ?var2 dbpp:name \"Aristotle\"@en . ?var2 dbpp:nationality ?var3. ?var3 dbpp:languages dbpr:Greek_language.}";	
		//Albertus_Magnus Maimonides (Duns_Scotus  Aristotle Greeks)
		String total = "select (count(?s) as ?count) where{ ?s ?p ?o.}";
		jena.QueryTDBandFormat(total);
	}

}
