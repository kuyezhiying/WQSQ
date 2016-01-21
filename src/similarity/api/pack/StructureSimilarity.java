package similarity.api.pack;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.rdf.model.*;

public class StructureSimilarity extends Similarity {
	
	public static final String ontology = "file:/D:/Ontology/dbpedia_3.9.owl";

	@Override
    public float computeSimilarity(Statement triple1, Statement triple2) {
		float score = 0;
		float score1 = 0; //SimilarityOfNodes(triple1.getSubject(), triple2.getSubject());	//similarity score between subjects
		float score2 = SimilarityOfNodes(triple1.getPredicate(), triple2.getPredicate());	//similarity score between predicates
		float score3 = SimilarityOfNodes(triple1.getObject(), triple2.getObject());		//similarity score between objects
		score = (score1 + score2 + score3) / 3;		//similarity score between triples
		return score;
    }

    //similarity score between subjects
    public float SimilarityOfNodes(Resource res1, Resource res2) {
		float score = 0;
		try {
			OWLOntologyManager manager = createOntManager();
			OWLClass node1 = manager.getOWLDataFactory().getOWLClass(IRI.create(res1.toString()));	//transform the resource to OWLClass node
			OWLClass node2 = manager.getOWLDataFactory().getOWLClass(IRI.create(res2.toString()));
			score = (float)2*depth(LCA(node1, node2))/(depth(node1) + depth(node2));	//similarity score between two class nodes
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return score;
	}
	
	//similarity score between predicates
	public float SimilarityOfNodes(Property pro1, Property pro2) {
		float score = 0;
		if(pro1.toString().equals(pro2.toString())) {
			score = 1;
			return score;
		}
		try {
			OWLOntologyManager manager = createOntManager();
			if(manager.contains(IRI.create(pro1.toString())) && manager.contains(IRI.create(pro2.toString()))) {
				OWLObjectProperty node1 = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(pro1.toString()));	//transform the resource to OWLObjectProperty node
				OWLObjectProperty node2 = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create(pro2.toString()));
				score = (float)2*depth(LCA(node1, node2))/(depth(node1) + depth(node2));		//similarity score between two property nodes
			}
			else
				return 0;
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return score;
	}

