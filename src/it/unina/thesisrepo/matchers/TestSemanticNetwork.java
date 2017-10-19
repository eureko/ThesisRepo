package it.unina.thesisrepo.matchers;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.util.DepthFinder;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class TestSemanticNetwork 
{
	static final String WN_DICT_FOLDER = "C:\\Program Files (x86)\\WordNet\\2.1\\dict";
	static public IDictionary dictionary;
	
	TreeMap<Integer, ISynset> synsetMap = new TreeMap<Integer, ISynset>();
	//TreeMap<Integer, Node>
		
	static GraphDatabaseService graphDb;
	
	Label synsetLabel = DynamicLabel.label("Synset");;
	Label wordLabel = DynamicLabel.label("Word");
	
	static ILexicalDatabase db = new NictWordNet();
	DepthFinder depthFinder = new DepthFinder(db);
	
	TreeMap<String, Node> termsNodeMap = new TreeMap<String, Node>();
	TreeMap<Integer, Node> synsetNodeMap = new TreeMap<Integer, Node>();
	
	Vector<String> terms = new Vector<String>();		
	
	public static void main(String[] args) 
	{
		try 
		{
			WS4JConfiguration.getInstance().setMFS(true);
			File wordnetDir = new File(WN_DICT_FOLDER);
			
			 // construct the dictionary object and open it
			dictionary = new Dictionary(wordnetDir);
			dictionary.open();
			
			
			TestSemanticNetwork sen = new TestSemanticNetwork();
			sen.createSN("/Users/caldarola/Documents/Neo4j/test");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public TestSemanticNetwork() {}
	
	public void createSN(String graphDB)
	{
		//Access Neo4J running server 
		
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(graphDB);
			registerShutdownHook(graphDb);
			
			System.out.println("Hook Registered");
			
			try ( Transaction tx = graphDb.beginTx() )
			{
				addTerm("food");				
								
				System.out.println("Before commit");
				tx.success();
				System.out.println("After commit");
			}
			graphDb.shutdown();
			System.out.println("OK!");
			
	}
	
	void addTerm(String s)
	{
		if (!terms.contains(s))
		{
			terms.add(s);
			System.out.println("Adding terms: " + s);			
			IIndexWord idxWord = dictionary.getIndexWord(s, POS.NOUN);
			if (idxWord != null)
			{
				List<IWordID> wordsID = idxWord.getWordIDs();
				
				for( IWordID wid : wordsID)
				{
					IWord word = dictionary.getWord(wid);
					ISynset synset = word.getSynset();
					
					Node s_node = graphDb.createNode();
					long nodeId = s_node.getId();	
					
					s_node.setProperty("label", synset.getWords().toString());
					s_node.setProperty("lemma", synset.getID().toString());
					s_node.setProperty("type", "synset");
					s_node.addLabel(synsetLabel);
					
					
					//addMeronyms(synset, s_node, Pointer.MERONYM_SUBSTANCE);
					//addMeronyms(synset, s_node, Pointer.MERONYM_MEMBER);
					addTermsFromSynsetHyponymyHierarchy(synset, s_node);
					
					addMeronyms(synset, s_node, Pointer.MERONYM_PART);
				}
			}
			else
			{
				System.out.print(" not found!\n");
			}
		}
		else
		{
			System.out.println(s + " already existing!");
		}
	}
	
	void addMeronyms(ISynset s, Node n, Pointer p)
	{
		List < ISynsetID > meronyms = s.getRelatedSynsets(p);
		
		 // print out each hyponyms id and synonyms
		 for( ISynsetID sid : meronyms)
		 {
			ISynset meronym = dictionary.getSynset(sid);
			Node mero_node = null;
			if (!synsetNodeMap.containsKey(meronym.getID().getOffset()))
			{
				mero_node = graphDb.createNode();
				long mero_nodeId = mero_node.getId();	
					
				mero_node.setProperty("label", meronym.getWords().toString());
				mero_node.setProperty("lemma", meronym.getID().toString());
				mero_node.setProperty("type", "synset");
				mero_node.addLabel(synsetLabel);
				
				synsetNodeMap.put(meronym.getID().getOffset(), mero_node);
			}
			else
				 mero_node = synsetNodeMap.get(meronym.getID().getOffset());
			
				
			Relationship relationship = n.createRelationshipTo(mero_node, RelTypes.Meronym);
			Relationship inv_relationship = mero_node.createRelationshipTo(n, RelTypes.Holonym);
			relationship.setProperty("type", "semantic");
			relationship.setProperty("name", "meronym");
			relationship.setProperty("w", 0.5);
			inv_relationship.setProperty("type", "semantic");
			inv_relationship.setProperty("name", "holonym");
			inv_relationship.setProperty("w", 0.25);
			
			List<IWord> words = meronym.getWords();
			
			for( Iterator <IWord > w = words . iterator (); w. hasNext () ;)
			{
				String current_term = w.next().getLemma();
				
				Node w_node = null;
				if (!termsNodeMap.containsKey(current_term))
				{
					w_node = graphDb.createNode();
					long w_nodeId = w_node.getId();		
					w_node.setProperty("lemma", current_term);
					w_node.setProperty("type", "word");
					w_node.addLabel(wordLabel);
					 termsNodeMap.put(current_term, w_node);
				}
				else
					w_node = termsNodeMap.get(current_term);
				
							
				Relationship w_relationship = mero_node.createRelationshipTo(w_node, RelTypes.hasWord);
				w_relationship.setProperty("type", "meta");
				w_relationship.setProperty("name", "hasWord");
				w_relationship.setProperty("w", 0);
				
			}
			
		 }
	}
	
	void addTermsFromSynsetHyponymyHierarchy(ISynset s, Node s_node)
	{
		//System.out.println(s + ": " + s.getID() + " " + s.getID().getOffset());
		if (!synsetNodeMap.containsKey(s.getID().getOffset()))
		{
			synsetNodeMap.put(s.getID().getOffset(), s_node);
			
						
			List<IWord> words = s.getWords();
			
			for( Iterator <IWord > w = words . iterator (); w. hasNext () ;)
			{
				String current_term = w.next().getLemma();
				
				Node w_node = null;
				if (!termsNodeMap.containsKey(current_term))
				{
					w_node = graphDb.createNode();
					long w_nodeId = w_node.getId();		
					w_node.setProperty("lemma", current_term);
					w_node.setProperty("type", "word");
					w_node.addLabel(wordLabel);
					termsNodeMap.put(current_term, w_node);
				}
				else
					w_node = termsNodeMap.get(current_term);
				
							
				Relationship relationship = s_node.createRelationshipTo(w_node, RelTypes.hasWord);
				relationship.setProperty("type", "meta");
				relationship.setProperty("name", "hasWord");
				relationship.setProperty("w", 0);
				
			}	
			
			List < ISynsetID > hyponyms = s.getRelatedSynsets(Pointer.HYPONYM);
			
				
			 // print out each hyponyms id and synonyms
			 for( ISynsetID sid : hyponyms)
			 {
				ISynset hyponym = dictionary.getSynset(sid);
				
				Node hypo_node = graphDb.createNode();
				long hypo_nodeId = hypo_node.getId();	
					
				hypo_node.setProperty("label", hyponym.getWords().toString());
				hypo_node.setProperty("lemma", hyponym.getID().toString());
				hypo_node.setProperty("type", "synset");
				hypo_node.addLabel(synsetLabel);
				
				Relationship relationship = s_node.createRelationshipTo(hypo_node, RelTypes.Hyponym);
				relationship.setProperty("type", "semantic");
				relationship.setProperty("name", "hyponym");
				relationship.setProperty("w", 0.75);
				
				addTermsFromSynsetHyponymyHierarchy(hyponym, hypo_node);
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
	
	private static enum RelTypes implements RelationshipType
	{
	    hasWord,
	    Hyponym,
	    Meronym,
	    Holonym,
	    Synonym
	}
}
