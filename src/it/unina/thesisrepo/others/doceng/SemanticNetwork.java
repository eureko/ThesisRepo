package it.unina.thesisrepo.others.doceng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.util.DepthFinder;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.cmu.lti.ws4j.util.DepthFinder.Depth;
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
	static Vector<String> ext_target_terms = new Vector<String>();
	static Vector<String> ext_input_terms = new Vector<String>();
	
	static GraphDatabaseService graphDb;
	
	Label synsetLabel = DynamicLabel.label("Synset");;
	Label wordLabel = DynamicLabel.label("Word");
	
	double sg_alpha = 0.5;
	double sg_beta = 0.5;
	
	static Vector<String> inputLexicalChain = new Vector<String>();
	static Vector<String> targetLexicalChain = new Vector<String>();
	
	static ILexicalDatabase db = new NictWordNet();
	DepthFinder depthFinder = new DepthFinder(db);
	
	//TreeMap<String, Node> targetNodeMap = new TreeMap<String, Node>();
	//TreeMap<String, Node> inputNodeMap = new TreeMap<String, Node>();
	
	TreeMap<String, Node> termsNodeMap = new TreeMap<String, Node>();
	TreeMap<Integer, Node> synsetNodeMap = new TreeMap<Integer, Node>();
		
	
	public static void main(String[] args) 
	{
		try 
		{
			WS4JConfiguration.getInstance().setMFS(true);
			
			File wordnetDir = new File(WN_DICT_FOLDER);
			
			 // construct the dictionary object and open it
			dictionary = new Dictionary(wordnetDir);
			dictionary.open();
			
			BufferedReader file_buffer = new BufferedReader(new FileReader("./ontologies/target.csv"));
			String line = "";
			
			file_buffer.readLine(); // skip the first
			while((line = file_buffer.readLine()) != null)
			{
				String[] tokens = line.split("\\|");
				//System.out.println("*" + tokens[0] + "*");
				targetLexicalChain.add(tokens[0].toLowerCase());
			}
			file_buffer.close();
			
			
			SemanticNetwork sen = new SemanticNetwork("./ontologies/1.csv");
			sen.createSN("/Users/caldarola/Documents/Neo4j/test1");
			
			
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void retrieveShared()
	{
		System.out.println(ext_input_terms.size());
		System.out.println(ext_target_terms.size());
		
		int c = 0;
		for (String s:ext_target_terms)
		{
			if (ext_input_terms.contains(s))
			{
				Node n = termsNodeMap.get(s);
				n.setProperty("type", "sharedWord");
				c++;
			}
		}
		
		System.out.println(c);
	}
	
	public SemanticNetwork(String inputLexicalChainPath) 
	{
		try
		{
			BufferedReader file_buffer = new BufferedReader(new FileReader(inputLexicalChainPath));
			String line = "";
			
			file_buffer.readLine(); // skip the first
			
			while((line = file_buffer.readLine()) != null)
			{
				//System.out.println(tokens[0]);
				String inputStr = line.trim().toLowerCase();
				//System.out.println("*"+inputStr+"*");
				if (targetLexicalChain.contains(inputStr))
					System.out.println(">" + inputStr);
				inputLexicalChain.add(inputStr);
			}
			file_buffer.close();	
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	public void createSN(String graphDB)
	{
		//Access Neo4J running server 
		
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(graphDB);
			registerShutdownHook(graphDb);
			
			System.out.println("Hook Registered");
			
			try ( Transaction tx = graphDb.beginTx() )
			{
				for (String s:inputLexicalChain)
					addTerm(s, false); 
				
				for (String s:targetLexicalChain)
					addTerm(s, true);
				
				
				
				System.out.println("Before commit");
				tx.success();
				System.out.println("After commit");
				
				/*double semantic_grade = 0.0;
				
				
				for (String src:inputLexicalChain)
				{
					for (String dst:targetLexicalChain)
					{
						//System.out.println("Find path between " + src + " " + dst);
						semantic_grade += calculatePath(src, dst);
						System.out.println(semantic_grade);
					}
				}*/
				
				retrieveShared();
			}
			graphDb.shutdown();
			System.out.println("OK!");
			
	}
	
	double calculatePath(String srcLabel, String dstLabel)
	{
		// Read Access
		//System.out.println("Traversing the graph...");
		double semantic_grade_partial = 0.0;
		ResourceIterator<Node> src_iter = graphDb.findNodes(wordLabel, "lemma", srcLabel);
		ResourceIterator<Node> dst_iter = graphDb.findNodes(wordLabel, "lemma", dstLabel);
		
		PathFinder<WeightedPath> shortestPathFinder = GraphAlgoFactory.dijkstra(PathExpanders.allTypesAndDirections(), "w");
		
		while(src_iter.hasNext())
		{
			Node current_src = src_iter.next();
			while(dst_iter.hasNext())
			{
				Node current_dst = dst_iter.next();
							
				Iterable<WeightedPath> paths = shortestPathFinder.findAllPaths( current_src, current_dst );
				
				//System.out.println(paths);
				Iterator<WeightedPath> iter_path = paths.iterator();
				while(iter_path.hasNext())
				{
					WeightedPath p = iter_path.next();
					//p.weight();
					System.out.print("Short Path between: " + srcLabel + " and " + dstLabel + " (weight: " + p.weight()+ ")\n");
					semantic_grade_partial += SemanticGradeCalc(p.weight(), lcsDepth(srcLabel, dstLabel));
					//System.out.println(semantic_grade_partial);
					
				}
			}
		}
		return semantic_grade_partial;
	}
	
	void addTerm(String s, boolean target)
	{
		System.out.println("Adding terms: " + s);
		
		if (target)
			ext_target_terms.add(s);
		else
			ext_input_terms.add(s);
		
		if (!terms.contains(s))
		{
			terms.add(s);
			
						
			/*Node w_node = graphDb.createNode();
			long w_nodeId = w_node.getId();	
			
			w_node.setProperty("lemma", s);
			w_node.setProperty("type", "word");
			w_node.addLabel(wordLabel);*/
			
			
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
					
					addMeronyms(synset, s_node, Pointer.MERONYM_PART);
					addMeronyms(synset, s_node, Pointer.MERONYM_SUBSTANCE);
					addMeronyms(synset, s_node, Pointer.MERONYM_MEMBER);
					addTermsFromSynsetHyponymyHierarchy(synset, s_node, target);
				}
			}
			else
			{
				System.out.print(" not found!\n");
			}
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
	
	void addTermsFromSynsetHyponymyHierarchy(ISynset s, Node s_node, boolean target)
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
				
				if (target)
				{
					if (!ext_target_terms.contains(current_term))
						ext_target_terms.add(current_term);
				}
				else
				{
					if (!ext_input_terms.contains(current_term))
						ext_input_terms.add(current_term);
				}
								
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
				hypo_node.setProperty("lemma", hyponym.getID().toString());
				hypo_node.setProperty("type", "synset");
				hypo_node.addLabel(synsetLabel);
				
				Relationship relationship = s_node.createRelationshipTo(hypo_node, RelTypes.Hyponym);
				relationship.setProperty("type", "semantic");
				relationship.setProperty("name", "hyponym");
				relationship.setProperty("w", 0.75);
				
				addTermsFromSynsetHyponymyHierarchy(hyponym, hypo_node, target);
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
	
	int lcsDepth(String src_label, String dst_label)
	{
		int max = 0;
		List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(src_label, "n");
		 List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(dst_label, "n");
		 
		 for (Concept src:synsets1)
		 {
			 for (Concept dst:synsets2)
			 {
				 
				 List<Depth> lcsList = depthFinder.getRelatedness( src, dst, null );
				 int depth = lcsList.get(0).depth; // sorted by depth (asc)
				 
				 if (depth > max)
					 max = depth;
			 }
			
		 }
		 
		 return max;
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
