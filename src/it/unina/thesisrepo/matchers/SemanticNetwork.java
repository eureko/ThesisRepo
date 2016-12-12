package it.unina.thesisrepo.matchers;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.Traversal;

import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.util.DepthFinder;
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
	//TreeMap<Integer, Node>
	static Vector<String> terms = new Vector<String>();
	
	GraphDatabaseService graphDb;
	
	Label synsetLabel = DynamicLabel.label("Synset");;
	Label wordLabel = DynamicLabel.label("Word");
	
	double sg_alpha = 0.5;
	double sg_beta = 0.5;
	
	public static void main(String[] args) 
	{
		try 
		{
			
			File wordnetDir = new File(WN_DICT_FOLDER);
			
			 // construct the dictionary object and open it
			dictionary = new Dictionary(wordnetDir);
			dictionary.open();
			
			SemanticNetwork sen = new SemanticNetwork();
			
			
			//for (int i = 0; i < terms.size(); i++)
				//System.out.println(terms.get(i));
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SemanticNetwork() 
	{
		//Access Neo4J running server 
		
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("/Users/caldarola/Documents/Neo4j/test");
		registerShutdownHook(graphDb);
		
		System.out.println("Hook Registered");
		
		try ( Transaction tx = graphDb.beginTx() )
		{
			addTerm("food");
			//addTerm("foodstuff");
			System.out.println("Before commit");
			tx.success();
			System.out.println("After commit");
			
			// Read Access
			System.out.println("Traversing the graph...");
			
			ResourceIterator<Node> src_iter = graphDb.findNodes(wordLabel, "lemma", "food");
			ResourceIterator<Node> dst_iter = graphDb.findNodes(wordLabel, "lemma", "water");
			
			
			while(src_iter.hasNext())
			{
				Node current_src = src_iter.next();
				while(dst_iter.hasNext())
				{
					Node current_dst = dst_iter.next();
					
					PathFinder<WeightedPath> shortestPathFinder = GraphAlgoFactory.dijkstra(PathExpanders.allTypesAndDirections(), "w");
					Iterable<WeightedPath> paths = shortestPathFinder.findAllPaths( current_src, current_dst );
					
					//System.out.println(paths);
					Iterator<WeightedPath> iter_path = paths.iterator();
					while(iter_path.hasNext())
					{
						WeightedPath p = iter_path.next();
						System.out.print(p + "\n");
					}
				}
			}
		}
		graphDb.shutdown();
		System.out.println("OK!");
		   
				
				
	}
	
	void addTerm(String s)
	{
		if (!terms.contains(s))
		{
			terms.add(s);
			
			/*Node w_node = graphDb.createNode();
			long w_nodeId = w_node.getId();	
			
			w_node.setProperty("lemma", s);
			w_node.setProperty("type", "word");
			w_node.addLabel(wordLabel);*/
			
			
			IIndexWord idxWord = dictionary.getIndexWord(s, POS.NOUN);
			List<IWordID> wordsID = idxWord.getWordIDs();
			
			for( IWordID wid : wordsID)
			{
				IWord word = dictionary.getWord(wid);
				ISynset synset = word.getSynset();
				
				Node s_node = graphDb.createNode();
				long nodeId = s_node.getId();	
				
				s_node.setProperty("label", synset.getWords().toString());
				s_node.setProperty("type", "synset");
				s_node.addLabel(synsetLabel);
				
				addTermsFromSynsetHyponymyHierarchy(synset, s_node);
			}
		}
	}
	
	void addTermsFromSynsetHyponymyHierarchy(ISynset s, Node s_node)
	{
		//System.out.println(s + ": " + s.getID() + " " + s.getID().getOffset());
		if (!synsetMap.containsKey(s.getID().getOffset()))
		{
			synsetMap.put(s.getID().getOffset(), s);
			
						
			List<IWord> words = s.getWords();
			
			for( Iterator <IWord > w = words . iterator (); w. hasNext () ;)
			{
				String current_term = w.next().getLemma();
				
				Node w_node = graphDb.createNode();
				long w_nodeId = w_node.getId();	
				
				w_node.setProperty("lemma", current_term);
				w_node.setProperty("type", "word");
				w_node.addLabel(wordLabel);
				
				Relationship relationship = s_node.createRelationshipTo(w_node, RelTypes.hasWord);
				relationship.setProperty("type", "meta");
				relationship.setProperty("name", "hasWord");
				relationship.setProperty("w", 0);
				
				
				if (!terms.contains(current_term))
				{
					terms.add(current_term);
				}
			}	
			
			List < ISynsetID > hyponyms = s.getRelatedSynsets(Pointer.HYPONYM);
			
				
			 // print out each hyponyms id and synonyms
			 for( ISynsetID sid : hyponyms)
			 {
				ISynset hyponym = dictionary.getSynset(sid);
				
				Node hypo_node = graphDb.createNode();
				long hypo_nodeId = hypo_node.getId();	
					
				hypo_node.setProperty("label", hyponym.getWords().toString());
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
	
	double SemanticGradeCalc(double l, int d)
	{
		return Math.exp(-sg_alpha*l)*(Math.exp(sg_beta*d) - Math.exp(-sg_beta*d))/(Math.exp(sg_beta*d) + Math.exp(-sg_beta*d));
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
	    Synonym
	}
}
