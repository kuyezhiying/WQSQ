package lucene.api.pack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class LuceneAPI {
	private String indexpath;	//the path to store the index
	Directory dir;				//directory of the index file
	
	public LuceneAPI(String path) {
		indexpath = path;
		try {
			dir = FSDirectory.open(new File(indexpath));
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.err.println("Cannot open the index file.");
		}
	}
	
	//build index with subject-object pair
	public void BuildIndex(HashMap<Resource, List<RDFNode>> soMap) {		 
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_33);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_33, analyzer);
		IndexWriter writer;
		try {
			writer = new IndexWriter(dir, iwc); 	//create a index writer with index directory and configuration info
			//create document of every subject-object pair
	        for(Resource subject : soMap.keySet()) {
				Document doc = new Document();
				Field subjectField = new Field("subject", subject.toString(), Field.Store.YES,Field.Index.ANALYZED);	//create a field of the subject
				doc.add(subjectField);		//add the subject into subject field
				List<RDFNode> objects = soMap.get(subject);
				for(RDFNode object : objects) {
					Field objecttField = new Field("object", object.toString(), Field.Store.YES,Field.Index.ANALYZED);	//create a field of the object
					doc.add(objecttField);		//add the object into object field
					//String content = subject.toString() + "\t" + object.toString() + "\n";
					//System.out.print(content);	//output the index content to console
					writer.addDocument(doc);	//write the document to create index file					
					//FileWriter fwriter = new FileWriter("pair.txt", true);
		            //fwriter.write(content);	//write the index content to a local file
		            //fwriter.close();				
				}
			}
			System.out.println("Finished Building Index.");
			writer.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	//search the index file with a keyword and query field(subject or object)
	@SuppressWarnings("resource")
	public ArrayList<String> SearchIndex(String keyword, String queryfield) {
		IndexReader reader;
		ArrayList<String> uri = new ArrayList<String>();
		try {
			reader = IndexReader.open(FSDirectory.open(new File(indexpath)));	//read the index file
		    IndexSearcher searcher = new IndexSearcher(reader);		//create a index searcher
		    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_33);				//create an analyzer
		    QueryParser parser = new QueryParser(Version.LUCENE_33, queryfield, analyzer);
		    Query q = parser.parse(keyword);		//parse the query field with the keyword
		    int k = 10;
		    TopDocs doc = searcher.search(q, k);	//search the query to return top k document
		    float topDocScore = doc.getMaxScore();
		    for(ScoreDoc sr : doc.scoreDocs) {
		    	if(sr.score == topDocScore) {
			    	Document dc = searcher.doc(sr.doc);	//obtain the document with document id
			    	String sub = dc.get("subject");		//get the subject(uri) of the doc
			    	if(!uri.contains(sub)) {
			    		uri.add(dc.get("subject"));
			    	}
		    	}
		    }
		    if(uri.isEmpty()) {
		    	System.out.println("Can not mapping the why-not keyword on the rdf instance.");
		    }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return uri;
	}
	
	public static void main(String[] args) {
		//String datasetpath ="D:\\workspace\\WhyNot\\tdb_imdb";
		//JenaAPI jena = new JenaAPI(datasetpath);
		
		String indexPath = "D:/workspace/WhyNot/index_ooi_name_label";
		LuceneAPI lucene = new LuceneAPI(indexPath);
		
		//String property = "http://www.w3.org/2000/01/rdf-schema#label";
		//HashMap<Resource, List<RDFNode>> soMap = jena.ListStatement(property);
		//lucene.BuildIndex(soMap);
		
		String keyword = "Axel Anderson";
		List<String> uriList = lucene.SearchIndex(keyword, "object");
		for(String uri : uriList) {
			System.out.println(uri);
		}
	}
}
