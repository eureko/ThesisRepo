package it.unina.thesisrepo.testset.manager;

import it.unina.thesisrepo.testset.manager.AlignmentHandler.Alignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.ejml.data.Matrix;
import org.ejml.simple.SimpleMatrix;

public class TestSetEvaluator 
{
	static final int num_of_alignments = 11;
	static HashSet<Alignment> allAlignments = new HashSet<Alignment>();
	static Alignment[] alignArray; 
	
	static List<Alignment> alList;
	
	static HashSet<Alignment> groundedAlignment = new HashSet<Alignment>();
	
	double[][] confusionMatrix = new double[][]{{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
	double[][] symmetricPrecisionRelaxationMatrix = new double[][]{{1.0,0,0,0},{0,1.0,0.5,0},{0,0.5,1.0,0.25},{0,0,0.25,1.0}};
	double[][] symmetricRecallRelaxationMatrix = new double[][]{{1.0,0,0,0},{0,1.0,0.5,0},{0,0.5,1.0,0.25},{0,0,0.25,1.0}};
	
	SimpleMatrix C;
	SimpleMatrix symPRM = new SimpleMatrix(symmetricPrecisionRelaxationMatrix);
	
	
	public static void main(String[] args) 
	{
		try
		{
			collectAllAlignments();
			TestSetEvaluator testSetEvaluator = new TestSetEvaluator();
			testSetEvaluator.visualizeConfusionMatrix();
			testSetEvaluator.getStatisticsV2();
			
			
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
		
		C = new SimpleMatrix(confusionMatrix);
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
		double eqv_recall = (double)((double)confusionMatrix[0][0] / (double)(confusionMatrix[0][0] + confusionMatrix[0][1] + confusionMatrix[0][2] + confusionMatrix[0][3]));
		double hyp_recall = (double)((double)confusionMatrix[1][1] / (double)(confusionMatrix[1][0] + confusionMatrix[1][1] + confusionMatrix[1][2] + confusionMatrix[1][3]));
		double rel_recall = (double)(double)(confusionMatrix[2][2] / (double)(confusionMatrix[2][0] + confusionMatrix[2][1] + confusionMatrix[2][2] + confusionMatrix[2][3]));
		double dsj_recall = (double)((double)confusionMatrix[3][3] / (double)(confusionMatrix[3][0] + confusionMatrix[3][1] + confusionMatrix[3][2] + confusionMatrix[3][3]));
		
		double eqv_accuracy = (double)((double)confusionMatrix[0][0] / (double)(confusionMatrix[0][0] + confusionMatrix[1][0] + confusionMatrix[2][0] + confusionMatrix[3][0]));
		double hyp_accuracy = (double)((double)confusionMatrix[1][1] / (double)(confusionMatrix[0][1] + confusionMatrix[1][1] + confusionMatrix[2][1] + confusionMatrix[3][1]));
		double rel_accuracy = (double)(double)(confusionMatrix[2][2] / (double)(confusionMatrix[0][2] + confusionMatrix[1][2] + confusionMatrix[2][2] + confusionMatrix[3][2]));
		double dsj_accuracy = (double)((double)confusionMatrix[3][3] / (double)(confusionMatrix[0][3] + confusionMatrix[1][3] + confusionMatrix[2][3] + confusionMatrix[3][3]));
				
		double rel_eqv_accuracy = (double)((double)confusionMatrix[0][0] / (double)(confusionMatrix[0][0] + confusionMatrix[1][0] + confusionMatrix[2][0] + confusionMatrix[3][0]));
		double rel_hyp_accuracy = (double)(double)((confusionMatrix[1][1] + 0.5*confusionMatrix[2][1]) / (double)(confusionMatrix[0][1] + confusionMatrix[1][1] + confusionMatrix[2][1] + confusionMatrix[3][1]));
		double rel_rel_accuracy = (double)(double)((confusionMatrix[1][2]*0.5 + confusionMatrix[2][2] + 0.25*confusionMatrix[3][2]) / (double)(confusionMatrix[0][2] + confusionMatrix[1][2] + confusionMatrix[2][2] + confusionMatrix[3][2]));
		double rel_dsj_accuracy = (double)((double)((confusionMatrix[3][3] + 0.25*confusionMatrix[2][3])) / (double)(confusionMatrix[0][3] + confusionMatrix[1][3] + confusionMatrix[2][3] + confusionMatrix[3][3]));
		
		double rel_eqv_recall = (double)((double)confusionMatrix[0][0] / (double)(confusionMatrix[0][0] + confusionMatrix[0][1] + confusionMatrix[0][2] + confusionMatrix[0][3]));
		double rel_hyp_recall = (double)((double)((confusionMatrix[1][1] + 0.5*confusionMatrix[1][2])) / (double)(confusionMatrix[1][0] + confusionMatrix[1][1] + confusionMatrix[1][2] + confusionMatrix[1][3]));
		double rel_rel_recall = (double)(double)((confusionMatrix[2][1]*0.5 + confusionMatrix[2][2] + 0.25*confusionMatrix[2][3]) / (double)(confusionMatrix[2][0] + confusionMatrix[2][1] + confusionMatrix[2][2] + confusionMatrix[2][3]));
		double rel_dsj_recall = (double)((double)(confusionMatrix[3][3] + 0.25*confusionMatrix[3][2]) / (double)(confusionMatrix[3][0] + confusionMatrix[3][1] + confusionMatrix[3][2] + confusionMatrix[3][3]));
		
		int sum = 0;
		
		
		for (int i = 0 ; i < 4; i ++)
		{
			for (int j = 0; j < 4; j++)
			{
				sum += confusionMatrix[i][j];
			}
		}
		
		
		double totalAccuracy = ((double)(confusionMatrix[0][0] + confusionMatrix[1][1] + confusionMatrix[2][2] + confusionMatrix[3][3])/(double)sum);
		double rel_totalAccuracy = ((double)(confusionMatrix[0][0] + (confusionMatrix[1][1] + 0.5*confusionMatrix[2][1]) + (0.5*confusionMatrix[1][2] + confusionMatrix[2][2] + 0.25*confusionMatrix[3][2] )+ (confusionMatrix[3][3] + 0.25*confusionMatrix[2][3]))/(double)sum);
		System.out.println("specific accuracy(recall): " );
		System.out.println("eqv" + "\t\t" + "hypo" + "\t\t" + "rel" + "\t\t" + "dsj" + "\t\t" + "tot acc.");
		System.out.println(String.format("%.3g", eqv_accuracy) + "(" + String.format("%.3g", eqv_recall) + ")"+ "\t" + String.format("%.3g", hyp_accuracy) + "(" + String.format("%.3g", hyp_recall) + ")"+ "\t" + 
				String.format("%.3g", rel_accuracy) + "(" + String.format("%.3g", rel_recall) + ")"+ "\t" +  String.format("%.3g", dsj_accuracy) + "(" + String.format("%.3g", dsj_recall) + ")"+ "\t" + String.format("%.3g", totalAccuracy));
		
		System.out.println(String.format("%.3g", rel_eqv_accuracy) + "(" + String.format("%.3g", rel_eqv_recall) + ")"+ "\t" + String.format("%.3g", rel_hyp_accuracy) + "(" + String.format("%.3g", rel_hyp_recall) + ")"+ "\t" + 
				String.format("%.3g", rel_rel_accuracy) + "(" + String.format("%.3g", rel_rel_recall) + ")"+ "\t" +  String.format("%.3g", rel_dsj_accuracy) + "(" + String.format("%.3g", rel_dsj_recall) + ")"+ "\t" + String.format("%.3g", rel_totalAccuracy) + "\n");
	}
	
	void getStatisticsV2()
	{
		SimpleMatrix accuracies = getAccuracies(C, null);
		SimpleMatrix recall = getRecall(C, null);
		
		SimpleMatrix rel_accuracies = getAccuracies(C, symPRM);
		SimpleMatrix rel_recall = getRecall(C, symPRM);
		
		System.out.println("specific accuracy(recall): " );
		System.out.println("eqv" + "\t\t" + "hypo" + "\t\t" + "rel" + "\t\t" + "dsj" + "\t\t" + "tot acc.");
		System.out.println(String.format("%.3g", accuracies.get(0)) + "(" + String.format("%.3g", recall.get(0)) + ")"+ "\t" + String.format("%.3g", accuracies.get(1)) + "(" + String.format("%.3g", recall.get(1)) + ")"+ "\t" + 
				String.format("%.3g", accuracies.get(2)) + "(" + String.format("%.3g", recall.get(2)) + ")"+ "\t" +  String.format("%.3g", accuracies.get(3)) + "(" + String.format("%.3g", recall.get(3)) + ")"+ "\t" + String.format("%.3g", C.extractDiag().elementSum()/C.elementSum()));
		System.out.println(String.format("%.3g", rel_accuracies.get(0)) + "(" + String.format("%.3g", rel_recall.get(0)) + ")"+ "\t" + String.format("%.3g", rel_accuracies.get(1)) + "(" + String.format("%.3g", rel_recall.get(1)) + ")"+ "\t" + 
				String.format("%.3g", rel_accuracies.get(2)) + "(" + String.format("%.3g", rel_recall.get(2)) + ")"+ "\t" +  String.format("%.3g", rel_accuracies.get(3)) + "(" + String.format("%.3g", rel_recall.get(3)) + ")"+ "\t" + String.format("%.3g", C.transpose().mult(symPRM).extractDiag().elementSum()/C.elementSum()));
	}
	
	SimpleMatrix getAccuracies(SimpleMatrix C, SimpleMatrix R)
	{
		SimpleMatrix result;
		if (R == null)
		{
			SimpleMatrix diag = C.extractDiag();
			double[][] tmp = new double[C.numCols()][1];
			for (int i = 0; i < C.numCols(); i++)
			{
				tmp[i][0] = C.extractVector(false, i).elementSum();
			}
			SimpleMatrix rowSumVect = new SimpleMatrix(tmp);
			result = diag.elementDiv(rowSumVect);
		}
		else
		{
			SimpleMatrix diag = C.transpose().mult(R).extractDiag();
			double[][] tmp = new double[C.numCols()][1];
			for (int i = 0; i < C.numCols(); i++)
			{
				tmp[i][0] = C.extractVector(false, i).elementSum();
			}
			
			SimpleMatrix rowSumVect = new SimpleMatrix(tmp);
			result = diag.elementDiv(rowSumVect);
		}
		
		return result;
	}
	
	SimpleMatrix getRecall(SimpleMatrix C, SimpleMatrix R)
	{
		SimpleMatrix result;
		if (R == null)
		{
			SimpleMatrix diag = C.extractDiag();
			double[][] tmp = new double[C.numCols()][1];
			for (int i = 0; i < C.numRows(); i++)
			{
				tmp[i][0] = C.extractVector(true, i).elementSum();
			}
			
			SimpleMatrix rowSumVect = new SimpleMatrix(tmp);
			result = diag.elementDiv(rowSumVect);
		}
		else
		{
			SimpleMatrix diag = C.mult(R).extractDiag();
			double[][] tmp = new double[C.numCols()][1];
			for (int i = 0; i < C.numRows(); i++)
			{
				tmp[i][0] = C.extractVector(true, i).elementSum();
			}
			
			SimpleMatrix rowSumVect = new SimpleMatrix(tmp);
			result = diag.elementDiv(rowSumVect);
		}
		return result;
	}
	
	/*double[] relaxConfusionMatrix(int[][] confusionMatrix, double[][] relaxationMatrix, int type)
	{
		double[] relaxed = new double[confusionMatrix.length];
		double sum = 0.0;
		if (type == 1) // relax recall
		{
		
			for (int i = 0; i < confusionMatrix.length; i++)
			{
				for (int j = 0; j < confusionMatrix.length; j++)
				{
					sum += confusionMatrix[i]
				}
			}
		}
		else //  relax accuracy
		{
			
		}
		
		return relaxedAccuracies
	}*/
}
