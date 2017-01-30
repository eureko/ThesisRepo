package it.unina.thesisrepo.ontoadapters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;

public class AGROVCAdapter 
{
	static final String uri = "http://aims.fao.org/aos/agrovoc#";
	static OntModel model;
	
	static final String root_resource_uri = "http://aims.fao.org/aos/agrovoc/c_6211";
	//static final String root_resource_uri = "http://aims.fao.org/aos/agrovoc/c_8678";
	static final String resultOntoFilePath = "./ontologies/2.owl";
	static final String sourceFilePath = "./ontologies/src/agrovoc_core.rdf";
	
	public static void main(String[] args) 
	{
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		model.setNsPrefix("agrovc", uri);
		
		Ontology ont = model.createOntology("http://aims.fao.org/aos/agrovoc");
		ont.addComment("Automatically created through Jena APis", "en");

		try
		{
			// <products> (c_6211v) is the Root concept
			String rootClassNameLabel = getPreferredLabel(getPreferredLabelUri(root_resource_uri,  "en"));
			
			String[] tokens = rootClassNameLabel.split("\\s");
			String className = ""; 
			for (String str:tokens)
				className += capitalizeString(str);
			
			OntClass rootClass = model.createClass(uri + className);
			rootClass.addLabel(rootClassNameLabel,"en");
			rootClass.addVersionInfo(root_resource_uri.substring(root_resource_uri.lastIndexOf('/')+1));
			
			getNarrower(root_resource_uri, rootClass);
			
			FileWriter ncicpOntoFile = new FileWriter(resultOntoFilePath);
			model.write(ncicpOntoFile, "RDF/XML-ABBREV");
				
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	static String getPreferredLabelUri(String resource, String lang) throws IOException
	{
		BufferedReader file_buffer = new BufferedReader(new FileReader(sourceFilePath));
		String line = "", lineint = "";
		
		while((line = file_buffer.readLine()) != null)
		{
			if (line.contains("<rdf:Description rdf:about=\""+resource+"\">"))
			{
				while((lineint = file_buffer.readLine()) != null)
				{
					if (lineint.contains("<prefLabel") && lineint.contains("xl_"+lang))
					{
						String[] tokens = lineint.split("\\s");
						String tmp = tokens[3];
						
						String label_uri = tmp.substring(tmp.indexOf('"')+1, tmp.lastIndexOf('"'));
						file_buffer.close();
						return label_uri;
					}
					else if (lineint.trim().compareTo("</rdf:Description>") == 0)
					{
						break;
					}
				}
			}
		}
		file_buffer.close();
		
		return "";
	}
	static String getPreferredLabel(String label_uri) throws IOException
	{
		BufferedReader file_buffer = new BufferedReader(new FileReader(sourceFilePath));
		String line = "", lineint = "";
		
		while((line = file_buffer.readLine()) != null)
		{
			if (line.contains("<rdf:Description rdf:about=\""+label_uri+"\">"))
			{
				while((lineint = file_buffer.readLine()) != null)
				{
					if (lineint.contains("<literalForm"))
					{
						String label = lineint.substring(lineint.indexOf('>')+1, lineint.lastIndexOf('<'));
						file_buffer.close();
						return label;
					}
					else if (lineint.trim().compareTo("</rdf:Description>") == 0)
					{
						break;
					}
				}
			}
		}
		
		file_buffer.close();
		return "";
	}
	
	static void exploreResource(String resource) throws IOException
	{
		BufferedReader file_buffer = new BufferedReader(new FileReader(sourceFilePath));
		String line = "", lineint = "";
		StringBuffer classDescBuffer = new StringBuffer();
		
		while((line = file_buffer.readLine()) != null)
		{
			if (line.contains("<rdf:Description rdf:about=\""+resource+"\">"))
			{
				classDescBuffer.append(line + "\n");
				while((lineint = file_buffer.readLine()) != null)
				{
					if (lineint.trim().compareTo("</rdf:Description>") == 0)
					{
						classDescBuffer.append(lineint + "\n");
						break;
					}
					else
						classDescBuffer.append(lineint + "\n");
				}
				System.out.println(classDescBuffer.toString());
			}
		}
		file_buffer.close();
	}
	
	static Vector<String> getNarrower(String resource, OntClass c) throws IOException
	{
		System.out.println(c);
		BufferedReader file_buffer = new BufferedReader(new FileReader(sourceFilePath));
		String line = "", lineint = "";
		Vector<String> specialized = new Vector<String>();
		
		while((line = file_buffer.readLine()) != null)
		{
			if (line.contains("<rdf:Description"))
			{
				while((lineint = file_buffer.readLine()) != null)
				{
					if (lineint.contains("<broader"))
					{
						String[] tokens = lineint.split("\\s");
						String tmp = tokens[3];
						String lineint_resource = tmp.substring(tmp.indexOf('"')+1, tmp.lastIndexOf('"'));
						
						if (lineint_resource.compareTo(resource)==0)
						{
							specialized.add(line.substring(line.indexOf('"') +1, line.lastIndexOf('"')));
							lineint = "";
							break;
						}
					}
					else if (lineint.trim().compareTo("</rdf:Description>") == 0)
					{
						break;
					}
				}
			}
		}
		
		for (String s:specialized)
		{
			
			String label = getPreferredLabel(getPreferredLabelUri(s,  "en"));
			String[] tokens = label.split("\\s");
			String subclassName = ""; 
			for (String str:tokens)
				subclassName += capitalizeString(str);
			OntClass newC = model.createClass(uri + subclassName);
			newC.addVersionInfo(s.substring(s.lastIndexOf('/')+1));
			newC.addLabel(label,"en");
			c.addSubClass(newC);
			getNarrower(s, newC);
			
		}
		file_buffer.close();
		return specialized;
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
