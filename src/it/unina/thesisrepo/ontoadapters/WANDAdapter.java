package it.unina.thesisrepo.ontoadapters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class WANDAdapter 
{
	static final String base_uri = "http://www.wandinc.com/#";
	
	public static void main(String[] args) 
	{
		
		try
		{
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
			
			BufferedReader file_buffer = new BufferedReader(new FileReader("./ontologies/src/WANDExcerpt.txt"));
			String line;
			//line = file_buffer.readLine(); // Read comment line
			OntClass c = null;
			while((line = file_buffer.readLine()) != null)
			{
				if (!line.startsWith("\t"))
				{
					c = model.createClass(capitalizeString(line.trim()).replace(" ", ""));
					c.addLabel(line.trim(), "en");
				}
				else
				{
					OntClass subClass = model.createClass(capitalizeString(line.trim()).replace(" ", ""));
					subClass.addLabel(line.trim(), "en");
					
					c.addSubClass(subClass);
				}
			}
			
			file_buffer.close();
			
			FileWriter file_writer = new FileWriter("./ontologies/9.owl");
			model.write(file_writer, "RDF/XML-ABBREV", base_uri);
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
