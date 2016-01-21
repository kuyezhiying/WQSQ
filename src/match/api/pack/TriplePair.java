package match.api.pack;

import com.hp.hpl.jena.rdf.model.Statement;

public class TriplePair {
	
	private Statement source;
	private Statement target;
	private float similarity;

	public TriplePair(Statement source, Statement target) {
		this.source = source;
		this.target = target;
		this.similarity = GraphMatch.computeSimilarity(source, target);
	}
	
	public TriplePair(Statement source, Statement target, float similarity) {
		this.source = source;
		this.target = target;
		this.similarity = similarity;
	}
	
	public Statement getSource() {
		return source;
	}

	public void setSource(Statement source) {
		this.source = source;
	}

	public Statement getTarget() {
		return target;
	}

	public void setTarget(Statement target) {
		this.target = target;
	}

	public float getSimilarity() {
		return similarity;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}
}
