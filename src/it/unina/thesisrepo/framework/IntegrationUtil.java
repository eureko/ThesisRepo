package it.unina.thesisrepo.framework;

import it.unina.thesisrepo.matchers.TermsSetMeasuresCalculator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class IntegrationUtil 
{
	static final String ontoCorpusPath = "./ontologies/urls.txt/";
	static final String int_uri = "http://integrated-ontologies/";
	
	public static void main(String[] args) 
	{
		
		try
		{
			BufferedReader file_buffer = new BufferedReader(new FileReader(ontoCorpusPath));
			String line;
			
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
			model.setNsPrefix("integrated", int_uri);
			
			Ontology ont = model.createOntology("http://integrated-ontologies/");
			ont.addComment("Automatically created through Jena APis", "en");
			
			
		
			while((line = file_buffer.readLine()) != null)
			{
				
				String[] tokens = line.split("\\|");
				String index = tokens[0];
				String ontoName = tokens[1];
				
				OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
				m.read("./ontologies/"+index+".owl");
				
				model.add(m);
				ExtendedIterator<Ontology> iter =   m.listOntologies();
				
				while(iter.hasNext())
				{
					Ontology ontology = iter.next();
					ExtendedIterator<OntResource> imports = ontology.listImports();
					System.out.println(ontology);
					while(imports.hasNext())
						System.out.println("\t" + imports.next());
				}
			}
			System.out.println("****************************");
			ExtendedIterator<Ontology> iter = model.listOntologies();
			while(iter.hasNext())
			{
				System.out.println(iter.next());
			}
			
			file_buffer.close();
			
			FileWriter writer = new FileWriter("./ontologies/allModels.owl");
			model.write(writer, "RDF/XML-ABBREV");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		
	}
}
