package it.unina.thesisrepo.utilities;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class JenaExamples 
{
	 static String nsA = "http://somewhere/else#";
	 static String nsB = "http://nowhere/else#";
	 
	public static void main(String[] args) {
		
		
		OntModel model  = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		model.setNsPrefix("some", nsA);
		model.setNsPrefix("now", nsB);
		 
		 model.createClass(nsA + "root");
		 model.createClass(nsB + "A");

		 try
		 {
			FileWriter writer = new FileWriter("./ontologies/example.owl");
			model.write(writer, "RDF/XML-ABBREV");
		 }
		 catch(IOException ioe)
		 {
			 ioe.printStackTrace();
		 }
	}
}
