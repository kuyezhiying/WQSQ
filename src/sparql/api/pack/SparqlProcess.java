package sparql.api.pack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class SparqlProcess {

	public static final String id = "1";
	public static final String name = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>";
	public static final String description = "?name";
	public static final String varNumber = "2";
	public static final String tripleNumber = "3";
	public static final String outputFile = "D:\\workspace\\JenaTest\\sparql.xml";
	
	//transform a sparql to XML format 
	public void sparql2XML(String sparql) {
		File file = new File(outputFile);
		if(!file.exists()) {
			createXMLWithDOM();
		}
		
	}
	
	public void createXMLWithDOM() {
		DOMImplementation domImpl = new DOMImplementationImpl();
		Document doc = domImpl.createDocument(null, "SPARQL", null);
		createDocument(doc);
		try {
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
			//serialize DOM tree by using the DOM Level 3 Load and Save
			LSSerializer serializer = impl.createLSSerializer();
			//set the output with a pretty format
			serializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
			String str = serializer.writeToString(doc);
			File file = new File(outputFile);
		    if(!file.exists()) {
		    	file.createNewFile();
		    }
		    FileWriter writer = new FileWriter(file);
		    BufferedWriter bufWiter = new BufferedWriter(writer);
		    bufWiter.write(str, 0, str.length());
		    bufWiter.close();
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (ClassCastException e1) {
			e1.printStackTrace();
		}
	}
	
	public void createDocument(Document doc) {
		Element root = doc.getDocumentElement();		
		//ID of SPARQL
		root.setAttribute("id", id);
		
		//prefix section of the sparql
		Element prefixElement = doc.createElement("prefix");
		Text nameText = doc.createTextNode(name);
		prefixElement.appendChild(nameText);
		root.appendChild(prefixElement);
		
		//select section of the sparql
		Element selectElement = doc.createElement("select");
		selectElement.setAttribute("varNumber", varNumber);
		Text selectText = doc.createTextNode(description);
		selectElement.appendChild(selectText);
		root.appendChild(selectElement);

		//where section of the sparql
		Element whereElement = doc.createElement("where");
		whereElement.setAttribute("tripleNumber", tripleNumber);
		root.appendChild(whereElement);
		
		//triple of the where section
		Element tripleElement = doc.createElement("triple");
		Text tripleText = doc.createTextNode(description);
		tripleElement.appendChild(tripleText);
		whereElement.appendChild(tripleElement);	
		
		//filter of the where section
		Element filterElement = doc.createElement("filter");
		Text filterText = doc.createTextNode(description);
		filterElement.appendChild(filterText);
		whereElement.appendChild(filterElement);
		
		//other section of the sparql
		Element otherElement = doc.createElement("other");
		Text otherText = doc.createTextNode(description);
		otherElement.appendChild(otherText);
		root.appendChild(otherElement);		
	}
	
	public void createXMLWithJAXP() {
	    try {
			// Find a parser
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    factory.setNamespaceAware(true);
		    DocumentBuilder parser = factory.newDocumentBuilder();
		      
		    // Read the document
		    Document document = parser.newDocument(); 
		     
		    // Modify the document
			Element root = document.createElement("sparql");
			document.appendChild(root);
		    createDocument(document);
		      
		    // Write it out
		    TransformerFactory xformFactory = TransformerFactory.newInstance();
		    Transformer idTransform = xformFactory.newTransformer();
		    idTransform.setOutputProperty(OutputKeys.INDENT, "yes");
		    Source input = new DOMSource(document);
			File file = new File(outputFile);
		    if(!file.exists()) {
		    	file.createNewFile();
		    }
		    Result output = new StreamResult(new FileOutputStream(file));
			idTransform.transform(input, output);
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//extract the triples in the where section
	public String[] triplesExtract(String sparql) {
		String triplesContent = sparql.substring(sparql.indexOf("{") + 1, sparql.indexOf("}") - 1);
		String[] triples = triplesContent.split("\\.");
		printArray(triples);
		return triples;
	}
	
	//extract the prefix and corresponding namespace in the sparql
	public String[] prefixsExtract(String sparql) {
		String prefixContent = sparql.substring(sparql.indexOf("PREFIX"), sparql.indexOf("SELECT"));
		String[] prefix = prefixContent.split("PREFIX");
		printArray(prefix);
		return prefix;
	}
	
	public void printArray(String[] array) {
		for(int i = 0; i < array.length; i++)
			System.out.println(array[i].trim());
	}

	public void main(String[] args) {
	    //user query
		String sparql1 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
				    + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"
				    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
					+ "SELECT ?name (COUNT(?friend) AS ?count)"
					+ "WHERE {" 
					+ "    ?person dbpedia-owl:name ?name ." 
					+ "    ?name rdfs:label \"huangchao\" ." 
					+ "    ?person dbpedia-owl:type dbpedia-owl:Thing ." 
					+ "    ?person dbpedia-owl:knownFor ?friend ." 
					+ "} GROUP BY ?person ?name";
		String sparql2 = "PREFIX schema:<http://schema.org/>" +
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
				"PREFIX foaf:<http://xmlns.com/foaf/0.1/>" +
				"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>" +
				"PREFIX owl:<http://www.w3.org/2002/07/owl#>" +
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX steam:<http://steam.org/mini-project/>" +
				"SELECT DISTINCT * WHERE {" +
				"?x a steam:procedure ." +
				"?x schema:name ?procName ." +
				"?disease steam:hasProcedure ?x ." +
				"?disease steam:carriedBy ?hospital ." +
				"?disease steam:totalDischarge ?total ." +
				"?disease steam:averageCoveredCharge ?covered ." +
				"?disease steam:averageTotalPayment ?totalPayment ." +
				"?hospital schema:name ?hosp_name ." +
				"?hospital schema:address ?hosp_address ." +
				"?hospital schema:subtype ?type ." +
				"?hospital schema:ownedThrough ?owner ." +
				"?hospital schema:availableService ?service ." +
				"?hosp_address schema:streetAddress ?streetAddress ." +
				"?hosp_address schema:addressLocality ?hosp_local ." +
				"?hosp_address schema:addressRegion ?hosp_region ." +
				"?hosp_address schema:postalCode ?postalCode ." +
				"?hospital schema:telephone ?hosp_phone ." +
				"?hosp_address schema:longitude ?hosp_long ." +
				"?hosp_address schema:latitude ?hosp_lat ." +
				"FILTER regex(xsd:string(?procName), \"cardiac arrhythmia\", \"is\") " +
						"}";
		createXMLWithDOM();
		//createXMLWithJAXP();
		sparql2XML(sparql1);
		sparql2XML(sparql2);
	}
	
}
