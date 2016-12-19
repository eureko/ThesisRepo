package it.unina.thesisrepo.testset.manager;

import it.unina.thesisrepo.testset.manager.AlignmentHandler.Alignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

public class TestSetEvaluator 
{
	static final int num_of_alignments = 11;
	static HashSet<Alignment> allAlignments = new HashSet<Alignment>();
	static Alignment[] alignArray; 
	
	static List<Alignment> alList;
	
	static HashSet<Alignment> groundedAlignment = new HashSet<Alignment>();
	
	int[][] confusionMatrix = new int[][]{{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
	
	public static void main(String[] args) 
	{
		try
		{
			collectAllAlignments();
			TestSetEvaluator testSetEvaluator = new TestSetEvaluator();
			testSetEvaluator.visualizeConfusionMatrix();
			testSetEvaluator.getStatistics();
			
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	public TestSetEvaluator() throws IOException
	{
		System.out.println("Reading ground file...");
		BufferedReader file_buffer = new BufferedReader(new FileReader("./alignments/groundtruth.csv"));
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
							if (ground.compareTo("eqv") == 0)
							{
								confusionMatrix[0][0]++; 
							}
							else if (ground.compareTo("hypo") == 0 || ground.compareTo("hyper")== 0)
							{
								confusionMatrix[1][0]++;
							}
							else if (ground.compareTo("rel")== 0)
							{
								confusionMatrix[2][0]++;
							}
							else // dsj case
							{
								confusionMatrix[3][0]++;
							}
						}
						else if (expAlignResult.compareTo("hypo") == 0)
						{
							if (ground.compareTo("eqv") == 0)
							{
								confusionMatrix[0][1]++; 
							}
							else if (ground.compareTo("hypo") == 0 || ground.compareTo("hyper")== 0)
							{
								confusionMatrix[1][1]++;
							}
							else if (ground.compareTo("rel")== 0)
							{
								confusionMatrix[2][1]++;
							}
							else // dsj case
							{
								confusionMatrix[3][1]++;
							}
						}
						else if (expAlignResult.compareTo("rel") == 0)
						{
							if (ground.compareTo("eqv") == 0)
							{
								confusionMatrix[0][2]++; 
							}
							else if (ground.compareTo("hypo") == 0 || ground.compareTo("hyper")== 0)
							{
								confusionMatrix[1][2]++;
							}
							else if (ground.compareTo("rel")== 0)
							{
								confusionMatrix[2][2]++;
							}
							else // dsj case
							{
								confusionMatrix[3][2]++;
							}
						}
						else // dsj case
						{
							if (ground.compareTo("eqv") == 0)
							{
								confusionMatrix[0][3]++; 
							}
							else if (ground.compareTo("hypo") == 0 || ground.compareTo("hyper")== 0)
							{
								confusionMatrix[1][3]++;
								//System.out.println("MISMATCH: src: " + src + " dst: " + dst);
							}
							else if (ground.compareTo("rel")== 0)
							{
								confusionMatrix[2][3]++;
							}
							else // dsj case
							{
								confusionMatrix[3][3]++;
							}
						}
					}
					else 
					{
						System.out.println("Alignment: " + src + ", " + dst + " not found!");
					}
				}
			}
			catch(Exception ex)
			{
				System.out.println("ERROR in: " + line);
			}
		}
		System.out.println("Num of tested alignments (groundtruth size): " + num_of_tested_alignment);
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
		
			System.out.println("Reading alignment...");
			BufferedReader file_buffer = new BufferedReader(new FileReader("./alignments/test_alignment.csv"));
		    
			String line;
			line = file_buffer.readLine(); // Read comment line
			
			while((line = file_buffer.readLine()) != null)
			{
				
				
				try
				{
					String[] tokens = line.split(",");
					String src = tokens[0];
					String dst = tokens[1];
					String exp = tokens[12];
					String ground = tokens[13];
					
					Alignment a = new Alignment(src, dst, null, exp, null);
					allAlignments.add(a);
				}
				catch(Exception ex)
				{
					System.out.println(line);
					ex.printStackTrace();
				}

			}
			System.out.println("End reading alignment...");
			file_buffer.close();
	}
	
	void visualizeConfusionMatrix()
	{
		System.out.println("Confusion Matrix:\n");
		System.out.print("\t" + "eqv" +"\t" + "hypo" +"\t" + "rel" + "\t" + "dsj");
		String[] headers = new String[]{"eqv", "hypo", "rel", "dsj"};
		for (int i = 0 ; i < 4; i ++)
		{
			System.out.print("\n" + headers[i] + "\t");
			for (int j = 0; j < 4; j++)
			{
				System.out.print(confusionMatrix[i][j] + "\t");
			}
			
		}
		System.out.print("\n\n");
	}
	
	void getStatistics()
	{
		double eqv_accuracy = (double)((double)confusionMatrix[0][0] / (double)(confusionMatrix[0][0] + confusionMatrix[0][1] + confusionMatrix[0][2] + confusionMatrix[0][3]));
		double hyp_accuracy = (double)((double)confusionMatrix[1][1] / (double)(confusionMatrix[1][0] + confusionMatrix[1][1] + confusionMatrix[1][2] + confusionMatrix[1][3]));
		double rel_accuracy = (double)(double)(confusionMatrix[2][2] / (double)(confusionMatrix[2][0] + confusionMatrix[2][1] + confusionMatrix[2][2] + confusionMatrix[2][3]));
		double dsj_accuracy = (double)((double)confusionMatrix[3][3] / (double)(confusionMatrix[3][0] + confusionMatrix[3][1] + confusionMatrix[3][2] + confusionMatrix[3][3]));
		
		int sum = 0;
		
		
		for (int i = 0 ; i < 4; i ++)
		{
			for (int j = 0; j < 4; j++)
			{
				sum += confusionMatrix[i][j];
			}
		}
		
		double totalAccuracy = ((double)(confusionMatrix[0][0] + confusionMatrix[1][1] + confusionMatrix[2][2] + confusionMatrix[3][3])/(double)sum);
		System.out.println("eqv_accuracy: " + eqv_accuracy + " hyp_accuracy: " + hyp_accuracy + " rel_accuracy: " + rel_accuracy + " dsj_accuracy: " + dsj_accuracy);
		System.out.println("Total accuracy: " + totalAccuracy);
	}
}
