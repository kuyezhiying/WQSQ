package similarity.api.pack;

public class SimilarityFactory {
	
	public static Similarity createSimilarityMethod(String method) {
		Similarity sim = null;
		switch(method) {
			case "structure" :
				sim = new StructureSimilarity();	//use the structure method to compute similarity
				break;
			case "lexicon" :
				sim = new LexiconSimilarity();		//use the statistical method to compute similarity
				break;
			case "combination" :
				//sim = new CombinationSimilarity();	//use the combination method to compute similarity
				break;
			default :
				System.out.println("choose the similarity computing method with error.");
		}
		return sim;
	}
}
