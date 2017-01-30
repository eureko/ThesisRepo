package it.unina.thesisrepo.testset.manager;

import it.unina.thesisrepo.matchers.SemanticallyGroundedAligner;
import it.unina.thesisrepo.testset.manager.AlignmentHandler.Alignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.ejml.simple.SimpleMatrix;

import weka.classifiers.evaluation.Prediction;
import weka.gui.visualize.plugins.ConfusionMatrix;
import weka.gui.visualize.plugins.HeatmapVisualization;

public class CopyOfAlignerTestSetEvaluator 
{
	static final String alignmentFolder = "./alignments";
	static HashSet<Alignment> allAlignments = new HashSet<Alignment>();
	static Alignment[] alignArray; 
	
	static List<Alignment> alList;
	
	HashSet<Alignment> groundedAlignment = new HashSet<Alignment>();
	
	double[][] confusionMatrix = new double[][]{{0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0}};
	double[][] symmetricPrecisionRelaxationMatrix = new double[][]{{1.0,0,0,0,0},{0,1.0,0.5,0,0},{0,0.5,1.0,0.25,0},{0,0,0.25,1.0,0},{0,0,0,0,1}};
	
	
	
	static double[][] symmetricRecallRelaxationMatrix = new double[][]{{1.0,	0.0,	0.0,	0.0,	0.0,	0.0},
																{0.0,	1.0,	0.0,	0.0,	0.0,	0.0},
																{0.0,	0.0,	1.0,	0.0,	0.0,	0.0},
																{0.0,	0.0,	0.0,	1.0,	0.0,	0.0},
																{0.0,	0.0,	0.0,	0.0,	1.0,	0.0},
																{1.0,	1.0,	1.0,	1.0,	1.0,	1.0}};
	
	/*static double[][] asymPrecisionEditRelaxationMatrix = new double[][]{{1.0,	0.0,	0.0,	0.0,	0.0,	1.0},
																{0.0,	1.0,	1.0,	1.0,	1.0,	1.0},
																{0.0,	0.0, 	1.0,	0.0,	0.0,	1.0},
																{0.0,	1.0, 	0.75,	1.0, 	1.0,	1.0},
																{0.0,	1.0,	0.5,	1.0,	1.0,	1.0},
																{0.0,	0.0,	0.0,	0.0,	0.0,	0.0}};*/
	
	static double[][] asymPrecisionEditRelaxationMatrix = new double[][]{{1.0,	0.0,	0.0,	0.0,	0.0,	1.0},
																	 	{0.0,	1.0,	1.0,	1.0,	1.0,	1.0},
																	 	{0.0,	0.0, 	1.0,	0.0,	0.0,	1.0},
																	 	{0.0,	1.0, 	0.75,	1.0, 	1.0,	1.0},
																		{0.0,	0.0,	0.5,	0.0,	1.0,	1.0},
																		{0.0,	0.0,	0.0,	0.0,	0.0,	0.0}};
	
	
	static SimpleMatrix C;
	SimpleMatrix symPRM = new SimpleMatrix(symmetricPrecisionRelaxationMatrix);
	static SimpleMatrix asymRRM = new SimpleMatrix(symmetricRecallRelaxationMatrix);
	static SimpleMatrix editPRM = new SimpleMatrix(asymPrecisionEditRelaxationMatrix);
	
	static final String measuresFile = alignmentFolder + "/test_alignment_measures.csv";
	static final String testAlignment = alignmentFolder + "/test_alignment.csv";
	
	static Vector<Double[]> eqv_evaluations = new Vector<Double[]>();
	static Vector<Double[]> hype_evaluations = new Vector<Double[]>();
	static Vector<Double[]> hyp_evaluations = new Vector<Double[]>();
	static Vector<Double[]> rel_evaluations = new Vector<Double[]>();
	static Vector<Double[]> dsj_evaluations = new Vector<Double[]>();
	
	static final double hyp_t_low_index = 0.0;
	static final double hyp_t_high_index = 1.0;
	static final double hyp_t_step = 0.1;
	
