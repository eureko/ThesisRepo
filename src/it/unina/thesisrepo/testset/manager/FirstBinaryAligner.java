package it.unina.thesisrepo.testset.manager;

import it.unina.thesisrepo.matchers.SynsetUtility;
import it.unina.thesisrepo.utilities.Inflector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import me.xdrop.fuzzywuzzy.FuzzySearch;

import org.simmetrics.metrics.JaccardSimilarity;
import org.simmetrics.metrics.Levenshtein;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.DepthFinder;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class FirstBinaryAligner 
{
	static 
	{
		System.setProperty("wordnet.database.dir", "C:\\Program Files (x86)\\WordNet\\2.1\\dict");
		WS4JConfiguration.getInstance().setMFS(true);
	}
	static WordNetDatabase wsdatabase = WordNetDatabase.getFileInstance(); 
	static ILexicalDatabase db = new NictWordNet();
	static RelatednessCalculator wprc =  new WuPalmer(db);
	static RelatednessCalculator pathrc =  new Path(db);
	static RelatednessCalculator leskrc = new Lesk(db);
	static RelatednessCalculator hsorc = new HirstStOnge(db);
	static DepthFinder depthFinder = new DepthFinder(db);
	
	static ArrayList<TreeSet<String>> terms_sets = new ArrayList<TreeSet<String>>(11);
	static TreeSet<String> target_terms_set = new TreeSet<String>();
	
	static TreeSet<Alignment> allAlignments = new TreeSet<Alignment>();
	
	static Levenshtein levSim = new Levenshtein();
	static JaccardSimilarity<String> jaccardSim = new JaccardSimilarity<String>();
	
	static final String alignmentFolder = "./alignments";
	
	static final String regex1 = "[-_/\\s]";
	static final String regex2 = "(?<!^)(?=[A-Z])";
	
	double eqv_t = 1.0;
	double hyp1_t = 0.5;
	double dsj_t = 0.4;
	
	static final Stemmer stemmer = new Stemmer();
	static final Inflector inflactor = new Inflector();
	
	static final String[] singularizerStoWordList = {"pasta", "sangria", "ricotta", "focaccia"};
	static final Vector<String> singularizedStopWordListObj = new Vector<String>();
	
	String measuresFile;
	String outputfileName;
	
	public static void main(String[] args) {
		
		System.out.println(getAverageDepth("beverage"));
	}
	
	public FirstBinaryAligner(double eqv_t, double hyp1_t, String measuresFile, String fileName) 
	{
		this.eqv_t = eqv_t;
		this.hyp1_t = hyp1_t;
		
		this.measuresFile = measuresFile;
		this.outputfileName = fileName;
		
		for (int i = 0; i < singularizerStoWordList.length; i++)
			singularizedStopWordListObj.add(singularizerStoWordList[i]);
	}
	
	public void startAligning()
	{
		String line = "";
		try
		{
			//System.out.println("Starting binary alignment...");
			BufferedReader file_buffer = new BufferedReader(new FileReader(measuresFile));
			BufferedWriter file_buffer_writer = new BufferedWriter(new FileWriter(outputfileName));
			file_buffer_writer.write("#Test Alignment src,dst,str,lev,jac,fuz,syn,cos,wup,path,extWup,exp,ground\n");
		    
			
			line = file_buffer.readLine(); // Read comment line
			
			while((line = file_buffer.readLine()) != null)
			{
				String[] tokens = line.split(",");
				String src = tokens[0];
				String dst = tokens[1];
				
				
				double exactMatch = Double.parseDouble(tokens[2]);
				double levMatch = Double.parseDouble(tokens[3]);
				double jaccardMatch = Double.parseDouble(tokens[4]);
				double fuzzyMatch = Double.parseDouble(tokens[5]);
				double syno_g = Double.parseDouble(tokens[6]);
				double cosyno_g = Double.parseDouble(tokens[7]);
				double wupalm = Double.parseDouble(tokens[8]);;
				double path = Double.parseDouble(tokens[9]);
				double extWup = Double.parseDouble(tokens[10]);
				String ground = tokens[11];
				//double extlesk = 0.0;
				//double exthso = 0.0;
				
				boolean srcUnknown = (tokens[12].compareTo("unknown")==0)?true:false;
				boolean dstUnknown = (tokens[13].compareTo("unknown")==0)?true:false;
				
				file_buffer_writer.write(src + "," + dst + "," +exactMatch + "," + levMatch + "," +
				jaccardMatch + "," + fuzzyMatch + "," + syno_g + "," + cosyno_g + "," + wupalm + "," + path + "," +
						extWup + ",");
				
				//First binary decision tree
				if (exactMatch == eqv_t || syno_g == eqv_t || jaccardMatch == 1.0)
				{
					file_buffer_writer.write("eqv,");
				}
				else
				{
					if (srcUnknown || dstUnknown)
						file_buffer_writer.write("unknown,");
					else
					{
						if ((wupalm >= hyp1_t || extWup >= hyp1_t)) // Compensate WS4J error on wupalm measure
						{
							file_buffer_writer.write("hypo+,");
						}
						else
							file_buffer_writer.write("rel-,");
					}
				}
				file_buffer_writer.write(ground + "\n");
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
			if (!singularizedStopWordListObj.contains(array[i].toLowerCase()))
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
			 
			  //System.out.println(posPairs.size() + ": " + posPair[0].toString() + "," + posPair[1].toString());
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
	
	static double leskSimilarity(String word1, String word2)
	 {
		 	 
		 List<POS[]> posPairs = leskrc.getPOSPairs();
		 double maxScore = -1D;

		 for(POS[] posPair: posPairs) {
			 
			 //System.out.println(posPairs.size() + ": " + posPair[0].toString() + "," + posPair[1].toString());
		     List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
		     List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2, posPair[1].toString());

		     for(Concept synset1: synsets1) {
		         for (Concept synset2: synsets2) {
		             Relatedness relatedness = leskrc.calcRelatednessOfSynset(synset1, synset2);
		             double score = relatedness.getScore();
		             //System.out.println(synset1.toString() + " vs " + synset2.toString() + " -> " + score);
		             if (score > maxScore) { 
		                 maxScore = score;
		             }
		         }
		     }
		 }

		 /*if (maxScore == -1D) {
		     maxScore = 0.0;
		 }*/

		 return maxScore;
		 
	 }
	
	static double HirstSimilarity(String word1, String word2)
	 {
		 	 
		 List<POS[]> posPairs = hsorc.getPOSPairs();
		 double maxScore = -1D;

		 for(POS[] posPair: posPairs) {
			 
			 //System.out.println(posPairs.size() + ": " + posPair[0].toString() + "," + posPair[1].toString());
		     List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
		     List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2, posPair[1].toString());

		     for(Concept synset1: synsets1) {
		         for (Concept synset2: synsets2) {
		             Relatedness relatedness = hsorc.calcRelatednessOfSynset(synset1, synset2);
		             double score = relatedness.getScore();
		             //System.out.println(synset1.toString() + " vs " + synset2.toString() + " -> " + score);
		             if (score > maxScore) { 
		                 maxScore = score;
		             }
		         }
		     }
		 }

		/* if (maxScore == -1D) {
		     maxScore = 0.0;
		 }*/

		 return maxScore;
	 }
	
	static double WuPalmerMatching(String word1, String word2)
	{
		List<POS[]> posPairs = wprc.getPOSPairs();
		 double maxScore = -1D;
		 
		 //System.out.println("posPairs :" + posPairs.size());
		

		 for(POS[] posPair: posPairs) {
		    
			 //System.out.println(posPairs.size() + ": " + posPair[0].toString() + "," + posPair[1].toString());
			 List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1,  posPair[0].toString());
		     List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2,  posPair[1].toString());

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
		 }

		 if (maxScore == -1D) {
		     maxScore = 0.0;
		 }

		 return maxScore;
	}
	
	static double extendedMaxWuPalmerMatching(String src, String dst, String regex1, String regex2)
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
	
	static double extendedMinWuPalmerMatching(String src, String dst, String regex1, String regex2)
	{
		String[] words1 = src.split(regex1);
		String[] words2 = dst.split(regex2);
		
		toLowerCase(words1);
		toLowerCase(words2);
		
		singolarizeArrayofString(words1);
		singolarizeArrayofString(words2);
		
		double min = 1.0;
		for (String s1:words1)
		{
			for(String s2:words2)
			{
				//System.out.println("WuPalmer tra: " + s1 + "," + s2);
				double v = WuPalmerMatching(s1, s2);
				if (v < min)
				{
					min = v;
				}
			}
		}
		return min;
		
		//stemArrayofString(words1);
		//stemArrayofString(words2);
	}
	
	static double extendedAvarageWuPalmerMatching(String src, String dst, String regex1, String regex2)
	{
		String[] words1 = src.split(regex1);
		String[] words2 = dst.split(regex2);
		
		toLowerCase(words1);
		toLowerCase(words2);
		
		singolarizeArrayofString(words1);
		singolarizeArrayofString(words2);
		
		double sum = 0.0;
		int count = 0;
		for (String s1:words1)
		{
			for(String s2:words2)
			{
				//System.out.println("WuPalmer tra: " + s1 + "," + s2);
				
				double v = WuPalmerMatching(s1, s2);
				if (v > 0) count++;
				sum += v;
			}
		}
		return sum/count;
		
		//stemArrayofString(words1);
		//stemArrayofString(words2);
	}
	
	static double extendedRestrictedWuPalmerMatching(String src, String dst, String regex1, String regex2)
	{
		String[] words1;
		String[] words2;
		
		if (src.contains(" and "))
		{
			words1 = src.split(" and ");
		}
		else
			words1 = src.split(regex1);
		
		if (dst.contains(" and "))
		{
			words2 = dst.split(" and ");
			//System.out.println(words2);
		}
		else
			words2 = dst.split(regex1);
		
		//for (String s:words1)
			//System.out.println(s);
		
		//for (String s:words2)
			//System.out.println(s);
		
		int srcTokenArraySize = words1.length;
		int dstTokenArraySize = words2.length;
		
		//System.out.println(srcTokenArraySize + " " + dstTokenArraySize);
		
		int srcFrom, srcTo;
		int dstFrom, dstTo;
		
		if (src.contains(" and "))
		{
			srcFrom = 0;
			srcTo = srcTokenArraySize - 1;
		}
		else
		{
			if (srcTokenArraySize > 2)
			{
				srcFrom = srcTokenArraySize - 2;
				srcTo = srcTokenArraySize - 1;
			}
			else if (srcTokenArraySize == 2)
			{
				srcFrom = 1;
				srcTo = 1;
			}
			else
			{
				srcFrom = 0;
				srcTo = 0;
			}
		}
		
		if (dst.contains(" and "))
		{
			dstFrom = 0;
			dstTo = dstTokenArraySize - 1;
		}
		else
		{
			if (dstTokenArraySize > 2)
			{
				dstFrom = dstTokenArraySize - 2;
				dstTo = dstTokenArraySize - 1;
			}
			else if (dstTokenArraySize == 2)
			{
				dstFrom = 1;
				dstTo = 1;
			}
			else
			{
				dstFrom = 0;
				dstTo = 0;
			}
		}
		deletePunctuationsToArray(words1);
		deletePunctuationsToArray(words2);
		
		toLowerCase(words1);
		toLowerCase(words2);
		
		singolarizeArrayofString(words1);
		singolarizeArrayofString(words2);
		
		//System.out.println("Index: " + srcFrom + " " + srcTo + ";" + dstFrom + " " + dstTo);
		double max = 0.0;
		for (int i = srcFrom; i <= srcTo; i++)
		{
			for(int j = dstFrom; j <= dstTo; j++)
			{
				
				double v = WuPalmerMatching(words1[i], words2[j]);
				//System.out.println("WuPalmer tra: " + words1[i] + "," + words2[j] + "=" + v);
				if (v>max)
				{
					max = v;
				}
			}
		}
		return max;
				
		
	}
	
	static String deletePunctuations(String s)
	{
		return s.replace('(', ' ').replace(')', ' ').trim();
	}
	
	static String [] deletePunctuationsToArray(String []array)
	{
		for (int i = 0; i < array.length; i++)
		{
			array[i] = deletePunctuations(array[i]);
		}
		return array;
	}
	
	static double extendedLeskMatching(String src, String dst, String regex1, String regex2)
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
				double v = leskSimilarity(s1, s2);
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
	
	static double extendedHirstMatching(String src, String dst, String regex1, String regex2)
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
				double v = HirstSimilarity(s1, s2);
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
	
	static double getAverageDepth(String src)
	{
		double sum = 0.0;
		
		List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(src, "n");
		 int i = 0;
		 
		 for (Concept c_src:synsets1)
		 {
			sum += depthFinder.getShortestDepth(c_src);
			i++;
		 }
		 if (sum != 0)
			 return sum/i;
		 else
			 return Double.MAX_VALUE;
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
