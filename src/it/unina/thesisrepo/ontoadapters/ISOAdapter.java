package it.unina.thesisrepo.ontoadapters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class ISOAdapter 
{
	static final String uri = "http://www.iso.org/iso/catalogue_ics/foodtechnology/catalogue_ics#";
	
	static final String resultOntoFilePath = "./ontologies/8.owl";
	static final String sourceFilePath = "./ontologies/src/ISOStandard.txt";
	
	static final String regex2 = "(?<!^)(?=[A-Z])";
	
	public static void main(String[] args) 
	{
		
		try
		{
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
			model.setNsPrefix("iso", uri);
			
			Ontology ont = model.createOntology("http://www.iso.org/iso/catalogue_ics/foodtechnology/catalogue_ics");
			ont.addComment("Automatically created through Jena APis", "en");
			
			BufferedReader file_buffer = new BufferedReader(new FileReader(sourceFilePath));
			String line;
			line = file_buffer.readLine(); // Read comment line
			OntClass c = null;
			while((line = file_buffer.readLine()) != null)
			{
				if (!line.startsWith("\t"))
				{
					c = model.createClass(uri + capitalizeString(line.trim()).replace(" ", ""));
					c.addLabel(line.trim(), "en");
				}
				else
				{
					OntClass subClass = model.createClass(uri + capitalizeString(line.trim()).replace(" ", ""));
					subClass.addLabel(line.trim(), "en");
					
					c.addSubClass(subClass);
				}
			}
			
			file_buffer.close();
			
			FileWriter file_writer = new FileWriter(resultOntoFilePath);
			model.write(file_writer, "RDF/XML-ABBREV");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	public static String capitalizeString(String string) {
		  char[] chars = string.toLowerCase().toCharArray();
		  boolean found = false;
		  for (int i = 0; i < chars.length; i++) {
		    if (!found && Character.isLetter(chars[i])) {
		      chars[i] = Character.toUpperCase(chars[i]);
		      found = true;
		    } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
		      found = false;
		    }
		  }
		  return String.valueOf(chars);
		}
}
