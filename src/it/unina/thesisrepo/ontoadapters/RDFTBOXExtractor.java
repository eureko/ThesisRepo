package it.unina.thesisrepo.ontoadapters;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class RDFTBOXExtractor 
{
	OntModel ontoModel;
	
	public static void main(String[] args) 
	{
		RDFTBOXExtractor rdftboxExtractor = new RDFTBOXExtractor("./ontologies/src/foodon.owl");
		rdftboxExtractor.extractNamedClasses();
		rdftboxExtractor.extractDatatypeProperties();
		rdftboxExtractor.extractObjectProperties();
	}
	
	public RDFTBOXExtractor(String modelFile) 
	{
		ontoModel = ModelFactory.createOntologyModel();
		ontoModel.read(modelFile, "RDF/XML");
	}
	
	void extractNamedClasses()
	{
		ExtendedIterator<OntClass> classes = ontoModel.listNamedClasses();
		int counter = 0;
		
		while(classes.hasNext())
		{
			OntClass c = classes.next();
			System.out.println(c.getLocalName() + "|" + (c.getLabel(null)==null?"":c.getLabel(null)) + " " + (c.getComment(null)==null?"":c.getComment(null)));
			counter++;
		}
		
		//System.out.println("*************** Retrieved " + counter + " named classes *****************");
	}
	
	void extractDatatypeProperties()
	{
		ExtendedIterator<DatatypeProperty> dataProperties =  ontoModel.listDatatypeProperties();
		int counter = 0;
		while(dataProperties.hasNext())
		{
			DatatypeProperty p = dataProperties.next();
			System.out.println(p.getLocalName() + "|" + (p.getLabel(null)==null?"":p.getLabel(null)) + " " + (p.getComment(null)==null?"":p.getComment(null)));
			counter++;
		}
		//System.out.println("*************** Retrieved " + counter + " datatye properties *****************");
	}
	
	void extractObjectProperties()
	{
		ExtendedIterator<ObjectProperty> objProperties =  ontoModel.listObjectProperties();
		int counter = 0;
		while(objProperties.hasNext())
		{
			ObjectProperty op = objProperties.next();
			System.out.println(op.getLocalName() + "|" + (op.getLabel(null)==null?"":op.getLabel(null)) + " " + (op.getComment(null)==null?"":op.getComment(null)));
			counter++;
		}
		//System.out.println("*************** Retrieved " + counter + " object properties *****************");
	}
	
}
