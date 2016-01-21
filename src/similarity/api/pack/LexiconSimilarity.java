package similarity.api.pack;

import com.hp.hpl.jena.rdf.model.*;

public class LexiconSimilarity extends Similarity {

	@Override
	public float computeSimilarity(Statement triple1, Statement triple2) {
		float score = 0;
		float score1 = 0; //SimilarityOfNodes(triple1.getSubject(), triple2.getSubject()) / 3;		//the similarity between subjects
		float score2 = SimilarityOfNodes(triple1.getPredicate(), triple2.getPredicate()) / 3;		//the similarity between predicates
		float score3 = SimilarityOfNodes(triple1.getObject(), triple2.getObject()) / 3 ;			//the similarity between objects
		score = score1 + score2 + score3;		//the similarity between triples
		//System.out.println("triple similarity score : " + score);
		return score;
	}

	//the similarity between subjects
	public float SimilarityOfNodes(Resource res1, Resource res2) {
		LexiconComputation lexicon = new LexiconComputation();
		float res1LM = lexicon.constructEntityLM(res1.toString());		//language model of entity(res1's distribution)
		float res2LM = lexicon.constructEntityLM(res1.toString());		//language model of entity(res2's distribution)
		float score = 0;
		if(res1LM == 0 || res2LM == 0) {
			return 0;
		}
		else {
			float middleLM = (res1LM + res2LM) / 2;
			score = (float) Math.sqrt((float)(res1LM * Math.log10(res1LM / middleLM) + res2LM * Math.log10(res2LM / middleLM)));		//the distance between two entity language models as similarity score
		}
		//System.out.println("subject similarity score : " + score);
		return score;
	}
	
	//the similarity between predicates
	public float SimilarityOfNodes(Property prop1, Property prop2) {
		if(prop1 == prop2) {
			return 1.0f;	//if prop1 and prop2 is the same property, assign its similarity score to 1.0
		}
		LexiconComputation lexicon = new LexiconComputation();
		float prop1LM = lexicon.constructRelationLM(prop1.toString());		//language model of relation(prop1's distribution)
		float prop2LM = lexicon.constructRelationLM(prop2.toString());		//language model of relation(prop2's distribution)
		float middleLM = (prop1LM + prop2LM) / 2;
		if(prop1LM == 0 || prop2LM == 0) {
			return 0;
		}
		float middleRes = (float)(prop1LM * Math.log10(prop1LM / middleLM) + prop2LM * Math.log10(prop2LM / middleLM));
		float score = (float) Math.sqrt(middleRes);	//the distance between two relation language models as similarity score
		//System.out.println("predicate similarity score : " + score);
		return score;
	}
	
	//the similarity between objects
	public float SimilarityOfNodes(RDFNode obj1, RDFNode obj2) {
		float score = 0;
		if(obj1.isResource() && obj2.isResource()) {
			if(obj1 == obj2) {
				return 1.0f;	//if obj1 and obj2 is the same entity, assign its similarity score to 1.0
			}
			LexiconComputation lexicon = new LexiconComputation();
			float obj1LM = lexicon.constructEntityLM(obj1.toString());		//language model of entity(obj1's distribution)
			float obj2LM = lexicon.constructEntityLM(obj1.toString());		//language model of entity(obj2's distribution)
			float middleLM = (obj1LM + obj2LM) / 2;
			score = (float) Math.sqrt((obj1LM * Math.log10(obj1LM / middleLM) + obj2LM * Math.log10(obj2LM / middleLM)));		//the distance between two entity language models as similarity score		
		}
		else if(obj1.isLiteral() && obj2.isLiteral()) {
			String str1 = obj1.toString().substring(obj1.toString().indexOf("\"") + 1, obj1.toString().indexOf("\"", 1));
			String str2 = obj2.toString().substring(obj2.toString().indexOf("\"") + 1, obj2.toString().indexOf("\"", 1));
			score = SimilarityOfLiterals(str1, str2);
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
	
	public static void main(String[] args) {
		String str1 = "French";
		String str2 = "Alain Connes@en";
		LexiconSimilarity lexiconSim = new LexiconSimilarity();
		float sim = lexiconSim.SimilarityOfLiterals(str1, str2);
		System.out.println(sim);
	}
}