package it.unina.thesisrepo.ontoadapters;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class LIRMMAdapter 
{
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
	}
}
