package it.unina.thesisrepo.testset.manager;

import it.unina.thesisrepo.matchers.SynsetUtility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import me.xdrop.fuzzywuzzy.FuzzySearch;

import org.simmetrics.metrics.JaccardSimilarity;
import org.simmetrics.metrics.Levenshtein;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class TermsSetHandler 
{
	static 
	{
		System.setProperty("wordnet.database.dir", "C:\\Program Files (x86)\\WordNet\\2.1\\dict");
	}
	static WordNetDatabase wsdatabase = WordNetDatabase.getFileInstance(); 
	static ILexicalDatabase db = new NictWordNet();
	static RelatednessCalculator wprc =  new WuPalmer(db);
	static RelatednessCalculator pathrc =  new Path(db);
	
	static ArrayList<TreeSet<String>> terms_sets = new ArrayList<TreeSet<String>>(11);
	static TreeSet<String> target_terms_set = new TreeSet<String>();
	
	static TreeSet<Alignment> allAlignments = new TreeSet<Alignment>();
	
	static Levenshtein levSim = new Levenshtein();
	static JaccardSimilarity<String> jaccardSim = new JaccardSimilarity<String>();
	
	static final String alignmentFolder = "./alignments";
	
	static final String regex1 = "[-_\\s]";
	static final String regex2 = "(?<!^)(?=[A-Z])";
	
	
	public static void main(String[] args) 
	{
		WS4JConfiguration.getInstance().setMFS(true);
		
		try
		{
			TermsSetHandler termsSetHAndler = new TermsSetHandler();
			for (int i = 0; i < 11 ; i++)
			{
				System.out.println("Handling file: " + "./ontologies/"+(i+1)+".csv");
				TreeSet<String> current_set = new TreeSet<String>();
				termsSetHAndler.addSet("./ontologies/"+(i+1) + ".csv", current_set);
				terms_sets.add(current_set);
			}
			
			//for (TreeSet<String> set:terms_sets)
				//termsSetHAndler.visualizeSet(set);
			
			termsSetHAndler.createTargetSet("./ontologies/target.csv");
			//termsSetHAndler.visualizeSet(target_terms_set);
			
			// Start alignment
			int i = 0;
			
			for (TreeSet<String> current_set:terms_sets)
			{
				try
				{
					BufferedWriter file_buffer = new BufferedWriter(new FileWriter(alignmentFolder + "/" + (i+1)+".alignment"));
					System.out.println("Aligning "+ alignmentFolder + "/" + (i+1)+".alignment");
					file_buffer.write("#Alignment " + (i+1) + ". Total number of terms: " + current_set.size() + "src,dst,str,lev,jac,fuz,syn,cos,wup,path,ext,fun,exp,grd\n");
					double exactMatch = 0.0;
					double levMatch = 0.0;
					double jaccardMatch = 0.0;
					double fuzzyMatch = 0.0;
					double syno_g = 0.0;
					double cosyno_g = 0.0;
					double wupalm = 0.0;
					double path = 0.0;
					
					
					for (String src:current_set)
					{
						for (String dst:target_terms_set)
						{
							exactMatch = exactMatching(src, dst);
							file_buffer.write(src + "," + dst + "," + exactMatch + "," + levSim.compare(src.toLowerCase(), dst.toLowerCase()) + ",");
							if (i == 4 || i == 5)
							{
								jaccardMatch = Jaccard(src, dst, regex2, regex1);
								file_buffer.write("" + jaccardMatch + ",");
							}
							else
							{
								jaccardMatch = Jaccard(src, dst, regex1, regex1);
								file_buffer.write("" + jaccardMatch + ",");
							}
							
							fuzzyMatch = fuzzyMatching(src.toLowerCase(),dst.toLowerCase());
							syno_g = SynonymyGrade(src, dst);
							cosyno_g = cosynonymGrade(src, dst);
							wupalm = WuPalmerMatching(src, dst);
							path = pathSimilarity(src, dst);
							
							file_buffer.write("" + fuzzyMatch + ",");
							file_buffer.write("" + syno_g + ",");
							file_buffer.write("" + cosyno_g + ",");
							file_buffer.write("" + wupalm + ",");
							file_buffer.write("" + path + ",0.0,0.0,");
							
							if (exactMatch == 1.0 || syno_g == 1.0)
							{
								file_buffer.write("=,?\n");
							}
							else if ((wupalm > 0.6 && wupalm < 1.0) || (wupalm > 1.0 && path < 1.0)) // Compensate WS4J error on wupalm measure
							{
								file_buffer.write("-->,?\n");
							}
							else if ((wupalm < 0.6) && wupalm > 0.3)
							{
								file_buffer.write("^,?\n");
							}
							else if (path == 0.0 && jaccardMatch > 0.0)
							{
								file_buffer.write("^,?\n");
							}
							else
								file_buffer.write("<>,?\n");
						}
					}
					i++;
					file_buffer.close();
					
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			
					
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	
	
	void addSet(String file, TreeSet<String> set) throws IOException
	{
		BufferedReader file_buffer = new BufferedReader(new FileReader(file));
	    
		String line;
		line = file_buffer.readLine(); // Read comment line
		
		while((line = file_buffer.readLine()) != null)
		{
			if (line.contains("|"))
			{
				String[] pair = line.split("\\|");
				set.add(pair[0]);
			}
			else
			{
				line.trim();
				set.add(line.trim());
			}
		}
		file_buffer.close();
	}
	
	void createTargetSet(String file) throws IOException
	{
		BufferedReader file_buffer = new BufferedReader(new FileReader(file));
	    
		String line;
		line = file_buffer.readLine(); // Read comment line
		
		while((line = file_buffer.readLine()) != null)
		{
			if (line.contains("|"))
			{
				String[] pair = line.split("\\|");
				target_terms_set.add(pair[0]);
			}
			else
			{
				line.trim();
				target_terms_set.add(line.trim());
			}
		}
		file_buffer.close();
	}
	
	void visualizeSet(Set<String> set)
	{
		System.out.println("********************************* The set contains " + set.size() + " elements *****************************");
		for (String s:set)
			System.out.println(s);
	}
	
	void visualizeAlignmentSet(Set<Alignment> set)
	{
		System.out.println("********************************* The set contains " + set.size() + " elements *****************************");
		for (Alignment a:set)
			System.out.println(a);
	}
	
	static double exactMatching(String src, String dst)
	{
		if (src.compareToIgnoreCase(dst) == 0) // perfect matching
			return 1.0;
		else
			return 0.0;
	}
	
	static double fuzzyMatching(String src, String dst)
	{
		//TODO To be implemented....
		return (double)(FuzzySearch.partialRatio(src,dst)/100);
	}
	
	static double compareMultiWords(String s1, String s2, String regex)
	{
		String[] words1 = s1.split(regex);
		String[] words2 = s2.split(regex);
		
		int counter = 0;
		//System.out.println(words1.length + " " + words2.length);
		
		for (int i = 0; i < words1.length; i++)
		{
			for (int j = 0; j < words2.length; j++)
			{
				
				if (words1[i].compareToIgnoreCase(words2[j])==0)
					counter++;
			}
		}
		
		return (double)(2*((double)counter/(words1.length + words2.length)));
	}
	
	static double Jaccard(String s1, String s2, String regex1, String regex2)
	{
		String[] words1 = s1.split(regex1);
		String[] words2 = s2.split(regex2);
		
		toLowerCase(words1);
		toLowerCase(words2);
		
		Set<String> set1 = new HashSet<String>(Arrays.asList(words1));
		Set<String> set2 = new HashSet<String>(Arrays.asList(words2));
		
		return jaccardSim.compare(set1, set2);
	}
	
	static String[]  toLowerCase(String[] array)
	{
		for (int i = 0; i < array.length; i++)
		{
			array[i] = array[i].toLowerCase();
		}
		return array;
	}
	
	static double SynonymyGrade(String word1, String word2)
	{
		Synset[] synsets1 = (Synset[]) wsdatabase.getSynsets(word1, SynsetType.NOUN);
		Synset[] synsets2 = (Synset[]) wsdatabase.getSynsets(word2, SynsetType.NOUN);
		
		return (double)(SynsetUtility.calculateSynonimy(synsets1, synsets2));
		
	}
	
	static double cosynonymGrade(String word1, String word2)
	{
		Synset[] synsets1 = (Synset[]) wsdatabase.getSynsets(word1, SynsetType.NOUN);
		Synset[] synsets2 = (Synset[]) wsdatabase.getSynsets(word2, SynsetType.NOUN);
		
		return (SynsetUtility.calculateCosynonimy(synsets1, synsets2));
	}
	
	static double pathSimilarity(String word1, String word2)
	 {
		 	 
		 List<POS[]> posPairs = pathrc.getPOSPairs();
		 double maxScore = -1D;

		 for(POS[] posPair: posPairs) {
		     List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
		     List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2, posPair[1].toString());

		     for(Concept synset1: synsets1) {
		         for (Concept synset2: synsets2) {
		             Relatedness relatedness = pathrc.calcRelatednessOfSynset(synset1, synset2);
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
	static double WuPalmerMatching(String word1, String word2)
	{
		List<POS[]> posPairs = wprc.getPOSPairs();
		 double maxScore = -1D;
		 
		 //System.out.println("posPairs :" + posPairs.size());

		// for(POS[] posPair: posPairs) {
		     List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, "n");
		     List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2, "n");

		     for(Concept synset1: synsets1) {
		         for (Concept synset2: synsets2) {
		             Relatedness relatedness = wprc.calcRelatednessOfSynset(synset1, synset2);
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
	
	class Alignment implements Comparable<Alignment>
	{
		
		String src;
		String dst;
		double measures[];
		String result;
		
		Alignment(String src, String dst, double[] measures, String result)
		{
			this.src = src;
			this.dst = dst;
			this.measures = measures;
			this.result = result;
		}
		
		@Override
		public int compareTo(Alignment a) {
			// TODO Auto-generated method stub
			if (this.src.compareToIgnoreCase(a.src)==0 && this.dst.compareToIgnoreCase(a.dst)==0)
				return 0;
			else
				return 1;
		}
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return src + " " + result + " " + dst;
		}
	}
}
