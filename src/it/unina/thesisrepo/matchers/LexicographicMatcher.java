package it.unina.thesisrepo.matchers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import me.xdrop.fuzzywuzzy.FuzzySearch;

import org.simmetrics.metrics.JaccardSimilarity;
import org.simmetrics.metrics.Levenshtein;

public class LexicographicMatcher 
{
	static final int EXACT_MATCHTING = 0;
	static final int MULTIWORD_MATCHTING = 1;
	static final int LEVENSHTEIN = 2;
	static final int JACCARD = 3;
	static final int FUZZY = 4;
	
	TreeMap<String, String> srcMap = new TreeMap<String, String>();
	TreeMap<String, String> dstMap = new TreeMap<String, String>();
	
	static final String targetOntoFile = "ontologies/target.csv"; 
	
	static double[][] exactStringAlignment;
	
	static final String regex1 = "[-_\\s]";
	static final String regex2 = "(?<!^)(?=[A-Z])";
	
	Levenshtein levSim = new Levenshtein();
	static JaccardSimilarity<String> jaccardSim = new JaccardSimilarity<String>();
	
		
	public static void main(String[] args) 
	{
		//LexicographicMatcher lexicographicMatcher = new LexicographicMatcher("ontologies/10.csv", targetOntoFile);
			
		System.out.println(Jaccard("Food", "comfort_food", regex1, regex1));
		//lexicographicMatcher.performMatching(LexicographicMatcher.FUZZY);
		//lexicographicMatcher.visualizeResults(exactStringAlignment);
		
			

	}
	
	public LexicographicMatcher(String src, String dst) 
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
		
		exactStringAlignment = new double[srcMap.keySet().size()][dstMap.keySet().size()];
		int i = 0, j = 0;
		
		for (String src_item:srcMap.keySet())
		{
			for (String dst_item:dstMap.keySet())
			{
				if (type == LexicographicMatcher.EXACT_MATCHTING)
					exactStringAlignment[i][j] = exactMatching(src_item, dst_item);
				else if (type == LexicographicMatcher.MULTIWORD_MATCHTING)
					exactStringAlignment[i][j] = compareMultiWords(src_item, dst_item, regex1);
				else if (type == LexicographicMatcher.LEVENSHTEIN)
					exactStringAlignment[i][j] = levSim.compare(src_item, dst_item);
				else if (type == LexicographicMatcher.JACCARD)
					exactStringAlignment[i][j] = Jaccard(src_item, dst_item, regex1, regex1);
				else if (type == LexicographicMatcher.FUZZY)
					exactStringAlignment[i][j] = FuzzySearch.tokenSetPartialRatio(src_item, dst_item);
					
				j++;
			}
			j = 0;
			i++;
		}
	}
	
	
	
	double exactMatching(String src, String dst)
	{
		if (src.compareToIgnoreCase(dst) == 0) // perfect matching
			return 1.0;
		else
			return 0.0;
	}
	
	double fuzzyMatching(String src, String dst)
	{
		//TODO To be implemented....
		return 0.0;
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
	
	void visualizeResults(double[][] results)
	{
		for (int row = 0; row < results.length; row++) 
		{
	        for (int column = 0; column < results[row].length;column++)
	        	System.out.print(results[row][column] + " ");
	        
	        System.out.println();
	    }
	}
}
