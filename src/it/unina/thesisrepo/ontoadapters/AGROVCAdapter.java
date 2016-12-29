package it.unina.thesisrepo.ontoadapters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

public class AGROVCAdapter 
{
	static final String uri = "http://aims.fao.org/aos/agrovoc/#";
	static FileInputStream fileInputStreamReader;
	BufferedReader file_buffer;
	static OntModel model;
	
	//http://aims.fao.org/aos/agrovoc/c_6211
	//http://aims.fao.org/aos/agrovoc/xl_en_1299486360046
	
	static final String root_resource_uri = "http://aims.fao.org/aos/agrovoc/c_6211";
	//static final String root_resource_uri = "http://aims.fao.org/aos/agrovoc/c_8678";
	
	static final String filePath = "./ontologies/src/agrovoc_core.rdf";
	public static void main(String[] args) 
	{
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		model.setNsPrefix("ncicp", uri);
		//model.read("./ontologies/src/ncicp.owl", "RDF/XML");
		//System.out.println(model);
		
		try
		{
			
			OntClass rootClass = model.createClass(root_resource_uri);
			rootClass.addLabel(getPreferredLabel(getPreferredLabelUri(root_resource_uri,  "en")),"en");
			
			getSpecialised(root_resource_uri, rootClass);
			
			/*for (String s:specialized)
			{
				System.out.println(s);
				String label_uri = getPreferredLabelUri(s, "en");
				System.out.println(label_uri);
				System.out.println(getPreferredLabel(label_uri));
				
			}*/
			
			FileWriter ncicpOntoFile = new FileWriter("./ontologies/2.owl");
			
			model.write(ncicpOntoFile, "RDF/XML-ABBREV", uri);
				
			
			//exploreResource("http://aims.fao.org/aos/agrovoc/c_6211");
			//http://aims.fao.org/aos/agrovoc/xl_en_1299486360046
			//exploreResource("http://aims.fao.org/aos/agrovoc/c_6211");
			//getPreferredLabelUri("http://aims.fao.org/aos/agrovoc/c_6211", "en");
			
			//System.out.println(getPreferredLabel("http://aims.fao.org/aos/agrovoc/xl_en_1299485662168"));
			

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
			
	}
	
	static String getPreferredLabelUri(String resource, String lang) throws IOException
	{
		
		BufferedReader file_buffer = new BufferedReader(new FileReader(filePath));
		String line = "", lineint = "";
		boolean found = false;
		
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
		//<rdf:Description rdf:about="http://aims.fao.org/aos/agrovoc/xl_en_1299486360046">
		//<literalForm xmlns="http://www.w3.org/2008/05/skos-xl#" xml:lang="en">products</literalForm>
		//</rdf:Description>
		
		BufferedReader file_buffer = new BufferedReader(new FileReader(filePath));
		String line = "", lineint = "";
		StringBuffer classDescBuffer = new StringBuffer();
		boolean found = false;
		
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
		
		return "";
	}
	
	static void exploreResource(String resource) throws IOException
	{
		BufferedReader file_buffer = new BufferedReader(new FileReader(filePath));
		String line = "", lineint = "";
		StringBuffer classDescBuffer = new StringBuffer();
		boolean found = false;
		
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
	
	static Vector<String> getSpecialised(String resource, OntClass c) throws IOException
	{
		System.out.println(resource);
		BufferedReader file_buffer = new BufferedReader(new FileReader(filePath));
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
						//System.out.println(lineint);
						String[] tokens = lineint.split("\\s");
						String tmp = tokens[3];
						String lineint_resource = tmp.substring(tmp.indexOf('"')+1, tmp.lastIndexOf('"'));
						
						if (lineint_resource.compareTo(resource)==0)
						{
							//found = true;
							//classDescBuffer.append(lineint + "\n");
							//System.out.println(lineint);
							specialized.add(line.substring(line.indexOf('"') +1, line.lastIndexOf('"')));
							//specialized.add(line);
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
			OntClass newC = model.createClass(s);
			newC.addLabel(getPreferredLabel(getPreferredLabelUri(s,  "en")),"en");
			c.addSubClass(newC);
			getSpecialised(s, newC);
			
		}
		file_buffer.close();
		return specialized;
	}
}
