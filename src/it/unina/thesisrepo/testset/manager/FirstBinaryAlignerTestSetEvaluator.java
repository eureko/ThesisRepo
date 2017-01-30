package it.unina.thesisrepo.testset.manager;

import it.unina.thesisrepo.testset.manager.AlignmentHandler.Alignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class FirstBinaryAlignerTestSetEvaluator 
{
	static final String alignmentFolder = "./alignments";
	static HashSet<Alignment> allAlignments = new HashSet<Alignment>();
	static Alignment[] alignArray; 
	
	static List<Alignment> alList;
	
	HashSet<Alignment> groundedAlignment = new HashSet<Alignment>();
	
	static final String measuresFile = alignmentFolder + "/test_alignment_measures_ext.csv";
	static final String testAlignment = alignmentFolder + "/test_alignment_binary_ext.csv";
	
	static final double hyp_t_low_index = 0.0;
	static final double hyp_t_high_index = 1.0;
	static final double hyp_t_step = 0.05;
	
	static DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
	static NumberFormat form = new DecimalFormat("#0.00"); 
	
	static int tp, tn, fp, fn = 0;
	
	public static void main(String[] args) 
	{
		try
		{
			System.out.println("(Precision, Recall)");
			for (double i = hyp_t_low_index; i <= hyp_t_high_index; i = i + hyp_t_step)
			{
				FirstBinaryAligner aligner = new FirstBinaryAligner(1.0, 0.0+i, measuresFile, testAlignment);
				aligner.startAligning();
				collectAllAlignments();
				
				FirstBinaryAlignerTestSetEvaluator testSetEvaluator = new FirstBinaryAlignerTestSetEvaluator();
				allAlignments = new HashSet<Alignment>();
				
				double precision = (double)((double)tp/(double)(tp+fp));
				double recall = (double)((double)tp/(double)(tp+fn));
				
				System.out.println("" + i + ": " + tp + " " + tn + " " + fp + " " + fn);
				
				double tpr = (double)((double)tp/(double)(tp+fn));
				double fpr = (double)((double)fp/(double)(fp+tn));
				
				//System.out.print("(" + String.format("%.3g", fpr).replace(',','.') + ","+
					//	String.format("%.3g", tpr).replace(',','.')+")");
				
				//System.out.print("(" + String.format("%.3g", recall).replace(',','.') + ","+
					//String.format("%.3g", precision).replace(',','.')+")");
				
				tp = 0;
				tn = 0;
				fp = 0;
				fn = 0;
				
			}
				
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	public FirstBinaryAlignerTestSetEvaluator() throws IOException
	{
		//System.out.println("Reading ground file...");
		BufferedReader file_buffer = new BufferedReader(new FileReader("./alignments/groundtruth_ext.csv"));
		String line;
		//line = file_buffer.readLine(); // Read comment line
		
		int num_of_tested_alignment = 0;
		
		while((line = file_buffer.readLine()) != null)
		{
			try
			{
				String[] tokens = line.split(";");
				String src = tokens[0];
				String dst = tokens[1];
				String ground = tokens[2];
				
				Alignment current_a = new Alignment(src,dst, null, null, ground);
				if (!groundedAlignment.contains(current_a))
				{
					groundedAlignment.add(current_a);
					Alignment expectedAlignment = getAlignmentFromCollection(src,dst);
					
					if (expectedAlignment != null)
					{
						num_of_tested_alignment++;
						String expAlignResult = expectedAlignment.result;
						if (expAlignResult.compareTo("eqv") == 0)
						{
							// Nothing to do
						}
						else if (expAlignResult.compareTo("hypo+") == 0)
						{
							if (ground.compareTo("eqv") == 0)
							{
								// Nothing
							}
							else if (ground.compareTo("hyper") == 0)
							{
								tp++;
							}
							else if (ground.compareTo("hypo")== 0)
							{
								tp++;
							}
							else if (ground.compareTo("rel")== 0)
							{
								fp++;
							}
							else // dsj case
							{
								fp++;
							}
							
						}
						else if (expAlignResult.compareTo("rel-") == 0)
						{
							if (ground.compareTo("eqv") == 0)
							{
								//
							}
							else if (ground.compareTo("hyper") == 0)
							{
								fn++;
							}
							else if (ground.compareTo("hypo")== 0)
							{
								fn++;
							}
							else if (ground.compareTo("rel")== 0)
							{
								tn++;
							}
							else // dsj case
							{
								tn++;
							}
						}
					}
					else 
					{
						//System.out.println("Alignment: " + src + ", " + dst + " not found!");
					}
				}
			}
			catch(Exception ex)
			{
				System.out.println("ERROR in: " + line);
			}
		}
		
		//System.out.println("Num of tested alignments (groundtruth size): " + num_of_tested_alignment);
		file_buffer.close();
	}
	
	Alignment getAlignmentFromCollection(String src, String dst)
	{
		Alignment res = null;
		for (Alignment a:allAlignments)
		{
			if (a.src.compareToIgnoreCase(src)== 0 && a.dst.compareToIgnoreCase(dst)== 0)
				res = a;
		}
		
		return res;
			
	}
	
	static void collectAllAlignments() throws IOException
	{
		// collect all alignments
		
			//System.out.println("Reading alignment...");
			BufferedReader file_buffer = new BufferedReader(new FileReader(testAlignment));
		    
			String line;
			line = file_buffer.readLine(); // Read comment line
			
			while((line = file_buffer.readLine()) != null)
			{
				try
				{
					String[] tokens = line.split(",");
					String src = tokens[0];
					String dst = tokens[1];
					String exp = tokens[11];
					String ground = tokens[12];
					
					Alignment a = new Alignment(src, dst, null, exp, null);
					allAlignments.add(a);
				}
				catch(Exception ex)
				{
					System.out.println(line);
					ex.printStackTrace();
				}

			}
			//System.out.println("End reading alignment...");
			file_buffer.close();
	}
}
	
	