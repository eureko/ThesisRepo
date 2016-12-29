package it.unina.thesisrepo.ontoadapters;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class BBCFoodOntoAdapter 
{
	
	final static String base_uri = "http://www.bbc.co.uk/ontologies/creativework#";
	static OntModel write_model;
	
	public static void main(String[] args) 
	{
		try
		{
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
			write_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
			model.read("./ontologies/src/BBCfoodOntology.ttl");
			write_model.setNsPrefix("bbc", base_uri);
			
			ExtendedIterator<OntClass> iter = model.listNamedClasses();
			
			while(iter.hasNext())
			{
				OntClass c = iter.next();
				
				addClass(c);
				//if (c.getLabel(null)!= null)
				/*System.out.println(c);
				if (c.hasSubClass())
				{
					ExtendedIterator<OntClass> subClasses = c.listSubClasses();
					while(subClasses.hasNext())
						System.out.println("\t" + subClasses.next());
				}*/
			}
			
			FileWriter fileWriter = new FileWriter("./ontologies/4.owl");
			
			write_model.write(fileWriter, "RDF/XML-ABBREV", base_uri);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	static void addClass(OntClass c)
	{
		OntClass cls = write_model.createClass(base_uri + c.getLocalName());
		if (c.getLabel(null) != null)
			cls.setLabel(c.getLabel(null), "en");
		
		ExtendedIterator<OntClass> subClasses = c.listSubClasses();
		while(subClasses.hasNext())
		{
			OntClass subClass = subClasses.next();
			OntClass cls_sub = write_model.createClass(base_uri +  subClass.getLocalName());
			
			if (subClass.getLabel(null) != null)
				cls_sub.setLabel(subClass.getLabel(null), "en");
			
			cls.addSubClass(cls_sub);
			addClass(subClass);
		}
	}
}
