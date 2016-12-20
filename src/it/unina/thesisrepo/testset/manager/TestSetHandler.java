package it.unina.thesisrepo.testset.manager;

import it.unina.thesisrepo.matchers.SynsetUtility;
import it.unina.thesisrepo.utilities.Inflector;

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

import org.atteo.evo.inflector.English;
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

public class TestSetHandler 
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
	
	static final double eqv_t = 1.0;
	static final double hyp1_t = 0.6;
	static final double hyp2_t = 0.7;
	static final double dsj_t = 0.2;
	
	static final Stemmer stemmer = new Stemmer();
	static final Inflector inflactor = new Inflector();
	
	
	public static void main(String[] args) 
	{

		//System.out.println(Jaccard("Food Product", "canned_food", regex1, regex1));
		//Apple_cake	food
		//Cheese_bun	foodstuff
		//Chocolate_Crunchies	food
		//Food Product	whiskey_neat


		//Food Product	whiskey_neat
		//Berries	food
		//Pasta	food

		
		//System.out.println(getStem("Berries"));
		
		/*System.out.println(inflactor.singularize("Focaccia"));
		System.out.println(extendedLinguisticMatching("Focaccia", "food", regex1, regex1));
		System.out.println(WuPalmerMatching("Focaccia", "food"));*/

		WS4JConfiguration.getInstance().setMFS(true);
		String line = "";
		try
		{
			System.out.println("Starting alignment...");
			BufferedReader file_buffer = new BufferedReader(new FileReader("./alignments/groundtruth.csv"));
			BufferedWriter file_buffer_writer = new BufferedWriter(new FileWriter(alignmentFolder + "/test_alignment.csv"));
			
			file_buffer_writer.write("#Test Alignment src,dst,str,lev,jac,fuz,syn,cos,wup,path,ext,fun,exp,grd\n");
		    
			
			//line = file_buffer.readLine(); // Read comment line
			
			while((line = file_buffer.readLine()) != null)
			{
				String[] tokens = line.split(";");
				String src = tokens[0];
				String dst = tokens[1];
				String ground = tokens[2];
				
				
				double exactMatch = 0.0;
				//double levMatch = 0.0;
				double jaccardMatch = 0.0;
				double fuzzyMatch = 0.0;
				double syno_g = 0.0;
				double cosyno_g = 0.0;
				double wupalm = 0.0;
				double path = 0.0;
				double ext = 0.0;
				
				
				
				exactMatch = exactMatching(src, dst);
				file_buffer_writer.write(src + "," + dst + "," + exactMatch + "," + levSim.compare(src.toLowerCase(), dst.toLowerCase()) + ",");
				if (src.contains("_") || src.contains("-") || src.contains(" "))
				{
					jaccardMatch = Jaccard(src, dst, regex1, regex1);
					file_buffer_writer.write("" + jaccardMatch + ",");
				}
				else
				{
					jaccardMatch = Jaccard(src, dst, regex2, regex1);
					file_buffer_writer.write("" + jaccardMatch + ",");
				}
				
				ext = extendedLinguisticMatching(src, dst, regex1, regex1);
				
				src = inflactor.singularize(src);
				dst = inflactor.singularize(dst);
				
				fuzzyMatch = fuzzyMatching(src.toLowerCase(),dst.toLowerCase());
				syno_g = SynonymyGrade(src, dst);
				cosyno_g = cosynonymGrade(src, dst);
				wupalm = WuPalmerMatching(src, dst);
				path = pathSimilarity(src, dst);
				
				
				file_buffer_writer.write("" + fuzzyMatch + ",");
				file_buffer_writer.write("" + syno_g + ",");
				file_buffer_writer.write("" + cosyno_g + ",");
				file_buffer_writer.write("" + wupalm + ",");
				file_buffer_writer.write("" + path + "," + ext + ",0.0,");
				
				if (exactMatch == eqv_t || syno_g == eqv_t)
				{
					file_buffer_writer.write("eqv,");
				}
				else
				{
					if (wupalm >= hyp1_t) // Compensate WS4J error on wupalm measure
					{
						file_buffer_writer.write("hypo,");
					}
					else
					{
						if (ext >= hyp2_t)
							file_buffer_writer.write("hypo,");
						else if ((ext > dsj_t) && (ext < hyp2_t))
							file_buffer_writer.write("rel,");
						else
							file_buffer_writer.write("dsj,");
					}
				}
				
				//file_buffer_writer.write("rel,");
				file_buffer_writer.write(ground+"\n");
				
			}
			
			file_buffer_writer.close();
			file_buffer.close();
					
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.out.println(line);
		}
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
		
		singolarizeArrayofString(words1);
		singolarizeArrayofString(words2);
		
		/*for (String s:words1)
			System.out.println(s);
		
		for(String s:words2)
			System.out.println(s);*/
		
		
		
		Set<String> set1 = new HashSet<String>(Arrays.asList(words1));
		Set<String> set2 = new HashSet<String>(Arrays.asList(words2));
		
		return jaccardSim.compare(set1, set2);
	}
	
	static String getStem(String s)
	{
		stemmer.add(s.toCharArray(), s.length());
		stemmer.stem();
		return new String(stemmer.getResultBuffer(), 0, stemmer.getResultLength());
	}
	
	static String[]  toLowerCase(String[] array)
	{
		for (int i = 0; i < array.length; i++)
		{
			array[i] = array[i].toLowerCase();
		}
		return array;
	}
	
	static String[] stemArrayofString(String[] array)
	{
		for (int i = 0; i < array.length; i++)
		{
			array[i] = getStem(array[i]);
		}
		return array;
	}
	
	static String[] singolarizeArrayofString(String[] array)
	{
		for (int i = 0; i < array.length; i++)
		{
			array[i] = inflactor.singularize(array[i]);
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
	
	static double extendedLinguisticMatching(String src, String dst, String regex1, String regex2)
	{
		String[] words1 = src.split(regex1);
		String[] words2 = dst.split(regex2);
		
		toLowerCase(words1);
		toLowerCase(words2);
		
		singolarizeArrayofString(words1);
		singolarizeArrayofString(words2);
		
		double max = 0.0;
		for (String s1:words1)
		{
			for(String s2:words2)
			{
				//System.out.println("WuPalmer tra: " + s1 + "," + s2);
				double v = WuPalmerMatching(s1, s2);
				if (v>max)
				{
					max = v;
				}
			}
		}
		return max;
		
		//stemArrayofString(words1);
		//stemArrayofString(words2);
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
