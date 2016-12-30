package it.unina.thesisrepo.ontoadapters;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class ProductTypeOntoAdapter 
{
	final static String uri = "http://www.productontology.org/id/";
	static OntModel write_model;
	static final String resultOntoFilePath = "./ontologies/5.owl";
	static final String sourceFilePath = "./ontologies/src/productOntology.rdf";
	
	static final String regex2 = "(?<!^)(?=[A-Z])";
	
	public static void main(String[] args) 
	{
		
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		model.read(sourceFilePath);
			
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
}
