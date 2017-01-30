package it.unina.thesisrepo.utilities;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

public class ReadTaxonomy 
{
	static final String prefix = "product"; 
	public static void main(String[] args) 
	{
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		model.read("./ontologies/integration_ontology1.owl");
		
		ExtendedIterator<OntClass> iter = model.listNamedClasses();
		
		/*OntClass c = null;
		
		while (iter.hasNext())
		{
			c = iter.next();
			if (c.getLocalName().compareToIgnoreCase("Food")==0)
				break;
		}*/
		
		int i = 0,j = 0, k = 0, l = 0;
		
		
		while(iter.hasNext())
		{
			OntClass c = iter.next();
			System.out.println(prefix + ":" + c.getLocalName());
			if (c.hasSubClass())
			{
				ExtendedIterator<OntClass> iter2 = c.listSubClasses(true);
				while(iter2.hasNext())
				{
					OntClass c2 = iter2.next();
					System.out.println("->" + prefix + ":" + c2.getLocalName());
					if (c2.hasSubClass())
					{
						ExtendedIterator<OntClass> iter3 = c2.listSubClasses(true);
						while(iter3.hasNext() && j < 8)
						{
							OntClass c3 = iter3.next();
							System.out.println("-->" + prefix + ":" + c3.getLocalName());
							if (c3.hasSubClass())
							{
								ExtendedIterator<OntClass> iter4 = c3.listSubClasses(true);
								while(iter4.hasNext() && k < 4)
								{
									OntClass c4 = iter4.next();
									System.out.println("--->" + prefix + ":" + c4.getLocalName());
									k++;
								}
								k = 0;
							}
							
							j++;
						}
						j = 0;
					}
					
					i++;
				}
				i = 0;
			}
		}
		
		//System.out.println(i);
		
	}
	
	static void outputClass(OntClass c)
	{
		System.out.println("ncicb:"  + c.getLocalName());
		if (c.hasSubClass())
		{
			System.out.print("\t");
			ExtendedIterator<OntClass> subClasses = c.listSubClasses(true);
			while (subClasses.hasNext())
				outputClass(subClasses.next());
		}
	}
}
