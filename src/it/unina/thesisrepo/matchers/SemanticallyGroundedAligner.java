package it.unina.thesisrepo.matchers;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Vector;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;


public class SemanticallyGroundedAligner 
{
	OntModel model;
	HashSet<Alignment> linguistic_alignments = new HashSet<Alignment>();
	HashSet<Alignment> sem_gounded_alignments = new HashSet<Alignment>();
	HashSet<Alignment> total_alignments = new HashSet<Alignment>();
	
	public static void main(String[] args) {
		
		try
		{
			SemanticallyGroundedAligner sem_alig = new SemanticallyGroundedAligner(null, "./alignments/8.alignment");
			sem_alig.inferAlphaConsequences();
			sem_alig.visualizeSet();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		
	}
	
	public SemanticallyGroundedAligner(OntModel model, String alignmentFile) throws IOException {
		// TODO Auto-generated constructor stub
		
		//model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
		//model.read(srcOntology);
		this.model = model;
		
		//System.out.println("Handling alignment " + alignmentFile);
		BufferedReader file_buffer = new BufferedReader(new FileReader(alignmentFile));
	    
		String line;
		line = file_buffer.readLine(); // Read comment line
		
		while((line = file_buffer.readLine()) != null)
		{
			String[] tokens = line.split(",");
			String src = tokens[0];
			String dst = tokens[1];
			double[] measures = new double[10];
			try
			{
				measures[0] = Double.parseDouble(tokens[2]);
				measures[1] = Double.parseDouble(tokens[3]);
				measures[2] = Double.parseDouble(tokens[4]);
				measures[3] = Double.parseDouble(tokens[5]);
				measures[4] = Double.parseDouble(tokens[6]);
				measures[5] = Double.parseDouble(tokens[7]);
				measures[6] = Double.parseDouble(tokens[8]);
				measures[7] = Double.parseDouble(tokens[9]);
				measures[8] = Double.parseDouble(tokens[10]);
				String result = tokens[11];
				
				Alignment a = new Alignment(src, dst, measures, result);
				linguistic_alignments.add(a);
				total_alignments.add(a);
				
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}

		}
		
		
		file_buffer.close();
		
	}
	
	public void getStats()
	{
		System.out.println("Linguistic alignment set size: " + linguistic_alignments.size());
		System.out.println("Semantically grounded alignment set size: " + sem_gounded_alignments.size());
		System.out.println("Total alignment set size: " + total_alignments.size());
	}
	
	void visualizeSet()
	{
		System.out.println("linguistic alignment: " + linguistic_alignments.size());
		System.out.println("sem_gounded_alignment: " + sem_gounded_alignments.size());
		
		//linguistic_alignments.retainAll(sem_gounded_alignments);
		
		HashSet<Alignment> intersection = intersection(linguistic_alignments, sem_gounded_alignments);
		HashSet<Alignment> union = union(linguistic_alignments, sem_gounded_alignments);
		
		System.out.println(intersection.size());
		System.out.println(union.size());
		ImmutableSet<Alignment> newSet = Sets.difference(sem_gounded_alignments,intersection).immutableCopy();
		System.out.println("diff1: " + newSet.size());
		
		for (Alignment a:newSet)
			System.out.println(a.src + " " + a.result + " " + a.dst);
	}
	
	public <T> HashSet<T> intersection(HashSet<T> list1, HashSet<T> list2) {
		HashSet<T> list = new HashSet<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }
	
	public <T> HashSet<T> union(HashSet<T> list1, HashSet<T> list2) {

        list1.addAll(list2);

        return list1;
    }
	
	public void inferAlphaConsequences()
	{
		ExtendedIterator<OntClass> classes = model.listNamedClasses();
		while(classes.hasNext())
		{
			OntClass c = classes.next();
			String c_label = c.getLabel("en");
			//System.out.println(c_label);
			if (c.hasSuperClass())
			{
				ExtendedIterator<OntClass> superClasses = c.listSuperClasses(true);
				while(superClasses.hasNext())
				{
					OntClass superClass = superClasses.next();
					String supclassLabel = superClass.getLabel("en");
					
					Vector<Alignment> result = findAlignmentFromSrc(supclassLabel);
					
					System.out.println("***********************"  + c_label + " inherits from " + supclassLabel + ": ");
					for (Alignment a:result)
					{
						
						
						
						if (a.result.compareToIgnoreCase("eqv") == 0 || 
							a.result.compareToIgnoreCase("hypo") == 0)
						{
							// inherits all alignment as hypo
							System.out.println(a.src + "<" + a.result + ">" + a.dst);
							System.out.println(c_label + " hypo " + a.dst + " (" + supclassLabel + ")");
							
							Alignment sem_a  = new Alignment(c_label, a.dst, null, "hypo");
							sem_gounded_alignments.add(sem_a);
							total_alignments.add(sem_a);
						}
						else if (a.result.compareToIgnoreCase("rel") == 0)
						{
							//System.out.println(c_label + " rel " + a.dst + " (" + supclassLabel + ")");
							Alignment sem_a  = new Alignment(c_label, a.dst, null, "rel");
							sem_gounded_alignments.add(sem_a);
							total_alignments.add(sem_a);
						}
						/*else
						{
							//System.out.println(c_label + " dsj " + a.dst + " (" + supclassLabel + ")");
							Alignment sem_a  = new Alignment(c_label, a.dst, null, "dsj");
							sem_gounded_alignments.add(sem_a);
							total_alignments.add(sem_a);
						}*/
					}
				}
			}
		}
	}
	
	Vector<Alignment> findAlignmentFromSrc(String src)
	{
		Vector<Alignment> result = new Vector<Alignment>();
		for (Alignment a:total_alignments)
		{
			if (a.src.compareToIgnoreCase(src)==0)
				result.add(a);
		}
		return result;
	}
	
	public void writeSemanticallyGrounded(String file) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write("#Semantically grounded alignments\n");
		for (Alignment a:sem_gounded_alignments)
			writer.write(a.src + "," + a.dst + ","+ a.result + "\n");
		writer.close();
	}
}