	static DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
	static NumberFormat form = new DecimalFormat("#0.00"); 
	
	
	public static void main(String[] args) 
	{
		try
		{
			for (double i = hyp_t_low_index; i <= hyp_t_high_index; i = i + hyp_t_step)
			{
				Aligner aligner = new Aligner(1.0, 1.1, 0.0+i, measuresFile, testAlignment);
				//Aligner aligner = new Aligner(1.0, 0.5, 0.4, measuresFile, testAlignment);
				aligner.startAligning();
				collectAllAlignments();
				
				//OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
				//model.read("./ontologies/2.owl");
				
				//System.out.println("Start semantically grounded aligning on 2.owl");
				//SemanticallyGroundedAligner sem_alig = new SemanticallyGroundedAligner(model, testAlignment);
				//sem_alig.inferAlphaConsequences();
				//sem_alig.getStats();
				//sem_alig.writeSemanticallyGrounded("./alignments/test_alignment.sem");
				//for (it.unina.thesisrepo.matchers.Alignment as:sem_alig.sem_gounded_alignments)
					//allAlignments.add(new Alignment(as.src, as.dst, null, as.result, null));
				
				CopyOfAlignerTestSetEvaluator testSetEvaluator = new CopyOfAlignerTestSetEvaluator();
				//testSetEvaluator.getStatisticsV2();
				//testSetEvaluator.visualizeConfusionMatrix();
				//testSetEvaluator.getStatisticsV2();
				
				SimpleMatrix accuracies = getAccuracies(C, null);
				SimpleMatrix recall = getRecall(C, null);
				
				eqv_evaluations.add(new Double[]{
					accuracies.get(0), 
					recall.get(0), 
					2*(accuracies.get(0)*recall.get(0))/(accuracies.get(0) + recall.get(0))
					});
				
				hype_evaluations.add(new Double[]{
						accuracies.get(1), 
						recall.get(1), 
						2*(accuracies.get(1)*recall.get(1))/(accuracies.get(1) + recall.get(1))
						});
				
				hyp_evaluations.add(new Double[]{
						accuracies.get(2), 
						recall.get(2), 
						2*(accuracies.get(2)*recall.get(2))/(accuracies.get(2) + recall.get(2))
						});
				
				rel_evaluations.add(new Double[]{
						accuracies.get(3), 
						recall.get(3), 
						2*(accuracies.get(3)*recall.get(3))/(accuracies.get(3) + recall.get(3))
						});
				
				dsj_evaluations.add(new Double[]{
						accuracies.get(4), 
						recall.get(4), 
						2*(accuracies.get(4)*recall.get(4))/(accuracies.get(4) + recall.get(4))
						});
				
				
				
				//testSetEvaluator.visualizeConfusionMatrix();
				//testSetEvaluator.getStatisticsV2();
				allAlignments = new HashSet<Alignment>();
			}
				
			//getStatisticsV3();
			getPrecisionRecallCoords();
			
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	public CopyOfAlignerTestSetEvaluator() throws IOException
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
							else if (ground.compareTo("hyper") == 0)
							{
								confusionMatrix[1][0]++;
							}
							else if (ground.compareTo("hypo")== 0)
							{
								confusionMatrix[2][0]++;
							}
							else if (ground.compareTo("rel")== 0)
							{
								confusionMatrix[3][0]++;
							}
							else // dsj case
							{
								confusionMatrix[4][0]++;
							}
						}
						else if (expAlignResult.compareTo("hyper") == 0)
						{
							if (ground.compareTo("eqv") == 0)
							{
								confusionMatrix[0][1]++; 
							}
							else if (ground.compareTo("hyper") == 0)
							{
								confusionMatrix[1][1]++;
							}
							else if (ground.compareTo("hypo")== 0)
							{
								confusionMatrix[2][1]++;
							}
							else if (ground.compareTo("rel")== 0)
							{
								confusionMatrix[3][1]++;
							}
							else // dsj case
							{
								confusionMatrix[4][1]++;
							}
						}
						else if (expAlignResult.compareTo("hypo") == 0)
						{
							if (ground.compareTo("eqv") == 0)
							{
								confusionMatrix[0][2]++; 
							}
							else if (ground.compareTo("hyper") == 0)
							{
								confusionMatrix[1][2]++;
							}
							else if (ground.compareTo("hypo")== 0)
							{
								confusionMatrix[2][2]++;
							}
							else if (ground.compareTo("rel")== 0)
							{
								confusionMatrix[3][2]++;
							}
							else // dsj case
							{
								confusionMatrix[4][2]++;
							}
						}
						else if (expAlignResult.compareTo("rel") == 0)
						{
							if (ground.compareTo("eqv") == 0)
							{
								confusionMatrix[0][3]++; 
							}
							else if (ground.compareTo("hyper") == 0)
							{
								confusionMatrix[1][3]++;
							}
							else if (ground.compareTo("hypo")== 0)
							{
								confusionMatrix[2][3]++;
							}
							else if (ground.compareTo("rel")== 0)
							{
								confusionMatrix[3][3]++;
							}
							else // dsj case
							{
								confusionMatrix[4][3]++;
							}
						}
						else if (expAlignResult.compareTo("dsj") == 0)
						{
							if (ground.compareTo("eqv") == 0)
							{
								confusionMatrix[0][4]++; 
							}
							else if (ground.compareTo("hyper") == 0)
							{
								confusionMatrix[1][4]++;
							}
							else if (ground.compareTo("hypo")== 0)
							{
								confusionMatrix[2][4]++;
							}
							else if (ground.compareTo("rel")== 0)
							{
								confusionMatrix[3][4]++;
							}
							else // dsj case
							{
								confusionMatrix[4][4]++;
							}
						}
						else // Unknown case
						{
							if (ground.compareTo("eqv") == 0)
							{
								confusionMatrix[0][5]++; 
							}
							else if (ground.compareTo("hyper") == 0)
							{
								confusionMatrix[1][5]++;
							}
							else if (ground.compareTo("hypo")== 0)
							{
								confusionMatrix[2][5]++;
							}
							else if (ground.compareTo("rel")== 0)
							{
								confusionMatrix[3][5]++;
							}
							else // dsj case
							{
								confusionMatrix[4][5]++;
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
			System.out.println("End reading alignment...");
			file_buffer.close();
	}
	
	void visualizeConfusionMatrix()
	{
		System.out.println("Confusion Matrix:\n");
		System.out.print("\t" + "eqv" +"\t" + "hyper" +"\t" + "hypo" + "\t" + "rel" + "\t" + "dsj" + "\t" + "unk");
		String[] headers = new String[]{"eqv", "hyper", "hypo", "rel", "dsj", "unk"};
		for (int i = 0 ; i < 6; i ++)
		{
			System.out.print("\n" + headers[i] + "\t");
			for (int j = 0; j < 6; j++)
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
		
		SimpleMatrix accuracies1 = getAccuracies(C, editPRM);
		SimpleMatrix recall1 = getRecall(C, editPRM);
		
		//SimpleMatrix rel_accuracies = getAccuracies(C, symPRM);
		//SimpleMatrix rel_edit_accuracies = getAccuracies(C,editPRM);
		//SimpleMatrix recall1 = getRecall(C, asymRRM);
		
		System.out.println("specific accuracy(recall): " );
		System.out.println("eqv" + "\t\t" + "hyper" + "\t\t"  + "hypo" + "\t\t" + "rel" + "\t\t" + "dsj" + "\t\t" + "tot acc.");
		System.out.println(
				String.format("%.3g", accuracies.get(0)) + "(" + String.format("%.3g", recall.get(0)) + ")"+ "\t" + 
				String.format("%.3g", accuracies.get(1)) + "(" + String.format("%.3g", recall.get(1)) + ")"+ "\t" + 
				String.format("%.3g", accuracies.get(2)) + "(" + String.format("%.3g", recall.get(2)) + ")"+ "\t" + 
				String.format("%.3g", accuracies.get(3)) + "(" + String.format("%.3g", recall.get(3)) + ")"+ "\t" +
				String.format("%.3g", accuracies.get(4)) + "(" + String.format("%.3g", recall.get(4)) + ")" + "\t" +
				String.format("%.3g", C.extractDiag().elementSum()/C.elementSum()));
		
		System.out.println(
				String.format("%.3g", accuracies1.get(0)) + "(" + String.format("%.3g", recall1.get(0)) + ")"+ "\t" + 
				String.format("%.3g", accuracies1.get(1)) + "(" + String.format("%.3g", recall1.get(1)) + ")"+ "\t" + 
				String.format("%.3g", accuracies1.get(2)) + "(" + String.format("%.3g", recall1.get(2)) + ")"+ "\t" + 
				String.format("%.3g", accuracies1.get(3)) + "(" + String.format("%.3g", recall1.get(3)) + ")"+ "\t" +
				String.format("%.3g", accuracies1.get(4)) + "(" + String.format("%.3g", recall1.get(4)) + ")" + "\t" +
				String.format("%.3g", C.extractDiag().elementSum()/C.elementSum()));
		
		
		
		
		
		
		
		
		
		
		//System.out.println(String.format("%.3g", rel_accuracies.get(0)) + "(" + String.format("%.3g", rel_recall.get(0)) + ")"+ "\t" + String.format("%.3g", rel_accuracies.get(1)) + "(" + String.format("%.3g", rel_recall.get(1)) + ")"+ "\t" + 
			//	String.format("%.3g", rel_accuracies.get(2)) + "(" + String.format("%.3g", rel_recall.get(2)) + ")"+ "\t" +  String.format("%.3g", rel_accuracies.get(3)) + "(" + String.format("%.3g", rel_recall.get(3)) + ")"+ "\t" + String.format("%.3g", C.transpose().mult(symPRM).extractDiag().elementSum()/C.elementSum()));
		//System.out.println(String.format("%.3g", rel_edit_accuracies.get(0)) + "\t" + String.format("%.3g", rel_edit_accuracies.get(1)) + "\t" + 
			//	String.format("%.3g", rel_edit_accuracies.get(2)) + "\t" +  String.format("%.3g", rel_edit_accuracies.get(3)) + "\t" + String.format("%.3g", C.transpose().mult(editPRM).extractDiag().elementSum()/C.elementSum()));
		
		//System.out.println("eqv\t" + String.format("%.3g", accuracies.get(0)) +  "\t" + String.format("%.3g", rel_accuracies.get(0)));
		//System.out.println("hypo\t" + String.format("%.3g", accuracies.get(1)) + "\t" + String.format("%.3g", rel_accuracies.get(1)));
		//System.out.println("rel\t" + String.format("%.3g", accuracies.get(2)) +  "\t" + String.format("%.3g", rel_accuracies.get(2)));
		//System.out.println("dsj\t" + String.format("%.3g", accuracies.get(3)) +  "\t" + String.format("%.3g", rel_accuracies.get(3)));
	}
	
	static void getPrecisionRecallCoords()
	{
		for (double i =  hyp_t_low_index; i <= hyp_t_high_index; i = i + hyp_t_step)
			System.out.print("" + String.format("%.3g", i) + "\t");
		
		System.out.println();
		
		for (int i = 0; i < hyp_evaluations.size(); i++)
		{
			System.out.print("(" + String.format("%.3g", hyp_evaluations.get(i)[1]).replace(',','.') + ","+
					String.format("%.3g", hyp_evaluations.get(i)[0]).replace(',','.')+")");
		}
		System.out.println();
		for (int i = 0; i < rel_evaluations.size(); i++)
		{
			System.out.print("(" + String.format("%.3g", rel_evaluations.get(i)[1]).replace(',','.') + ","+
					String.format("%.3g", rel_evaluations.get(i)[0]).replace(',','.')+")");
		}
		
		System.out.println();
		for (int i = 0; i < dsj_evaluations.size(); i++)
		{
			System.out.print("(" + String.format("%.3g", dsj_evaluations.get(i)[1]).replace(',','.') + ","+
					String.format("%.3g", dsj_evaluations.get(i)[0]).replace(',','.')+")");
		}
		
		System.out.println("\nMacro averge:");
		
		for (int i = 0; i < rel_evaluations.size(); i++)
		{
			
			double acc_average =  (rel_evaluations.get(i)[0] + dsj_evaluations.get(i)[0])/2;
			double rec_average =  (rel_evaluations.get(i)[1] + dsj_evaluations.get(i)[1])/2;
			
			System.out.print("(" + String.format("%.3g", acc_average).replace(',','.') + ","+
					String.format("%.3g", rec_average).replace(',','.')+")");
		}
	}
	
	
	static void getStatisticsV3()
	{
		System.out.println("Classes Accuracy, Recall and F-measure (no relax):");
		System.out.println("-------------------------------------------------");
		
		for (double i =  hyp_t_low_index; i <= hyp_t_high_index; i = i + hyp_t_step)
			System.out.print("" + String.format("%.3g", i) + "\t");
		System.out.println();
		for (int j = 0; j < 3; j++)
		{
			for (int i = 0; i < eqv_evaluations.size(); i++)
			{
				System.out.print(String.format("%.3g", eqv_evaluations.get(i)[j]) + "\t");
			}
			System.out.println();
		}
		
		System.out.println();
		for (int j = 0; j < 3; j++)
		{
			for (int i = 0; i < hype_evaluations.size(); i++)
			{
				System.out.print(String.format("%.3g", hype_evaluations.get(i)[j]) + "\t");
			}
			System.out.println();
		}
		
		System.out.println();
		for (int j = 0; j < 3; j++)
		{
			for (int i = 0; i < hyp_evaluations.size(); i++)
			{
				System.out.print(String.format("%.3g", hyp_evaluations.get(i)[j]) + "\t");
			}
			System.out.println();
		}
		
		System.out.println();
		for (int j = 0; j < 3; j++)
		{
			for (int i = 0; i < rel_evaluations.size(); i++)
			{
				System.out.print(String.format("%.3g", rel_evaluations.get(i)[j]) + "\t");
			}
			System.out.println();
		}
		
		System.out.println();
		for (int j = 0; j < 3; j++)
		{
			for (int i = 0; i < dsj_evaluations.size(); i++)
			{
				System.out.print(String.format("%.3g",dsj_evaluations.get(i)[j]) + "\t");
			}
			System.out.println();
		}
	}
	
	static SimpleMatrix getAccuracies(SimpleMatrix C, SimpleMatrix R)
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
	
	static SimpleMatrix getRecall(SimpleMatrix C, SimpleMatrix R)
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
}
