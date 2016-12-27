package it.unina.thesisrepo.ontoadapters;

import java.io.File;
import java.io.FileWriter;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class EurocodeTaxonomyAdapter 
{
	static String SOURCE = "http://www.danfood.info/eurocode/";
	static String NS = SOURCE + "#";
	
	public static void main(String[] args) 
	{
		try
		{
			/*BufferedReader file_buffer = new BufferedReader(new FileReader("./ontologies/src/Eurocode.txt"));
		    
			String line;
			line = file_buffer.readLine(); // Read comment line
			
			while((line = file_buffer.readLine()) != null)
			{
				String[] splited = line.split("\\s+");
				System.out.println(line.substring(splited[0].length()).trim());
				//System.out.println(splited[0]);
			}*/
			
			FileWriter eurocodeOntoFile = new FileWriter("./ontologies/8.owl");
			
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
			OntDocumentManager dm = model.getDocumentManager();
			
			//model.setNsPrefix("eurocode", SOURCE);
			
			File input = new File("./ontologies/src/Eurocode2.html");
			Document doc = Jsoup.parse(input, "UTF-8");
			
			Element table = doc.select("table").first();
			
			Elements trElems = table.getElementsByTag("tr");
			
			OntClass firstLevelClass = null;
			
			for (int i = 0; i < trElems.size(); i++)
			{
				Element a_ref = trElems.get(i).getElementsByTag("a").first();
				
				if (a_ref != null)
				{
					String className = trElems.get(i).getElementsByTag("strong").first().text();
					String capitalizedClassNAme = capitalizeString(className).replace(" ", "");
					
					//System.out.println(capitalizedClassNAme);
					
					firstLevelClass = model.createClass(NS + capitalizedClassNAme);
					firstLevelClass.addLabel(className, "en");
				}
				else 
				{
					Elements tdElems = trElems.get(i).getElementsByTag("TD");
					String index = tdElems.get(0).getElementsByTag("strong").first().text();
					String capitalizedClassName = capitalizeString(tdElems.get(1).text()).replace(" ","");
					String label = tdElems.get(1).text();
					String comment = tdElems.get(2).text();
					
					//System.out.println("\t" + capitalizedClassName);
					OntClass secondLevelCalss = model.createClass(NS + capitalizedClassName);
					secondLevelCalss.addComment(comment, "en");
					secondLevelCalss.addLabel(label, "en");
					
					firstLevelClass.addSubClass(secondLevelCalss);
				}
			}
			
			ExtendedIterator<OntClass> classIter = model.listNamedClasses();
			
			while(classIter.hasNext())
			{
				System.out.println(classIter.next().getLocalName());
			}
			
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
			
			model.write(eurocodeOntoFile, "RDF/XML-ABBREV", SOURCE);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
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
