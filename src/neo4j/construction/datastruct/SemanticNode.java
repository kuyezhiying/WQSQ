package neo4j.construction.datastruct;

import java.util.ArrayList;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class SemanticNode  {
	private String id;
	ArrayList<Propery> properties;
	
	Node node;
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SemanticNode(String id) {
		this.id = id;
		properties = new ArrayList<Propery>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SemanticNode other = (SemanticNode) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * @param graphDb
	 * @function 构建节点
	 */
	public Node InstantialNode(GraphDatabaseService graphDb){
		node = graphDb.createNode();
		node.setProperty("uri", id);
		for(Propery p : properties){
			node.setProperty( p.getKey(), p.getValue() );
		}
		return node;
	}
	
	public void addpropery(Propery p){
		properties.add(p);
	}
	
}
