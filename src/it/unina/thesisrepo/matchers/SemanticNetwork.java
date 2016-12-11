package it.unina.thesisrepo.matchers;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class SemanticNetwork 
{
	static final String WN_DICT_FOLDER = "C:\\Program Files (x86)\\WordNet\\2.1\\dict";
	static public IDictionary dictionary;
	
	TreeMap<Integer, ISynset> synsetMap = new TreeMap<Integer, ISynset>();
	static Vector<String> terms = new Vector<String>();
	
	
	public static void main(String[] args) 
	{
		try 
		{
			
			File wordnetDir = new File(WN_DICT_FOLDER);
			
			 // construct the dictionary object and open it
			dictionary = new Dictionary(wordnetDir);
			dictionary.open();
			
			SemanticNetwork sen = new SemanticNetwork();
			
			
			for (int i = 0; i < terms.size(); i++)
				System.out.println(terms.get(i));
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SemanticNetwork() 
	{
		//Access Neo4J running server 
		
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("/Users/caldarola/Documents/Neo4j/test");
		registerShutdownHook(graphDb);
		
		addTerm("food");
		
	}
	
	void addTerm(String s)
	{
		if (!terms.contains(s))
		{
			terms.add(s);
			IIndexWord idxWord = dictionary.getIndexWord(s, POS.NOUN);
			List<IWordID> wordsID = idxWord.getWordIDs();
			
			for( IWordID wid : wordsID)
			{
				IWord word = dictionary.getWord(wid);
				ISynset synset = word.getSynset();
				
				addTermsFromSynsetHyponymyHierarchy(synset);
			}
		}
	}
	
	void addTermsFromSynsetHyponymyHierarchy(ISynset s)
	{
		//System.out.println(s + ": " + s.getID() + " " + s.getID().getOffset());
		if (!synsetMap.containsKey(s.getID().getOffset()))
		{
			
			synsetMap.put(s.getID().getOffset(), s);
			
			List<IWord> words = s.getWords();
			
			for( Iterator <IWord > w = words . iterator (); w. hasNext () ;)
			{
				String current_term = w.next().getLemma();
				if (!terms.contains(current_term))
					terms.add(current_term);
			}	
			
			List < ISynsetID > hyponyms = s.getRelatedSynsets(Pointer.HYPONYM);
				
			 // print out each hyponyms id and synonyms
			 for( ISynsetID sid : hyponyms)
			 {
				 addTermsFromSynsetHyponymyHierarchy(dictionary.getSynset(sid));
			 }
		}
	}
	
	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	        }
	    } );
	}
}
