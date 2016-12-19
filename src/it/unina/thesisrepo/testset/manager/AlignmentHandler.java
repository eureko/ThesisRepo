package it.unina.thesisrepo.testset.manager;

import it.unina.thesisrepo.testset.manager.TermsSetHandler.Alignment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class AlignmentHandler 
{
	static final int num_of_alignments = 11;
	static HashSet<Alignment> allAlignments = new HashSet<Alignment>();
	static Measure M;
	
	
	public static void main(String[] args) 
	{
		try
		{
			Stemmer s = new Stemmer();
			s.add("Oilseeds".toCharArray(), "Oilseeds".length());
			s.stem();
			System.out.println(s.toString());
			
			collectAllAlignments();
			//visualizeAlignmentSet(allAlignments);
			//writeOnFile(allAlignments);
			searchFor("Oilseeds", null);
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	static void collectAllAlignments() throws IOException
	{
		// collect all alignments
		
		for (int i = 0; i < num_of_alignments; i++)
		{
			System.out.println("Handling alignment " + (i+1));
			BufferedReader file_buffer = new BufferedReader(new FileReader("./alignments/"+(i+1) + ".alignment"));
		    
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
					measures[8] = Double.parseDouble(tokens[11]);
					String result = tokens[12];
					
					Alignment a = new Alignment(src, dst, measures, result, null);
					allAlignments.add(a);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}

			}
			file_buffer.close();
		}	
	}
	
	static void searchFor(String src, String dst)
	{
		for (Alignment a:allAlignments)
		{
			if (src != null && dst != null)
			{
				if (a.src.compareToIgnoreCase(src)== 0 && a.dst.compareToIgnoreCase(dst)==0)
					System.out.println(a);
			}
			else if (src != null)
			{
				if (a.src.compareToIgnoreCase(src)== 0)
					System.out.println(a);
			}
			else
			{
				if (a.dst.compareToIgnoreCase(dst)== 0)
					System.out.println(a);
			}
		}
	}
	
	static void visualizeAlignmentSet(Set<Alignment> set)
	{
		
		int i = 0;
		for (Alignment a:set)
		{
			if (a.result.compareTo("dsj") == 0)// && (a.measures[M.wup.ordinal()] > 0.9 && a.measures[M.wup.ordinal()] < 1.0))
			{
				System.out.println(a);
				i++;
			}
		}
		System.out.println("The set contains " + set.size() + " elements. Filtered: " + i);
	}
	
	static void writeOnFile(Set<Alignment> set) throws IOException
	{
		BufferedWriter file_buffer = new BufferedWriter(new FileWriter("alignments/alignment.csv"));
		file_buffer.write("#src,dst,exp,grd\n");
		
		for (Alignment a:set)
		{
			file_buffer.write(a.src + "," + a.dst + "," + a.result + ",?\n");
		}
		file_buffer.close();
	}
	
	static class Alignment
	{
		
		String src;
		String dst;
		double measures[];
		String result;
		String ground;
		
		Alignment(String src, String dst, double[] measures, String result, String ground)
		{
			this.src = src;
			this.dst = dst;
			this.measures = measures;
			this.result = result;
			this.ground = ground;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.toLowerCase().hashCode());
			result = prime * result + ((src == null) ? 0 : src.toLowerCase().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Alignment other = (Alignment) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.toLowerCase().equals(other.dst.toLowerCase()))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.toLowerCase().equals(other.src.toLowerCase()))
				return false;
			return true;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return src + " " + result + " " + dst  + "\t\t(" + measures[0] + " " + measures[1] + " " + measures[2] + " " + measures[3] + " "
					+ measures[4] + " " + measures[5] + " " + measures[6] + " " + measures[7] + " " + measures[8] + ")";
		}
	
	}
	
	static enum Measure{
		
		str,lev,jac,fuz,syn,cos,wup,path,ext,fun
	}
}
