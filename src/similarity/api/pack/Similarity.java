package similarity.api.pack;

import com.hp.hpl.jena.rdf.model.Statement;

public abstract class Similarity {
	
	//an abstract method, compute the similarity between two statements
	public abstract float computeSimilarity(Statement triple1, Statement triple2);
	
}
