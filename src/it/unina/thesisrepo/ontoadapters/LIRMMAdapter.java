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
	
	static final String resultOntoFilePath = "./ontologies/4.owl";
	static final String sourceFilePath = "./ontologies/src/lirmm_05.rdf";
	
	static final String regex2 = "(?<!^)(?=[A-Z])";
	
	public static void main(String[] args) 
	{
		
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		model.read(sourceFilePath);
		model.setNsPrefix("lirmm", base_uri);
		
		ExtendedIterator<OntClass> iter = model.listNamedClasses();
		
		while(iter.hasNext())
		{
			OntClass c = iter.next();
			addLabel(c);
			System.out.println(c);
			if (c.hasSubClass())
			{
				ExtendedIterator<OntClass> subClasses = c.listSubClasses();
				while(subClasses.hasNext())
					System.out.println("\t" + subClasses.next());
			}
		}
		
		//OntModel write_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		
		try
		{
			FileWriter fileWriter = new FileWriter(resultOntoFilePath);
			model.write(fileWriter, "RDF/XML-ABBREV");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	static void addLabel(OntClass c)
	{
		c.setLabel(getLabelFromClass(c), "en");
		
		if (c.hasSubClass())
		{
			ExtendedIterator<OntClass> subClasses = c.listSubClasses();
			while(subClasses.hasNext())
				addLabel(subClasses.next());
		}
	}
	
	static String getLabelFromClass(OntClass c)
	{
		String result = "";
		
		String local = c.getLocalName();
		
		if (local.split(regex2).length > 1)
		{
			String[] tokens = local.split(regex2);
			for (String s:tokens)
				result += s + " ";
		}
		else
			result = local;
		
		return result.trim();
	}
}
