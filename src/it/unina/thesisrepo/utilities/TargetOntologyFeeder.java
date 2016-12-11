package it.unina.thesisrepo.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.item.Synset;

public class TargetOntologyFeeder 
{
	static final String WN_DICT_FOLDER = "C:\\Program Files (x86)\\WordNet\\2.1\\dict";
	static final String targetOntoTerminology = "ontologies/target_wordnet.csv";
	public IDictionary dictionary;
	
	TreeMap<String,String> terms = new TreeMap<String, String>();
	Vector<Triple> triples = new Vector<Triple>();
	
	public static void main(String[] args)
	{
		TargetOntologyFeeder targetOntoFeeder = new TargetOntologyFeeder();
		targetOntoFeeder.initialize();
		targetOntoFeeder.feedStartingFrom("food");
		targetOntoFeeder.feedStartingFrom("foodstuff");
		targetOntoFeeder.feedStartingFrom("drink");
		targetOntoFeeder.feedStartingFrom("production");
		targetOntoFeeder.printTerms();
		
		try
		{
			targetOntoFeeder.exportTerminology(targetOntoTerminology);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	public TargetOntologyFeeder(){}
	
	void initialize()
	{
		try 
		{
			File wordnetDir = new File(WN_DICT_FOLDER);
			
			 // construct the dictionary object and open it
			dictionary = new Dictionary (wordnetDir);
			dictionary.open();
			
		}
		catch(Exception ex) 
		{
			ex.printStackTrace();
		}
	}
	
	void feedStartingFrom(String t)
	{
		IIndexWord idxWord = dictionary.getIndexWord(t, POS.NOUN);
		List<IWordID> wordsID = idxWord.getWordIDs();
		
		for( IWordID wid : wordsID)
		{
			IWord word = dictionary.getWord(wid);
			ISynset synset = word.getSynset();
			
			List<IWord> words = synset.getWords();
			
			for( Iterator <IWord > w = words . iterator (); w. hasNext () ;)
			{
				String current_term = w.next().getLemma();
				if (!terms.containsKey((current_term)))
					terms.put(current_term, synset.getGloss());
			}	
			
			//System.out.println(synset.getGloss());
			addHyponymsTerms(synset);
		}
	}
	
	public void addHyponymsTerms(ISynset s)
	{
		
		 // get the hyponyms
		 List < ISynsetID > hyponyms = s.getRelatedSynsets(Pointer.HYPONYM);
		
		 // print out each hyponyms id and synonyms
		 List <IWord > words ;
		 for( ISynsetID sid : hyponyms)
		 {
			 words = dictionary.getSynset(sid).getWords();
			 for( Iterator <IWord > i = words . iterator (); i. hasNext () ;)
			 {
				 String current_term = i.next().getLemma();
				 if (!terms.containsKey(current_term))
					 terms.put(current_term, dictionary.getSynset(sid).getGloss());
			 }
		 }
	 }
	
	void printTerms()
	{
		System.out.println("Terms cardinality: " + terms.size());
		for (String t:terms.keySet())
			System.out.println(t + ", " + terms.get(t));
		
	}
	
	void exportTerminology(String file) throws IOException
	{
		FileWriter fileWriter = new FileWriter(file, false);
		fileWriter.append("#Target Ontology terminology\n");
		for (String t:terms.keySet())
			fileWriter.append(t + "|" + terms.get(t)+"\n");
		
		fileWriter.close();
		
	}
}
