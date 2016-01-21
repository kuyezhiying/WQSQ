package parameters.management;

public class Parameter {
	
	// JenaAPI parameters
	private final String tdbPath = "D:\\workspace\\WhyNot\\tdb_onttype_ontinfobox_infoprop";		//TDB directory		D:\\workspace\\WhyNot\\tdb_stw
	private final String ontPath = "D:\\workspace\\WhyNot\\data\\stw.ttl";		//Ontology file path
	
	// LuceneAPI parameters
	private final String indexPath = "D:\\workspace\\WhyNot\\data\\index_label_lucene3";		//Lucene index directory
	
	// Neo4j parameters
	private final String neoDBPath = "C:\\Users\\huangchao\\Documents\\Neo4j\\whynot.runtime.graphdb";		//Neo4j database directory

	public String getTdbPath() {
		return tdbPath;
	}

	public String getOntPath() {
		return ontPath;
	}

	public String getIndexPath() {
		return indexPath;
	}

	public String getNeoDBPath() {
		return neoDBPath;
	}
	
}
