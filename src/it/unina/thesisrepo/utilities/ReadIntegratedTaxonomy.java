package it.unina.thesisrepo.utilities;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

public class ReadIntegratedTaxonomy 
{
	static final String prefix = "product"; 
	public static void main(String[] args) 
	{
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		model.read("./ontologies/integration_ontology_inferred.owl");
		
		ExtendedIterator<OntClass> iter = model.listNamedClasses();
		
		while(iter.hasNext())
		{
			OntClass c = iter.next();
			System.out.println(c.getLocalName() + " " + c.getNameSpace());
		}
		
	}
}
