package it.unina.thesisrepo.utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.util.iterator.ExtendedIterator;

public class CopyOfReadTaxonomy 
{
	//static final String prefix = "eurocode2"; 
	
	static final String[] uris = new String[]{"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#",
		"http://aims.fao.org/aos/agrovoc#",
		"http://www.bbc.co.uk/ontologies/creativework#",
		"http://data.lirmm.fr/ontologies/food#",
		"http://www.productontology.org/id#",
		"http://www.danfood.info/eurocode#",
		"http://www.wandinc.com/#",
		"http://www.iso.org/iso/catalogue_ics/foodtechnology/catalogue_ics#",
		"http://purl.obolibrary.org/obo/foodon-edit.owl#"};
	
	static final String target_uri = "http://target-ontology#";
	static final String int_uri = "http://integrated-ontologies/";
	
	
	static TreeMap<String, String> prefixMaps = new TreeMap<String, String>();
	
	public static void main(String[] args) 
	{
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		//OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.read("./ontologies/integration_ontology1.owl");
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
		
		
		prefixMaps.put(int_uri, "integrated");
		prefixMaps.put(target_uri, "target");
		prefixMaps.put(uris[0], "ncicb");
		prefixMaps.put(uris[1], "agrovc");
		prefixMaps.put(uris[2], "bbc");
		prefixMaps.put(uris[3], "lirmm");
		prefixMaps.put(uris[4], "prod");
		prefixMaps.put(uris[5], "eurocode2");
		prefixMaps.put(uris[6], "wand");
		prefixMaps.put(uris[7], "iso");
		prefixMaps.put(uris[8], "foodon");
		
		
		//InfModel infmodel = ModelFactory.createRDFSModel(model);
		
		try
		{
			FileWriter fileWriter = new FileWriter("./ontologies/IntegratedOntologyStmtsInf.txt");
			
			
			StmtIterator iter = model.listStatements();
			
			int c = 0, d = 0;
			while (iter.hasNext())
			{
				Statement s = iter.next();
				//System.out.println(s);
				if (s.getPredicate().toString().compareTo("http://www.w3.org/2002/07/owl#equivalentClass")==0)
				{
					System.out.println(prefixMaps.get(s.getSubject().getNameSpace()) + ":" + s.getSubject().getLocalName()+
							" = " + prefixMaps.get(s.getObject().asNode().getNameSpace()) + ":" + s.getObject().asNode().getLocalName() );
					fileWriter.write(prefixMaps.get(s.getSubject().getNameSpace()) + ":" + s.getSubject().getLocalName()+
							" = " + prefixMaps.get(s.getObject().asNode().getNameSpace()) + ":" + s.getObject().asNode().getLocalName() + "\n");
					c++;
				}
				else if (s.getPredicate().toString().compareTo("http://www.w3.org/2000/01/rdf-schema#subClassOf")==0)
				{
					System.out.println(prefixMaps.get(s.getSubject().getNameSpace()) + ":" + s.getSubject().getLocalName()+
							" < " + prefixMaps.get(s.getObject().asNode().getNameSpace()) + ":" + s.getObject().asNode().getLocalName() );
					
					fileWriter.write(prefixMaps.get(s.getSubject().getNameSpace()) + ":" + s.getSubject().getLocalName()+
							" < " + prefixMaps.get(s.getObject().asNode().getNameSpace()) + ":" + s.getObject().asNode().getLocalName() + "\n");
					
					d++;
				}
			
			}
			System.out.println("\nNumber of equivalent classes: " + c + " Number of subclasses: " + d);
			fileWriter.close();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		
		/*int i = 0;
		while (iter.hasNext())
		{
			OntClass c = iter.next();
			ExtendedIterator<OntClass> equivClasses = c.listEquivalentClasses();
			System.out.print(prefixMaps.get(c.getNameSpace()) + ":" + c.getLocalName());
			while(equivClasses.hasNext())
			{	
				OntClass eqvCls = equivClasses.next();
				System.out.print(" = " + prefixMaps.get(eqvCls.getNameSpace()) + ":" + eqvCls.getLocalName());
			}
			System.out.println();
			i++;
		}
		System.out.println(i);*/
		
	}
	
}
