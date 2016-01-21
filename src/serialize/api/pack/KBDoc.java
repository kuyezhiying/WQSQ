package serialize.api.pack;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;

@SuppressWarnings("serial")
public class KBDoc implements Serializable{
	private static final String datasetpath = "D:\\workspace\\WhyNot\\tdb_type_infoprop";				//local TDB storage path
	private static final Model model  = TDBFactory.createDataset(datasetpath).getDefaultModel();
		
	public final Map<String, String> kbUnigrams = constructKBUnigramsTable();
	public final Map<String, String> kbBigrams = constructKBBigramsTable();
	
	public Map<String, String> getKBUnigrams() {
		return this.kbUnigrams;
	}

	public Map<String, String> getKBBigrams() {
		return this.kbBigrams;
	}

	//construct KB unigrams table
	public Map<String, String> constructKBUnigramsTable() {
		Map<String, String> KBUnigrams = new HashMap<String, String>();
		for (Iterator<Statement> i = model.listStatements(); i.hasNext();){
			Statement c = (Statement) i.next();
			updateOccurrence(KBUnigrams, c.getSubject().toString());	//add the triple(entity occurs as a subject) to the document
			if(c.getObject().isResource()) {
				updateOccurrence(KBUnigrams, c.getObject().toString());	//add the triple(entity occurs as a object) to the document
			}
		}
		return KBUnigrams;
	}
	
	//construct KB bigrams table
	public Map<String, String> constructKBBigramsTable() {
		Map<String, String> KBBigrams = new HashMap<String, String>();	//entity document
		for (Iterator<Statement> i = model.listStatements(); i.hasNext();){
			Statement c = (Statement) i.next();
			String spPair = c.getSubject().toString() + "\t" + c.getPredicate().toString();		//subject-predicate pair as a bigram
			updateOccurrence(KBBigrams, spPair);
			if(c.getObject().isResource()) {
				String poPair = c.getPredicate().toString() + "\t" + c.getObject().toString();	//predicate-object pair as a bigram
				updateOccurrence(KBBigrams, poPair);
				String soPair = c.getSubject().toString() + "\t" + c.getObject().toString();	//subject-object pair as a bigram
				updateOccurrence(KBBigrams, soPair);
			}
		}
		return KBBigrams;
	}
	
	//update the occurrences of map(unigram/bigram table)
	public void updateOccurrence(Map<String, String> gramTable, String item) {
		if(gramTable.containsKey(item)) {	//if the unigram/bigram existed in the  gram table
			int number = Integer.parseInt(gramTable.get(item));	//get its occurrences
			number++;	//update its occurrence
			gramTable.remove(item);
			gramTable.put(item, String.valueOf(number));		//add the gram and updated occurrence value to the gram table
			System.out.println(item + " : " + number);
		}
		else {
			gramTable.put(item, String.valueOf(1));				//add a new bigram and its initialized occurrence(1) to the gram table
			System.out.println(item + " : " + 1);
		}
	}
	
    public static void insertData(String dbName) {
    	KBDoc kbDoc = new KBDoc();
    	Map<String, String> unigrams = kbDoc.getKBUnigrams();
    	Map<String, String> bigrams = kbDoc.getKBBigrams();
    	try {
	    	for(String unigram : unigrams.keySet()) {
	    		String value = unigram + ", " + unigrams.get(unigram);
	    		System.out.println(value);
	    		String insertItemSql = "insert into uri_freq(uri, frequence) values(" + value + ")"; 
	            Class.forName("com.mysql.jdbc.Driver"); 
	            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dbName, "root", "root"); 
	            PreparedStatement ps = conn.prepareStatement(insertItemSql); 
	            ps.executeUpdate(); 
	            if (ps != null) { 
	                ps.close();             //close the statement
	            }   
	            if (conn != null) { 
	                conn.close();            //close the connection
	            }     	
	    	}
	    	for(String bigram : bigrams.keySet()) {
	    		String value = bigram + ", " + bigrams.get(bigram);
	    		System.out.println(value);
	    		String insertItemSql = "insert into uri_freq(uri, frequence) values(" + value + ")"; 
	            Class.forName("com.mysql.jdbc.Driver"); 
	            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dbName, "root", "root"); 
	            PreparedStatement ps = conn.prepareStatement(insertItemSql); 
	            ps.executeUpdate(); 
	            if (ps != null) { 
	                ps.close();             //close the statement
	            }   
	            if (conn != null) { 
	                conn.close();            //close the connection
	            }     	
	    	}
    	} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public static void main(String[] args) {
/*		//serialize
		KBDoc kbDoc = new KBDoc(); 
        try {
            FileOutputStream fos = new FileOutputStream("kbDoc.out");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(kbDoc);
            System.out.println("Serialized the KBDoc successfully.");
            oos.close();                        
        } catch (Exception ex) {  
        	ex.printStackTrace();  
        }
*/
		String dbName = "kbdoc";
		insertData(dbName);
	}
}
