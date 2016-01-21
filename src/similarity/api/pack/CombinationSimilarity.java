package similarity.api.pack;

import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class CombinationSimilarity{

	public static final String ontology = "file:/D:/Ontology/dbpedia_3.9.owl";

	public float computeSimilarity(Statement triple1, Statement triple2, Map<String, String> stmtsMap) {
		float score = 0;
		float preScore = SimilarityOfNodes(triple1.getPredicate(), triple2.getPredicate(), stmtsMap);		//similarity score between predicates
		String subject = triple1.getSubject().toString();
		String object = triple1.getObject().toString();
		if(subject.startsWith("?") && object.startsWith("?")) {
			score = preScore;		//if the subject and object are variables, then the similarity between triples is the similarity between predicates
		}
		else {
			float subScore = SimilarityOfNodes(triple1.getSubject(), triple2.getSubject(), stmtsMap);		//similarity score between subjects
			float objScore = SimilarityOfNodes(triple1.getObject(), triple2.getObject(), stmtsMap);		//similarity score between objects
			score = (subScore + preScore + objScore) / 2;		//similarity score between triples, either subSocre or objScore is zero			
		}
		//System.out.println("triple similarity score : " + score);
		return score;
	}
	
    //similarity score between subjects
    public float SimilarityOfNodes(Resource res1, Resource res2, Map<String, String> stmtsMap) {
		float score = 0;
		if(res1.toString().startsWith("?")) {
			return 0;
		}
		if(res1 == res2) {
			return 1.0f;	//if obj1 and obj2 is the same entity, assign its similarity score to 1.0
		}
		float sub1LM = Float.parseFloat(stmtsMap.get(res1.toString()));		//language model of entity(sub1's distribution)
		float sub2LM = Float.parseFloat(stmtsMap.get(res2.toString()));		//language model of entity(sub2's distribution)
		float middleLM = (sub1LM + sub2LM) / 2;
		score = (float) Math.sqrt((sub1LM * Math.log10(sub1LM / middleLM) + sub2LM * Math.log10(sub2LM / middleLM)));		//the distance between two entity language models as similarity score					
/*		if(res1.listProperties().hasNext() && res2.listProperties().hasNext()) {	//if res1 and res2 are both classes
			try {
				OWLOntologyManager manager = createOntManager();
				StructureSimilarity strucSim = new StructureSimilarity();
				OWLClass node1 = manager.getOWLDataFactory().getOWLClass(IRI.create(res1.toString()));	//transform the resource to OWLClass node
				OWLClass node2 = manager.getOWLDataFactory().getOWLClass(IRI.create(res2.toString()));
				score = (float)2*strucSim.depth(strucSim.LCA(node1, node2))/(strucSim.depth(node1) + strucSim.depth(node2));	//similarity score between two class nodes
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
		}

			LexiconComputation lexicon = new LexiconComputation();
			float obj1LM = lexicon.constructEntityLM(res1.toString());		//language model of entity(obj1's distribution)
			float obj2LM = lexicon.constructEntityLM(res2.toString());		//language model of entity(obj2's distribution)
			float middleLM = (obj1LM + obj2LM) / 2;
			score = (float) Math.sqrt((obj1LM * Math.log10(obj1LM / middleLM) + obj2LM * Math.log10(obj2LM / middleLM)));		//the distance between two entity language models as similarity score			
*/	  
		//System.out.println("subject similarity score : " + score);
		return score;
	}
	
	//similarity score between predicates
	public float SimilarityOfNodes(Property prop1, Property prop2, Map<String, String> stmtsMap) {
		float score = 0;
		if(prop1.toString().equals(prop2.toString())) {
			return 1.0f;
		}
		else {
			float prop1LM = Float.parseFloat(stmtsMap.get(prop1.toString()));		//language model of entity(prop1's distribution)
			float prop2LM = Float.parseFloat(stmtsMap.get(prop2.toString()));		//language model of entity(prop2's distribution)
			float middleLM = (prop1LM + prop2LM) / 2;
			float score1 = (float) Math.sqrt((prop1LM * Math.log10(prop1LM / middleLM) + prop2LM * Math.log10(prop2LM / middleLM)));		//the distance between two entity language models as similarity score	
			//String propStr1 = prop1.toString().substring(prop1.toString().indexOf("property") + 9, prop1.toString().length());
			//String propStr2 = prop2.toString().substring(prop2.toString().indexOf("property") + 9, prop2.toString().length());
			float score2 = SimilarityOfLiterals(prop1.toString(), prop2.toString());	//edit distance between string of properties
			score = (score1 + score2) / 2;		//normalization
		}
		//System.out.println("predicate similarity score : " + score);
		return score;
	}

	//similarity score between objects
	public float SimilarityOfNodes(RDFNode obj1, RDFNode obj2, Map<String, String> stmtsMap) {
		float score = 0;
		if(obj1.isResource() && obj2.isResource()) {
			if(obj1.toString().startsWith("?")) {
				return 0;
			}
			if(obj1 == obj2) {
				return 1.0f;	//if obj1 and obj2 is the same entity, assign its similarity score to 1.0
			}
			float obj1LM = Float.parseFloat(stmtsMap.get(obj1.toString()));		//language model of entity(obj1's distribution)
			float obj2LM = Float.parseFloat(stmtsMap.get(obj2.toString()));		//language model of entity(obj2's distribution)
			float middleLM = (obj1LM + obj2LM) / 2;
			score = (float) Math.sqrt((obj1LM * Math.log10(obj1LM / middleLM) + obj2LM * Math.log10(obj2LM / middleLM)));		//the distance between two entity language models as similarity score					
		}
		else if(obj1.isLiteral() && obj2.isLiteral()) {		//use edit distance to compute similarity between literals
			score = SimilarityOfLiterals(obj1.toString(), obj2.toString());
		}
		//System.out.println("object similarity score : " + score);
		return score;
	}

	//the similarity between literals(edit distance)
	public float SimilarityOfLiterals(String srcStr, String dstStr) {//Alain Connes@en
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
		for(int i = 1;i <= srcLen;i++) {
			for(int j = 1; j <= desLen; j++) {
				int modifyDis = distance[(i - 1) * (desLen + 1) + (j - 1)] + (srcStr.charAt(i - 1) == dstStr.charAt(j - 1) ? 0 : 1);
				int addDis = distance[(i - 1) * (desLen + 1) + j] + 1;
				int deleteDis = distance[i * (desLen + 1) + (j - 1)] + 1;
				int tempDis = modifyDis < addDis ? modifyDis : addDis;
				distance[i * (desLen + 1) + j] = tempDis < deleteDis ? tempDis : deleteDis;
			}
		}
		float similarity = 1 - (float) distance[srcLen * (desLen + 1) + desLen] / Math.max(srcLen, desLen); 
		return similarity;
	}

	//create an OWLOntologyManager
    public OWLOntologyManager createOntManager() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		   //create ontology manager
        manager.loadOntologyFromOntologyDocument(IRI.create(ontology));		   //load the ontology from a document
        return manager;
    }

}
