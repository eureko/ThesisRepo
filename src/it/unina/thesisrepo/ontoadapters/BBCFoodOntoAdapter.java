package it.unina.thesisrepo.ontoadapters;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class BBCFoodOntoAdapter 
{
	final static String uri = "http://www.bbc.co.uk/ontologies/creativework#";
	static OntModel write_model;
	static final String resultOntoFilePath = "./ontologies/3.owl";
	static final String sourceFilePath = "./ontologies/src/BBCfoodOntology.ttl";
	
	static final String regex2 = "(?<!^)(?=[A-Z])";
	
	public static void main(String[] args) 
	{
		try
		{
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
			model.read(sourceFilePath);
			
			write_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
			write_model.setNsPrefix("bbc", uri);
			
			Ontology ont = write_model.createOntology("http://www.bbc.co.uk/ontologies/creativework");
			ont.addComment("Automatically created through Jena APis", "en");
			
			
			ExtendedIterator<OntClass> iter = model.listNamedClasses();
			
			while(iter.hasNext())
			{
				OntClass c = iter.next();
				addClass(c);
			}
			
			FileWriter fileWriter = new FileWriter(resultOntoFilePath);
			write_model.write(fileWriter, "RDF/XML-ABBREV");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	static void addClass(OntClass c)
	{
		OntClass cls = write_model.createClass(uri + c.getLocalName());
		cls.setLabel(getLabelFromClass(c), "en");
		
		ExtendedIterator<OntClass> subClasses = c.listSubClasses();
		while(subClasses.hasNext())
		{
			OntClass subClass = subClasses.next();
			OntClass cls_sub = write_model.createClass(uri +  subClass.getLocalName());
			
			cls_sub.setLabel(getLabelFromClass(subClass), "en");
			
			cls.addSubClass(cls_sub);
			addClass(subClass);
		}
	}
	
	static String getLabelFromClass(OntClass c)
	{
		String result = "";
		String label = c.getLabel(null);
		
		if (label != null)
		{
			if (label.split(regex2).length > 1)
			{
				String[] tokens = label.split(regex2);
				for (String s:tokens)
					result += s.trim() + " ";
			}
			else
				result = label;
		}
		else
		{
			String local = c.getLocalName();
			
			if (local.split(regex2).length > 1)
			{
				String[] tokens = local.split(regex2);
				for (String s:tokens)
					result += s + " ";
			}
		}
		
		return result.trim();
	}
}
