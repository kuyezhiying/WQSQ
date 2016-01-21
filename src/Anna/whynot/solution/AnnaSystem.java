package Anna.whynot.solution;

import java.util.ArrayList;
import java.util.List;

import jena.api.pack.JenaAPI;
import lucene.api.pack.LuceneAPI;
import match.api.pack.UserQuery;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class AnnaSystem {
	
	private String datasetpath = "";
	
	// Replace the select variable with why not instance to construct WBGP(why-not basic graph pattern)
	public ArrayList<Statement> constructWBGP(String query, String whynotInsUri) {
		ArrayList<Statement> wBGP = new ArrayList<Statement>();
		Resource whynotIns = ResourceFactory.createResource(whynotInsUri);
		UserQuery userQuery = new UserQuery();
		String whynotVar = userQuery.getSelectVar(query);
		ArrayList<Statement> triples = userQuery.triplesStorage(query);
		for(Statement stmt : triples) {
			if(stmt.getSubject().toString().equals(whynotVar)) {
				Statement wBGPItem = ResourceFactory.createStatement(whynotIns, stmt.getPredicate(), stmt.getObject());
				wBGP.add(wBGPItem);
			} else if(stmt.getObject().toString().equals(whynotVar)) {
				Statement wBGPItem = ResourceFactory.createStatement(stmt.getSubject(), stmt.getPredicate(), whynotIns);
				wBGP.add(wBGPItem);
			} else {
				wBGP.add(stmt);
			}
		}
		return wBGP;
	}
	
	public ArrayList<Statement> modifyWBGP(ArrayList<Statement> wBGP) {
		ArrayList<Statement> mWBGP = new ArrayList<Statement>();
		ArrayList<Statement> wBGPWithOrder = sortWBGP(wBGP);
		for(Statement stmt : wBGPWithOrder) {
			mWBGP.add(stmt);
			if(isMWBGPWork(mWBGP) || getPatternOrder(stmt) == 0) {
				wBGPWithOrder.remove(stmt);
				continue;
			} else {
				mWBGP.remove(stmt);
				Statement generalizedStmt = getGeneralizedPattern(stmt);
				mWBGP.add(generalizedStmt);
				wBGPWithOrder.remove(stmt);
				break;
			}
		}
		while(!wBGPWithOrder.isEmpty()) {
			Statement stmt = wBGPWithOrder.get(0);
			mWBGP.add(stmt);
			wBGPWithOrder.remove(stmt);
		}
		return mWBGP;
	}
	
	private Statement getGeneralizedPattern(Statement pattern) {
		String prefix = "";
		String type = "";
		ArrayList<Statement> generalizedPatternWithOrder = new ArrayList<Statement>();
		if(pattern.getPredicate().toString().equals(type)) {
			RDFNode object = pattern.getObject();
			if(object.isResource() && !object.toString().startsWith("?")) {
				// get super classes of object
				
			}
		}
		String subject = pattern.getSubject().toString();
		if(subject.startsWith("?")) {
			
		}
		return null;
	}

	private ArrayList<Statement> sortWBGP(ArrayList<Statement> wBGP) {
		if(wBGP.size() == 1) {
			return wBGP;
		}
		ArrayList<Statement> sortedWBGP = new ArrayList<Statement>();
		int[] order = new int[wBGP.size()];
		int index = 0;
		for(Statement stmt : wBGP) {
			order[index++] = getPatternOrder(stmt);
		}
		for(int count = 0; count < order.length; count++) {
			int minOrder = order[0], minIndex = 0;
			for(int i = 1; i < order.length; i++) {
				if(minOrder > order[i]) {
					minOrder = order[i];
					minIndex = i;
				}
			}
			order[minIndex] = 10000;
			sortedWBGP.add(wBGP.get(minIndex));
		}
		return sortedWBGP;
	}
	
	public int getPatternOrder(Statement pattern) {
		String subject = pattern.getSubject().toString();
		String predicate = pattern.getPredicate().toString();
		String object = pattern.getObject().toString();
		int questionMarkNum = 0;
		if(subject.startsWith("?")) {
			questionMarkNum++;
		}
		if(predicate.startsWith("?")) {
			questionMarkNum++;
		}
		if(object.startsWith("?")) {
			questionMarkNum++;
		}
		switch(questionMarkNum) {
			case 0 : return 1;
			case 1 :
				if(subject.startsWith("?")) {
					return 2;
				} else if(object.startsWith("?")) {
					return 3;
				} else if(predicate.startsWith("?")) {
					return 4;
				}
			case 2 :
				if(subject.startsWith("?") && object.startsWith("?")) {
					return 5;
				} else if(subject.startsWith("?") && predicate.startsWith("?")) {
					return 6;
				} else if(predicate.startsWith("?") && object.startsWith("?")) {
					return 7;
				}
			case 3 : return 8;
			default : break;
		}
		return 0;
	}

	public boolean isMWBGPWork(ArrayList<Statement> mWBGP) {
		boolean mWBGPWork = false;
		UserQuery userQuery = new UserQuery();
		String query = userQuery.constructWhyNotSparql(mWBGP);
		JenaAPI jena = new JenaAPI(datasetpath);
		ResultSet result = jena.QueryTDB(query);
		if(result.hasNext()) {
			mWBGPWork = true;
		}
		return mWBGPWork;
	}
	
	public static void main(String[] args) {
		String sparql = "";
		String keyword = "";
		String indexPath = "D:/workspace/WhyNot/index_ooi_name_label";
		LuceneAPI lucene = new LuceneAPI(indexPath);
		List<String> uriList = lucene.SearchIndex(keyword, "object");
		AnnaSystem anna = new AnnaSystem();
		for(String uri : uriList) {
			ArrayList<Statement> wBGP = anna.constructWBGP(sparql, uri);
			ArrayList<Statement> mWBGP = anna.modifyWBGP(wBGP);
			for(Statement stmt : mWBGP) {
				System.out.println(stmt);
			}
		}
	}

}
