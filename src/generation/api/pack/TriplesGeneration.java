package generation.api.pack;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.rdf.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;

public class TriplesGeneration {
	
	private String filePath = "file:data/ontology.owl";		//config.getOntschema();	

	//user pellet reasoner to generalize the triples
	public ArrayList<Statement> generalizeTriplesWithPellet(ArrayList<Statement> triples) {
		ArrayList<Statement> middleRes = classGeneralization(triples);					//generalize the class nodes in the triples
		ArrayList<Statement> generalizedTriples = propertyGeneralization(middleRes);	//generalize the property nodes in the triples
		return generalizedTriples;
	}
	
    //generalize all the classes in why not triples
	public ArrayList<Statement> classGeneralization(ArrayList<Statement> triples) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();				//create ontology manager
        IRI docIRI = IRI.create(filePath);
		try {
			OWLOntology ont = manager.loadOntologyFromOntologyDocument(docIRI);			//load the ontology from a document
	        PelletReasonerFactory reasonerFactory = new PelletReasonerFactory();
		    PelletReasoner reasoner = reasonerFactory.createReasoner(ont);				//create the pellet reasoner
	        reasoner.precomputeInferences();
	        ArrayList<Statement> stmt = new ArrayList<Statement>();						//store the generalized triples
	        for(Statement st : triples) {
	        	Resource sub = st.getSubject();
	        	RDFNode node = st.getObject();			//we only consider the object part, cause the class nodes here only occur in this part 
	        	if(node.isResource() && ont.containsClassInSignature(IRI.create(node.toString()))) {
	                OWLClass searchCls = manager.getOWLDataFactory().getOWLClass(IRI.create(node.toString()));	//transform the class to OWLClass type
	                NodeSet<OWLClass> superClses = reasoner.getSuperClasses(searchCls, true);		//get the super classes
	                Set<OWLClass> clses = superClses.getFlattened();
	                for(OWLClass superCls : clses) {
	                	Resource newNode = ResourceFactory.createResource(superCls.toString());		//transform the super classes to a resource
	                	Statement newStmt = ResourceFactory.createStatement(st.getSubject(), st.getPredicate(), newNode);
	                	stmt.add(newStmt);				//composite with other parts and add the statement to the generalized tuples collection
			        }
		       	}	//else continue
	        	if(ont.containsClassInSignature(IRI.create(sub.toString()))) {
	                OWLClass searchCls = manager.getOWLDataFactory().getOWLClass(IRI.create(sub.toString()));	//transform the class to OWLClass type
	                NodeSet<OWLClass> superClses = reasoner.getSuperClasses(searchCls, true);		//get the super classes
	                Set<OWLClass> clses = superClses.getFlattened();
	                for(OWLClass superCls : clses) {
	                	Resource newNode = ResourceFactory.createResource(superCls.toString());		//transform the super classes to a resource
	                	Statement newStmt = ResourceFactory.createStatement(st.getSubject(), st.getPredicate(), newNode);
	                	stmt.add(newStmt);				//composite with other parts and add the statement to the generalized tuples collection
			        }
		       	}	//else continue
	        }
	        for(Statement s : stmt) {
	        	triples.add(s);		//put the generalized tuples into the origin tiples collection
	        }
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
        return triples;
	}
 
    //generalize all the properties in why not triples
	public ArrayList<Statement> propertyGeneralization(ArrayList<Statement> triples) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();			//create ontology manager
        IRI docIRI = IRI.create(filePath);
        String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		try {
			OWLOntology ont = manager.loadOntologyFromOntologyDocument(docIRI);		//load the ontology from a document
	        PelletReasonerFactory reasonerFactory = new PelletReasonerFactory();
		    PelletReasoner reasoner = reasonerFactory.createReasoner(ont);			//create the pellet reasoner
	        reasoner.precomputeInferences();
	        ArrayList<Statement> stmt = new ArrayList<Statement>();		//store the generalized triples and then add it to the triples collection
	        for(Statement st : triples) {
	        	Property node = st.getPredicate();			//we only consider the predicate part, cause the property nodes only occur in this part 
	        	if(node.toString().equals(type)) {		//if the property is type, then we should consider the object that is a class node to generalize	
	               	RDFNode cls = st.getObject();
	              	if(node.isResource()) {			//same to the generalization of a class node
		   		        OWLClass searchCls = manager.getOWLDataFactory().getOWLClass(IRI.create(cls.toString()));	//transform to the OWLClass type
				        NodeSet<OWLClass> superClses = reasoner.getSuperClasses(searchCls, true);					//get the super classes
				        Set<OWLClass> clses = superClses.getFlattened();
				        for(OWLClass superCls : clses) {
				        	Resource newNode = ResourceFactory.createResource(superCls.toString());
				        	Statement newStmt = ResourceFactory.createStatement(st.getSubject(), st.getPredicate(), newNode);
				        	stmt.add(newStmt);		//composite with other parts and add the statement to the generalized tuples collection
				        }
	              	}
	        	}
/*	        	else { 		//generalize the triples with a non-type property	
			        OWLObjectProperty searchObjProp = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(node.toString()));	//transform to the OWLObjectProperty type
			        NodeSet<OWLObjectPropertyExpression> superObjProps = reasoner.getSuperObjectProperties(searchObjProp, true);		//get the super object properties
			        Set<OWLObjectPropertyExpression> objProps = superObjProps.getFlattened();
			        for(OWLObjectPropertyExpression superProp : objProps) {
			        	if(superProp.toString().contains("Thing"))
			        		continue;
			        	Property newNode = ResourceFactory.createProperty(superProp.toString());
			        	Statement newStmt = ResourceFactory.createStatement(st.getSubject(), newNode, st.getObject());
			        	stmt.add(newStmt);			//composite with other parts and add the statement to the generalized tuples collection
			        }
	        	}
*/			}
	        for(Statement s : stmt) {
	        	if(!triples.contains(s)) {
	        		triples.add(s);		//put the generalized tuples into the origin tiples collection
	        	}
	        }
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
        return triples;
	}
           
    //generalize all the literals in why not triples
	public void variableGeneration(ArrayList<Statement> triples) {
		//unnecessary temporarily
	}

	//output the statements
	public void printStatements(ArrayList<Statement> triples) {
		for(Statement st : triples) {
			System.out.println(st);
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws LockObtainFailedException 
	 * @throws CorruptIndexException 
	 * @throws ParseException 
	 * @throws OWLOntologyCreationException 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String uri = "http://dbpedia.org/resource/Aristotle";		//http://dbpedia.org/ontology/grammyAward
		WhyNotTriples whynotTriples = new WhyNotTriples();
		ArrayList<Statement> questionTriples = whynotTriples.GenerateTriples(uri);
		TriplesGeneration triplesGeneration = new TriplesGeneration();
		triplesGeneration.printStatements(questionTriples);		
		//Generation
		ArrayList<Statement> generalizedTriples = triplesGeneration.generalizeTriplesWithPellet(questionTriples);  //generalize the nodes in the map
		System.out.println("\nAfter generalization:");
		triplesGeneration.printStatements(generalizedTriples);	//print the generalized triples
	}
}
