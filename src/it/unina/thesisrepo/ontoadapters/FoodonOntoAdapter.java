package it.unina.thesisrepo.ontoadapters;

import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class FoodonOntoAdapter 
{
	final static String uri = "http://purl.obolibrary.org/obo/foodon-edit.owl#";
	static OntModel write_model;
	static final String resultOntoFilePath = "./ontologies/9.owl";
	static final String sourceFilePath = "./ontologies/src/foodon.owl";
	
	static final String regex2 = "(?<!^)(?=[A-Z])";
	
	static TreeSet<String> classesSet = new TreeSet<String>();
	
	public static void main(String[] args) 
	{
		
		try
		{
			OntModel model = ModelFactory.createOntologyModel();
			model.read(sourceFilePath);
				
			write_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
			write_model.setNsPrefix("foodon", uri);
			
			Ontology ont = write_model.createOntology("http://purl.obolibrary.org/obo/foodon-edit.owl");
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
		classesSet.add(c.getURI());
		System.out.println(c);
		OntClass cls = write_model.createClass(uri + c.getLocalName());
		cls.setLabel(getLabelFromClass(c), "en");
		
		if (c.hasSubClass())
		{
			ExtendedIterator<OntClass> subClasses = c.listSubClasses(true);
			while(subClasses.hasNext())
			{
				OntClass subClass = subClasses.next();
				OntClass cls_sub = write_model.createClass(uri +  subClass.getLocalName());
				
				cls_sub.setLabel(getLabelFromClass(subClass), "en");
				
				cls.addSubClass(cls_sub);
				if (!classesSet.contains(subClass.getURI()))
					addClass(subClass);
			}
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
