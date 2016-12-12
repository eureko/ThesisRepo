package it.unina.thesisrepo.matchers;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.util.DepthFinder;
import edu.cmu.lti.ws4j.util.DepthFinder.Depth;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class LinguisticMatcher 
{
	
	static 
	{
		System.setProperty("wordnet.database.dir", "C:\\Program Files (x86)\\WordNet\\2.1\\dict");
	}
	WordNetDatabase wsdatabase = WordNetDatabase.getFileInstance(); 
	
	TreeMap<String, String> srcMap = new TreeMap<String, String>();
	TreeMap<String, String> dstMap = new TreeMap<String, String>();
	
	static final String targetOntoFile = "ontologies/target.csv"; 
	
	static final int Synonymy = 0;
	static final int Cosynonymy = 1;
	static final int WuPalmer = 2;
	static final int PATH = 5;
	
	
	static double[][] alignment;
	
	static ILexicalDatabase db;
	static RelatednessCalculator rc;
	
	String pattern = "#.##";
	DecimalFormat decimalFormat = new DecimalFormat(pattern);
		
	public static void main(String[] args) 
	{
		LinguisticMatcher lexicographicMatcher = new LinguisticMatcher("ontologies/10.csv", targetOntoFile);
			
		
		 db = new NictWordNet();
		WS4JConfiguration.getInstance().setMFS(true);
		 
		 
		//lexicographicMatcher.performMatching(LinguisticMatcher.Cosynonymy);
		
		
		//lexicographicMatcher.visualizeResults(alignment);
		
		System.out.println(getPolysemy("book"));
		
		/* List<Concept> synsets1 = (List<Concept>)db.getAllConcepts("sea", "n");
		 List<Concept> synsets2 = (List<Concept>)db.getAllConcepts("lake", "n");
		 
		 DepthFinder depthFinder = new DepthFinder(db);
		 
		 System.out.println(synsets1.size());
		 System.out.println(synsets2.size());
		 
		 for (Concept src:synsets1)
		 {
			 for (Concept dst:synsets2)
			 {
				 System.out.println("src depth: " + depthFinder.getShortestDepth(src));
				 System.out.println("dst depth: " + depthFinder.getShortestDepth(dst));
				 
				 List<Depth> lcsList = depthFinder.getRelatedness( src, dst, null );
				 int depth = lcsList.get(0).depth; // sorted by depth (asc)
				 
				 System.out.println("lcs depth: " + depth);
			 }
			
		 }
		 */
		
	}
	
	public LinguisticMatcher(String src, String dst) 
	{
		try 
		{
			// import terminologies
			BufferedReader src_br = new BufferedReader(new FileReader(src));
			BufferedReader dst_br = new BufferedReader(new FileReader(dst));
		    
			String line;
			line = src_br.readLine(); // Read comment line
			
			while((line = src_br.readLine()) != null)
			{
				if (line.contains("|"))
				{
					String[] pair = line.split("|");
					srcMap.put(pair[0], pair[1]);
				}
				else
				{
					srcMap.put(line, "");
				}
			}
			
			line = dst_br.readLine(); // Read comment line
			
			while((line = dst_br.readLine()) != null)
			{
				
				if (line.contains("|"))
				{
					String[] pair = line.split("\\|");
					dstMap.put(pair[0], pair[1]);
				}
				else
				{
					dstMap.put(line, "");
				}
			}
		    
			src_br.close();
			dst_br.close();
			
			System.out.println(srcMap.keySet().size() + " " + dstMap.keySet().size());
			
			
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void performMatching(int type)
	{
		// Perform matching
		
		alignment = new double[srcMap.keySet().size()][dstMap.keySet().size()];
		int i = 0, j = 0;
		
		for (String src_item:srcMap.keySet())
		{
			for (String dst_item:dstMap.keySet())
			{
				if (type == LinguisticMatcher.Synonymy)
					alignment[i][j] = SynonymyGrade(src_item, dst_item);
				else if (type == LinguisticMatcher.Cosynonymy)
					alignment[i][j] = cosynonymGrade(src_item, dst_item);
				else if(type == LinguisticMatcher.WuPalmer)
					alignment[i][j] = linguisticMatching(src_item, dst_item);
				else if (type == LinguisticMatcher.PATH)
					alignment[i][j] = pathSimilarity(src_item, dst_item);
					
					
				j++;
			}
			j = 0;
			i++;
		}
	}
	
	 double pathSimilarity(String word1, String word2)
	 {
		 rc = new Path(db);
		 
		 List<POS[]> posPairs = rc.getPOSPairs();
		 double maxScore = -1D;

		 for(POS[] posPair: posPairs) {
		     List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
		     List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2, posPair[1].toString());

		     for(Concept synset1: synsets1) {
		         for (Concept synset2: synsets2) {
		             Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
		             double score = relatedness.getScore();
		             //System.out.println(synset1.toString() + " vs " + synset2.toString() + " -> " + score);
		             if (score > maxScore) { 
		                 maxScore = score;
		             }
		         }
		     }
		 }

		 if (maxScore == -1D) {
		     maxScore = 0.0;
		 }

		 return maxScore;
		 
	 }
	static double linguisticMatching(String word1, String word2)
	{
		rc = new WuPalmer(db);
		
		List<POS[]> posPairs = rc.getPOSPairs();
		 double maxScore = -1D;
		 
		 //System.out.println("posPairs :" + posPairs.size());

		// for(POS[] posPair: posPairs) {
		     List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, "n");
		     List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2, "n");

		     for(Concept synset1: synsets1) {
		         for (Concept synset2: synsets2) {
		             Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
		             double score = relatedness.getScore();
		             //System.out.println(synset1.toString() + " vs " + synset2.toString() + " -> " + score);
		             if (score > maxScore) { 
		                 maxScore = score;
		             }
		         }
		     }
		 //}

		 if (maxScore == -1D) {
		     maxScore = 0.0;
		 }

		 return maxScore;
	}
	
	void visualizeResults(double[][] results)
	{
		for (int row = 0; row < results.length; row++) 
		{
	        for (int column = 0; column < results[row].length;column++)
	        {
	        		System.out.print(decimalFormat.format(results[row][column]) + " ");
	        }
	        System.out.println();
	    }
	}
	
	double SynonymyGrade(String word1, String word2)
	{
		Synset[] synsets1 = (Synset[]) wsdatabase.getSynsets(word1, SynsetType.NOUN);
		Synset[] synsets2 = (Synset[]) wsdatabase.getSynsets(word2, SynsetType.NOUN);
		
		return (double)(SynsetUtility.calculateSynonimy(synsets1, synsets2));
		
	}
	
	double cosynonymGrade(String word1, String word2)
	{
		Synset[] synsets1 = (Synset[]) wsdatabase.getSynsets(word1, SynsetType.NOUN);
		Synset[] synsets2 = (Synset[]) wsdatabase.getSynsets(word2, SynsetType.NOUN);
		
		return (SynsetUtility.calculateCosynonimy(synsets1, synsets2));
		
	}
	
	static int getPolysemy(String s)
	{
		return ((List<Concept>)db.getAllConcepts(s, "n")).size();
	}
}