package it.unina.thesisrepo.ontoadapters;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class LIRMMAdapter 
{
	static final String base_uri = "http://data.lirmm.fr/ontologies/food#";
	
	public static void main(String[] args) 
	{
		
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		model.read("./ontologies/src/lirmm_05.rdf");
		
		ExtendedIterator<OntClass> iter = model.listNamedClasses();
		
		while(iter.hasNext())
		{
			OntClass c = iter.next();
			System.out.println(c);
			if (c.hasSubClass())
			{
				ExtendedIterator<OntClass> subClasses = c.listSubClasses();
				while(subClasses.hasNext())
					System.out.println("\t" + subClasses.next());
			}
		}
		
		OntModel write_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		
		try
		{
			FileWriter fileWriter = new FileWriter("./ontologies/5.owl");
			
			model.write(fileWriter, "RDF/XML-ABBREV", base_uri);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		
	}
}