	//similarity score between objects
	public float SimilarityOfNodes(RDFNode obj1, RDFNode obj2) {
		float score = 0;
		try {
			OWLOntologyManager manager = createOntManager();
			if(obj1.isResource() && obj2.isResource()) {
				OWLClass node1 = manager.getOWLDataFactory().getOWLClass(IRI.create(obj1.toString()));			//transform the resource to OWLClass node
				OWLClass node2 = manager.getOWLDataFactory().getOWLClass(IRI.create(obj2.toString()));
				score = (float)2*depth(LCA(node1, node2))/(depth(node1) + depth(node2));	//similarity score between two object nodes
			}
			else if(obj1.isLiteral() && obj2.isLiteral()) {
				score = SimilarityOfLiterals(obj1.toString(), obj2.toString());				//similarity score between literals(compute the edit distance)
			}
			else
				return 0;
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return score;
	}

	//least common ancestor of two class node
	public OWLClass LCA(OWLClass node1, OWLClass node2) throws OWLOntologyCreationException {
        PelletReasoner reasoner = createPelletReasoner(); 
        int depth_node1 = depth(node1);    	//depth of node1
        int depth_node2 = depth(node2);    	//depth of node2
        int min_depth = depth_node1 < depth_node2 ? depth_node1 : depth_node2;
        OWLClass[] superClass1 = new OWLClass[depth_node1 + 1];   //store the super class of node1
        OWLClass[] superClass2 = new OWLClass[depth_node2 + 1];   //store the super class of node2
        superClass1[depth_node1] = node1;   //the last item is the node itself, in case node1 and node2 are siblings
        superClass2[depth_node2] = node2;
        NodeSet<OWLClass> parent1 = reasoner.getSuperClasses(node1, true);		//super classes of node1
        NodeSet<OWLClass> parent2 = reasoner.getSuperClasses(node2, true);		//super classes of node2
        while (!parent1.isEmpty()) {  		//if the parent node of node1 is not Empty, then store
        	superClass1[--depth_node1] = parent1.iterator().next().getRepresentativeElement();
			parent1 = reasoner.getSuperClasses(parent1.iterator().next().getRepresentativeElement(), true);
        }
        while (!parent2.isEmpty()) {  		//if the parent node of node2 is not Empty, then store
        	superClass2[--depth_node2] = parent2.iterator().next().getRepresentativeElement();
			parent2 = reasoner.getSuperClasses(parent2.iterator().next().getRepresentativeElement(), true);
        }
        if (node1.toString().equals(node2.toString())) {
        	return node1;		//if node1 and node2 is the same node, then return the node itself
        }
        int commonNodeNo = 0;	//the index of common ancestor
        for(int i = 0; i < min_depth; i++) {  	//backwards search to find the latest ancestor
        	if(superClass1[i].toString().equals(superClass2[i].toString())) {
	        	commonNodeNo = i; 		 	//remember the index 
        		continue;
        	}
        	else {
        		commonNodeNo = i - 1;		//unequal, means that the front item is the latest ancestor
        		break;
        	}
        }
        System.out.println("LCA method returns : " + superClass1[commonNodeNo].toString());  //output the latest common ancestor
        return superClass1[commonNodeNo];
	}

	//least common ancestor of two object property node
	public OWLObjectProperty LCA(OWLObjectProperty node1, OWLObjectProperty node2) throws OWLOntologyCreationException {
        PelletReasoner reasoner = createPelletReasoner(); 
        int depth_node1 = depth(node1);
        int depth_node2 = depth(node2);
        int min_depth = depth_node1 < depth_node2 ? depth_node1 : depth_node2;		//the minimum depth of node1 an node2
        OWLObjectProperty[] superObjProp1 = new OWLObjectProperty[depth_node1 + 1];
        OWLObjectProperty[] superObjProp2 = new OWLObjectProperty[depth_node2 + 1];
        superObjProp1[depth_node1] = node1;   //the last item is the node itself, in case node1 and node2 are siblings
        superObjProp2[depth_node2] = node2;
        NodeSet<OWLObjectPropertyExpression> parent1 = reasoner.getSuperObjectProperties(node1, true);		//super object properties of node1
        NodeSet<OWLObjectPropertyExpression> parent2 = reasoner.getSuperObjectProperties(node2, true);		//super object properties of node2
        while (!parent1.isEmpty()) {  		//if the parent node of node1 is not Empty, then store
        	superObjProp1[--depth_node1] = (OWLObjectProperty) parent1.iterator().next().getRepresentativeElement();
			parent1 = reasoner.getSuperObjectProperties(parent1.iterator().next().getRepresentativeElement(), true);
        }
        while (!parent2.isEmpty()) {  		//if the parent node of node2 is not Empty, then store
        	superObjProp2[--depth_node2] = (OWLObjectProperty) parent2.iterator().next().getRepresentativeElement();
			parent2 = reasoner.getSuperObjectProperties(parent2.iterator().next().getRepresentativeElement(), true);
        }
        if (node1.toString().equals(node2.toString())) {
        	return node1;		//if node1 and node2 is the same node, then return the node itself
        }
        int commonNodeNo = 0;	//the index of common ancestor
        for(int i = 0; i < min_depth; i++) {  	//backwards search to find the latest ancestor
        	if(superObjProp1[i].toString().equals(superObjProp2[i].toString())) {
        		commonNodeNo = i;
        		continue;
        	}
        	else {
        		commonNodeNo = i - 1;		//unequal, means that the front item is the latest ancestor
        		break;
        	}
        }
        System.out.println("LCA method returns : " + superObjProp1[commonNodeNo].toString());  //output the latest common ancestor
        return superObjProp1[commonNodeNo];
	}

	//least common ancestor of two data property node
	public OWLDataProperty LCA(OWLDataProperty node1, OWLDataProperty node2) throws OWLOntologyCreationException {
        PelletReasoner reasoner = createPelletReasoner(); 
        int depth_node1 = depth(node1);
        int depth_node2 = depth(node2);
        int min_depth = depth_node1 < depth_node2 ? depth_node1 : depth_node2;
        OWLDataProperty[] superDataProp1 = new OWLDataProperty[depth_node1 + 1];
        OWLDataProperty[] superDataProp2 = new OWLDataProperty[depth_node2 + 1];
        superDataProp1[depth_node1] = node1;   //the last item is the node itself, in case node1 and node2 are siblings
        superDataProp2[depth_node2] = node2;
        NodeSet<OWLDataProperty> parent1 = reasoner.getSuperDataProperties(node1, true);		//super data properties of node1
        NodeSet<OWLDataProperty> parent2 = reasoner.getSuperDataProperties(node2, true);		//super data properties of node2
        while (!parent1.isEmpty()) {  		//if the parent node of node1 is not Empty, then store
        	superDataProp1[--depth_node1] = parent1.iterator().next().getRepresentativeElement();
			parent1 = reasoner.getSuperDataProperties(parent1.iterator().next().getRepresentativeElement(), true);
        }
        while (!parent2.isEmpty()) {  		//if the parent node of node2 is not Empty, then store
        	superDataProp2[--depth_node2] = parent2.iterator().next().getRepresentativeElement();
			parent2 = reasoner.getSuperDataProperties(parent2.iterator().next().getRepresentativeElement(), true);
        }
        if (node1.toString().equals(node2.toString())) {
        	//System.out.println("Nodes should have different iris.");
        	return node1;
        }
        int commonNodeNo = 0;  //the index of common ancestor
        for(int i = 0; i < min_depth; i++) {  	//backwards search to find the latest ancestor
        	if(superDataProp1[i].toString().equals(superDataProp2[i].toString())) {
        		commonNodeNo = i;
        		continue;
        	}
        	else {
        		commonNodeNo = i - 1;		//unequal, means that the front item is the latest ancestor
        		break;
        	}
        }
        System.out.println("LCA method returns : " + superDataProp1[commonNodeNo].toString());  //output the latest common ancestor
        return superDataProp1[commonNodeNo];
	}

	//depth of a class node
	public int depth(OWLClass node) throws OWLOntologyCreationException {
        PelletReasoner reasoner = createPelletReasoner();    
        int node_depth = 0;
		if(node.isTopEntity()) {
			node_depth = 0;		//the depth of a top node is 0
		} else  {
            NodeSet<OWLClass> parent = reasoner.getSuperClasses(node, true);		//the super class of node
			while(!parent.isEmpty()) {
				node_depth++;
				parent = reasoner.getSuperClasses(parent.iterator().next().getRepresentativeElement(), true);
			}
		}
		return node_depth;
	}
	
	//depth of a object property node
	public int depth(OWLObjectProperty node) throws OWLOntologyCreationException {
	    PelletReasoner reasoner = createPelletReasoner(); 
	    int node_depth = 0;
		if(node.isTopEntity()) {
			node_depth = 0;		//the depth of a top node is 0
		} else {
            NodeSet<OWLObjectPropertyExpression> parent = reasoner.getSuperObjectProperties(node, true);		//the super object property of node
			while(!parent.isEmpty()) {
				node_depth++;
				parent = reasoner.getSuperObjectProperties(parent.iterator().next().getRepresentativeElement(), true);
			}			
		}
		return node_depth;
	}
	
	//depth of a data property node
	public int depth(OWLDataProperty node) throws OWLOntologyCreationException {
	    PelletReasoner reasoner = createPelletReasoner(); 
	    int node_depth = 0;
		if(node.isTopEntity()) {
			node_depth = 0;		//the depth of a top node is 0
		} else {
            NodeSet<OWLDataProperty> parent = reasoner.getSuperDataProperties(node, true);		//the super data property of node
			while(!parent.isEmpty()) {
				node_depth++;
				parent = reasoner.getSuperDataProperties(parent.iterator().next().getRepresentativeElement(), true);
			}			
		}
		return node_depth;
	}
	
	//the similarity between literals
	public float SimilarityOfLiterals(String srcStr, String dstStr) {
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
		for(int i = 1;i <= desLen;i++) {
			for(int j = 1; j <= desLen; j++) {
				int modifyDis = distance[(i - 1) * (desLen + 1) + (j - 1)] + (srcStr.charAt(i - 1) == dstStr.charAt(j - 1) ? 0 : 1);
				int addDis = distance[(i - 1) * (desLen + 1) + j] + 1;
				int deleteDis = distance[i * (desLen + 1) + (j - 1)] + 1;
				int tempDis = modifyDis < addDis ? modifyDis : addDis;
				distance[i * (desLen + 1) + j] = tempDis < deleteDis ? tempDis : deleteDis;
			}
		}
		float similarity =1 - (float) distance[srcLen * (desLen + 1) + desLen] / Math.max(srcLen, desLen); 
		return similarity;
	}

	//create a pellet reasoner
    public PelletReasoner createPelletReasoner() throws OWLOntologyCreationException {
		   OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		   //create ontology manager
		   IRI docIRI = IRI.create(ontology);
		   OWLOntology ont = manager.loadOntologyFromOntologyDocument(docIRI);		   //load the ontology from a document
		   PelletReasonerFactory reasonerFactory = new PelletReasonerFactory();
		   PelletReasoner reasoner = reasonerFactory.createReasoner(ont);			   //create a pellet reasoner
		   reasoner.precomputeInferences();		   	//ask the reasoner to do all the necessary work
		   return reasoner;
    }
    
    //create an OWLOntologyManager
    public OWLOntologyManager createOntManager() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		   //create ontology manager
        manager.loadOntologyFromOntologyDocument(IRI.create(ontology));		   //load the ontology from a document
        return manager;
    }
    
	public static void main(String[] args) throws OWLOntologyCreationException {
        Resource c1 = ResourceFactory.createResource("http://dbpedia.org/ontology/Engineer");
        Resource c2 = ResourceFactory.createResource("http://dbpedia.org/ontology/Family");
        StructureSimilarity structureSim = new StructureSimilarity();
		float score = structureSim.SimilarityOfNodes(c1, c2);
		//float score = computeSimilarity(c1, c2);
		System.out.println("the socre is :" + score);
	}

}
