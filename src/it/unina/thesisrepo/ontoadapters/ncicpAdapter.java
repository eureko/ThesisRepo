package it.unina.thesisrepo.ontoadapters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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

public class ncicpAdapter 
{
	static final String uri = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
	static FileInputStream fileInputStreamReader;
	BufferedReader file_buffer;
	static OntModel model;
	
	public static void main(String[] args) 
	{
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		model.setNsPrefix("ncicp", uri);
		//model.read("./ontologies/src/ncicp.owl", "RDF/XML");
		//System.out.println(model);
		
		OntClass foodClass = model.createClass(uri + "#Food");
		
		try
		{
			fileInputStreamReader = new FileInputStream("./ontologies/src/ncicp.owl");
			findTaxonomyStartingFrom("#Food", foodClass);
			
			//ExtendedIterator<OntClass> classIter = model.listNamedClasses();
			
			/*while(classIter.hasNext())
			{
				System.out.println(classIter.next().getLocalName());
			}*/
			
			/*while(classIter.hasNext())
			{
				OntClass c = classIter.next();
				if (c.hasSubClass())
				{
					System.out.println(c);
					ExtendedIterator<OntClass> subClasses = c.listSubClasses();
					while(subClasses.hasNext())
						System.out.println("  --> " + subClasses.next()) ;
				}
			}*/
			//fileInputStreamReader.getChannel().position(0);
			FileWriter ncicpOntoFile = new FileWriter("./ontologies/1.owl");
			
			model.write(ncicpOntoFile, "RDF/XML-ABBREV", uri);

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
			
	}
	
	static public void findTaxonomyStartingFrom(String s, OntClass c) throws IOException
	{
		BufferedReader file_buffer = new BufferedReader(new FileReader("./ontologies/src/ncicp.owl"));
		String line = "", lineint = "";
		StringBuffer classDescBuffer = new StringBuffer();
		boolean found = false;
		System.out.println(s);
		while((line = file_buffer.readLine()) != null)
		{
			if (line.contains("<owl:Class rdf:about"))
			{
				classDescBuffer.append(line + "\n");
				
				while((lineint = file_buffer.readLine()) != null)
				{
					if (lineint.contains("<rdfs:subClassOf rdf:resource=\""+s+"\"/>"))
					{
						found = true;
					}
					
					if (lineint.trim().compareTo("</owl:Class>")==0)
					{
						classDescBuffer.append(lineint + "\n");
						break;
					}
					else
						classDescBuffer.append(lineint + "\n");
						
				}
				
				lineint = "";
				
				//String s = line.substring(line.indexOf('#')+1,line.lastIndexOf('"'));
				//System.out.println(s);
				//file_writer.write(s + "\n");
				
				if (found)
				{
					Document doc = Jsoup.parse(classDescBuffer.toString(), "", Parser.xmlParser());
					String className = doc.getElementsByTag("owl:Class").first().attr("rdf:about");
					System.out.println("\t" + className);
					OntClass subC = model.createClass(uri + className);
					c.addSubClass(subC);
					
					
					//System.out.println("*************************");
				}
				
				found = false;
				
				classDescBuffer = new StringBuffer();
			}
		}
		file_buffer.close();
		
		ExtendedIterator<OntClass> subClasses = c.listSubClasses(true);
		
		while(subClasses.hasNext())
		{
			OntClass subC = subClasses.next();
			findTaxonomyStartingFrom("#" + subC.getLocalName(), subC);
		}
		
		
	}
}
