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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

public class ncicpAdapter 
{
	static final String uri = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
	static final String resultOntoFilePath = "./ontologies/1.owl";
	static final String sourceFilePath = "./ontologies/src/ncicp.owl";
	static OntModel model;
	
	public static void main(String[] args) 
	{
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		model.setNsPrefix("ncicp", uri);
		Ontology ont = model.createOntology("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl");
		ont.addComment("Automatically created through Jena APis", "en");
		
		OntClass foodClass = model.createClass(uri + "Food"); // Root concept: Food
		foodClass.addLabel("Food", "en");
		
		try
		{
			findTaxonomyStartingFrom("Food", foodClass);
			FileWriter ncicpOntoFile = new FileWriter(resultOntoFilePath);
			model.write(ncicpOntoFile, "RDF/XML-ABBREV");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	static public void findTaxonomyStartingFrom(String s, OntClass c) throws IOException
	{
		BufferedReader file_buffer = new BufferedReader(new FileReader(sourceFilePath));
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
					if (lineint.contains("<rdfs:subClassOf rdf:resource=\"#"+s+"\"/>"))
						found = true;
					
					if (lineint.trim().compareTo("</owl:Class>")==0)
					{
						classDescBuffer.append(lineint + "\n");
						break;
					}
					else
						classDescBuffer.append(lineint + "\n");
				}
				lineint = "";
				
				if (found)
				{
					Document doc = Jsoup.parse(classDescBuffer.toString(), "", Parser.xmlParser());
					String className = doc.getElementsByTag("owl:Class").first().attr("rdf:about");
					String classNameNormal = className.replace("#", "");
					OntClass subC = model.createClass(uri + classNameNormal);
					subC.addLabel(classNameNormal.replace("_", " "), "en");
					c.addSubClass(subC);
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
			findTaxonomyStartingFrom(subC.getLocalName(), subC);
		}
	}
}
