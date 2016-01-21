package neo4j.construction.datastruct;

import org.neo4j.graphdb.Relationship;

import neo4j.construction.RelTypes;

public class SemanticRelation {

	private String relationName;
	private SemanticNode start;
	private SemanticNode end;
	public static String name = "relation_label";
	public SemanticRelation(String relationName, SemanticNode start, SemanticNode end) {
		
		this.relationName = relationName;
		this.start = start;
		this.end = end;
	}

	public void InstantialRelation(){
		RelTypes relation = RelTypes.RELATION;
		Relationship relationship = this.start.node.createRelationshipTo(this.end.node, relation);
		relationship.setProperty(name, relationName);
	}
	public String getRelationName() {
		return relationName;
	}

	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}

	public SemanticNode getStart() {
		return start;
	}

	public void setStart(SemanticNode start) {
		this.start = start;
	}

	public SemanticNode getEnd() {
		return end;
	}

	public void setEnd(SemanticNode end) {
		this.end = end;
	}
	
	
	
}
