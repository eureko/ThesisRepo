package it.unina.thesisrepo.framework;

import it.unina.thesisrepo.matchers.SemanticallyGroundedAligner;
import it.unina.thesisrepo.matchers.TermsSetMeasuresCalculator;
import it.unina.thesisrepo.testset.manager.Aligner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class FrameworkMainClass 
{
	static final String ontoCorpusPath = "./ontologies/urls.txt/";
	static final String targetPath = "./ontologies/target.csv";
	
	
	static TreeSet<String> target_terms_set = new TreeSet<String>();
	
	public static void main(String[] args) 
	{
		System.out.println("Accessing the ontology corpus...");
		
		try
		{
			BufferedReader file_buffer = new BufferedReader(new FileReader(ontoCorpusPath));
			String line;
			
			TermsSetMeasuresCalculator.createTargetSet(targetPath);
			
			while((line = file_buffer.readLine()) != null)
			{
				String[] tokens = line.split("\\|");
				String index = tokens[0];
				int index_i = Integer.parseInt(index);
				String ontoName = tokens[1];
				
				if (index_i > 3)
				{
					System.out.println("Processing ontology: \n" + index + ". " + ontoName);
					
					OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
					model.read("./ontologies/"+index+".owl");
					
					// Start flattening...
					System.out.println("Flattening ontology " + ontoName);
					BufferedWriter file_writer = new BufferedWriter(new FileWriter("./ontologies/"+index+".csv"));
					file_writer.write("#" + ontoName + "\n");
					ExtendedIterator<OntClass> classes = model.listNamedClasses();
					while(classes.hasNext())
						file_writer.write(classes.next().getLabel("en") + "\n");
					file_writer.close();
					
					// Start measuring...
					System.out.println("Start measuring " + ontoName);
					TermsSetMeasuresCalculator termsSetCalculator = new TermsSetMeasuresCalculator("./ontologies/"+index+".csv", targetPath);
					termsSetCalculator.startMeasuring("./alignments/" + index + ".measures");
					
					// Start Aligning
					System.out.println("Start aligning " + ontoName);
					Aligner aligner = new Aligner("./alignments/"+index+".measures", "./alignments/"+index+".alignment");
					aligner.startAligning();
					
					// Start semantically grounded alignment
					/*System.out.println("Start semantically grounded aligning on " + ontoName);
					SemanticallyGroundedAligner sem_alig = new SemanticallyGroundedAligner(model, "./alignments/"+index+".alignment");
					sem_alig.inferAlphaConsequences();
					sem_alig.getStats();
					sem_alig.writeSemanticallyGrounded("./alignments/"+index+".alignment.sem");*/
					
				}
			}
			file_buffer.close();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}
