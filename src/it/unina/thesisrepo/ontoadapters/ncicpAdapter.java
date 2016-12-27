package it.unina.thesisrepo.ontoadapters;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

public class ncicpAdapter 
{
	public static void main(String[] args) 
	{
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		model.read("./ontologies/src/ncicp.owl", "RDF/XML");
		System.out.println(model);
		
	}
}
