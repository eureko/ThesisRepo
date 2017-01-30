package it.unina.thesisrepo.framework;

import it.unina.thesisrepo.matchers.Alignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class Integrator 
{
	static HashSet<Alignment> linguistic_alignments = new HashSet<Alignment>();
	static HashSet<Alignment> sem_gounded_alignments = new HashSet<Alignment>();
	static HashSet<Alignment> total_alignments = new HashSet<Alignment>();
	
	static final String target_uri = "http://target-ontology#";
	static final String ncicb_uri = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
	static final String int_uri = "http://integrated-ontologies/";
	
	static final String[] uris = new String[]{"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#",
		"http://aims.fao.org/aos/agrovoc#",
		"http://www.bbc.co.uk/ontologies/creativework#",
		"http://data.lirmm.fr/ontologies/food#",
		"http://www.productontology.org/id#",
		"http://www.danfood.info/eurocode#",
		"http://www.wandinc.com/#",
		"http://www.iso.org/iso/catalogue_ics/foodtechnology/catalogue_ics#",
		"http://purl.obolibrary.org/obo/foodon-edit.owl#"};
	
	static TreeMap<String, OntClass> classTreeMap = new TreeMap<String, OntClass>(); 
	
	static 
	{
		System.setProperty("wordnet.database.dir", "C:\\Program Files (x86)\\WordNet\\2.1\\dict");
	}
	static WordNetDatabase wsdatabase = WordNetDatabase.getFileInstance(); 
	
	public static void main(String[] args) 
	{
		try
		{
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
			model.setNsPrefix("integrated", int_uri);
			model.setNsPrefix("target", target_uri);
			model.setNsPrefix("ncicb", uris[0]);
			model.setNsPrefix("agrovc", uris[1]);
			model.setNsPrefix("bbc", uris[2]);
			model.setNsPrefix("lirmm", uris[3]);
			model.setNsPrefix("prod", uris[4]);
			model.setNsPrefix("eurocode2", uris[5]);
			model.setNsPrefix("wand", uris[6]);
			model.setNsPrefix("iso", uris[7]);
			model.setNsPrefix("foodon", uris[8]);
			
			Ontology ont = model.createOntology("http://integrated-ontologies/");
			ont.addComment("Automatically created through Jena APis", "en");
			
			
			int indexes[] = {1,2,6};
			
			for (int i = 0; i<indexes.length; i++)
			{
				System.out.println("******************************* Integrating " + "./alignments/"+indexes[i]+".alignment");
				BufferedReader file_buffer = new BufferedReader(new FileReader("./alignments/"+indexes[i]+".alignment"));
			    
				String line;
				line = file_buffer.readLine(); // Read comment line
				
				while((line = file_buffer.readLine()) != null)
				{
						try
						{
							String[] tokens = line.split(",");
							String src = tokens[0];
							String dst = tokens[1];
							double[] measures = new double[10];
							
							measures[0] = Double.parseDouble(tokens[2]);
							measures[1] = Double.parseDouble(tokens[3]);
							measures[2] = Double.parseDouble(tokens[4]);
							measures[3] = Double.parseDouble(tokens[5]);
							measures[4] = Double.parseDouble(tokens[6]);
							measures[5] = Double.parseDouble(tokens[7]);
							measures[6] = Double.parseDouble(tokens[8]); // Wup
							measures[7] = Double.parseDouble(tokens[9]);
							measures[8] = Double.parseDouble(tokens[10]); // ExtWup
							String result = tokens[11];
							
							Alignment a = new Alignment(src, dst, measures, result);
							linguistic_alignments.add(a);
							total_alignments.add(a);
							
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
				}
				file_buffer.close();
				
				for (Alignment a:linguistic_alignments)
				{
					if (a.result.compareTo("eqv")==0)
					{
						System.out.println(a.src + " " + a.result  + " " + a.dst);
						String src_uri = uris[indexes[i]-1] + capitalizeString(a.src).replace(" ", "");
						String dst_uri = target_uri + capitalizeString(a.dst).replace("( )+", "");
						
						OntClass srcClass;
						OntClass dstClass;
						
						if (classTreeMap.get(src_uri) != null)
							srcClass = classTreeMap.get(src_uri);
						else
						{
							srcClass = model.createClass(src_uri);
							classTreeMap.put(src_uri, srcClass);
						}
						
						if (classTreeMap.get(dst_uri) != null)
							dstClass = classTreeMap.get(dst_uri);
						else
						{
							dstClass = model.createClass(dst_uri);
							classTreeMap.put(dst_uri, dstClass);
						}
						
						srcClass.addEquivalentClass(dstClass);
						
					}
					//else if (a.result.compareTo("hypo")==0 && ((a.measures[6] > 0.9 && a.measures[6] < 1) || (a.measures[8] > 0.9 && a.measures[8] < 1.0)))
					else if (a.result.compareTo("hypo")==0)
					{
						if (a.src.contains(a.dst) && a.src.endsWith(a.dst))
						{
							System.out.println(a.src + " " + a.result  + " " + a.dst);
							String src_uri = uris[indexes[i]-1] + capitalizeString(a.src).replace(" ", "");
							String dst_uri = target_uri + capitalizeString(a.dst).replaceAll("\\s+","").replace("-", "");
							
							OntClass srcClass;
							OntClass dstClass;
							
							if (classTreeMap.get(src_uri) != null)
								srcClass = classTreeMap.get(src_uri);
							else
							{
								srcClass = model.createClass(src_uri);
								classTreeMap.put(src_uri, srcClass);
							}
							
							if (classTreeMap.get(dst_uri) != null)
								dstClass = classTreeMap.get(dst_uri);
							else
							{
								dstClass = model.createClass(dst_uri);
								classTreeMap.put(dst_uri, dstClass);
							}
							
							dstClass.addSubClass(srcClass);
						}
						else
						{
							if (isHyponymOf(a.src, a.dst))
							{
								System.out.println(a.src + " " + a.result  + " " + a.dst);
								String src_uri = uris[indexes[i]-1] + capitalizeString(a.src).replace(" ", "");
								String dst_uri = target_uri + capitalizeString(a.dst).replaceAll("\\s+","").replace("-", "");
								
								OntClass srcClass;
								OntClass dstClass;
								
								if (classTreeMap.get(src_uri) != null)
									srcClass = classTreeMap.get(src_uri);
								else
								{
									srcClass = model.createClass(src_uri);
									classTreeMap.put(src_uri, srcClass);
								}
								
								if (classTreeMap.get(dst_uri) != null)
									dstClass = classTreeMap.get(dst_uri);
								else
								{
									dstClass = model.createClass(dst_uri);
									classTreeMap.put(dst_uri, dstClass);
								}
								
								dstClass.addSubClass(srcClass);
							}
						}
					}
				}
			}
			
			FileWriter ncicpOntoFile = new FileWriter("./ontologies/integration_ontology1.owl");
			model.write(ncicpOntoFile, "RDF/XML-ABBREV");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	public static String capitalizeString(String string) {
		  char[] chars = string.toLowerCase().toCharArray();
		  boolean found = false;
		  for (int i = 0; i < chars.length; i++) {
		    if (!found && Character.isLetter(chars[i])) {
		      chars[i] = Character.toUpperCase(chars[i]);
		      found = true;
		    } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
		      found = false;
		    }
		  }
		  return String.valueOf(chars);
		}
	
	static boolean isHyponymOf(String src, String dst)
	{
		Vector<String> src_words = new Vector<String>();
		Synset[] synsets = wsdatabase.getSynsets(src, SynsetType.NOUN);
		
		for (Synset s:synsets)
		{
			Synset[] hypernyms = ((NounSynset)s).getHypernyms();
			for (Synset hyp:hypernyms)
			{
				String[] wordsForms = hyp.getWordForms();
				
				for (String w:wordsForms)
					src_words.add(w);
			}
		}
		
		//for (String s:src_words)
			//System.out.println(s);
		
		if (src_words.contains(dst))
			return true;
		else
			return false;
		
	  }
}
