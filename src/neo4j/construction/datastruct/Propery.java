package neo4j.construction.datastruct;

public class Propery {
	private String key;
	private String value;
	
	public Propery(String name, String value) {
		super();
		this.key = name;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
	
	
}
