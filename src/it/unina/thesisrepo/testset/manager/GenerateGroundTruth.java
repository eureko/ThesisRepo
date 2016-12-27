package it.unina.thesisrepo.testset.manager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class GenerateGroundTruth 
{
	static TreeSet<String> current_set = new TreeSet<String>();
	static TreeSet<String> target_terms_set = new TreeSet<String>();
	
	static final String src_file = "./ontologies/11.csv";
	static final String target_file = "./ontologies/target.csv";
	
	static JPanel mainPanel;
	
	static JList<String> src_list;
	static JList<String> target_list;
	
	static JPanel westPanel = new JPanel();
	static JPanel eastPanel = new JPanel();
	
	static JFrame frame;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
		      public void run() {
		        createGUI();
		      }
		    });
		
	}
	
	static void createTargetSet(String file) throws IOException
	{
		BufferedReader file_buffer = new BufferedReader(new FileReader(file));
	    
		String line;
		line = file_buffer.readLine(); // Read comment line
		
		while((line = file_buffer.readLine()) != null)
		{
			if (line.contains("|"))
			{
				String[] pair = line.split("\\|");
				target_terms_set.add(pair[0]);
			}
			else
			{
				line.trim();
				target_terms_set.add(line.trim());
			}
		}
		file_buffer.close();
	}
	
	static void addSet(String file, TreeSet<String> set) throws IOException
	{
		BufferedReader file_buffer = new BufferedReader(new FileReader(file));
	    
		String line;
		line = file_buffer.readLine(); // Read comment line
		
		while((line = file_buffer.readLine()) != null)
		{
			if (line.contains("|"))
			{
				String[] pair = line.split("\\|");
				set.add(pair[0]);
			}
			else
			{
				line.trim();
				set.add(line.trim());
			}
		}
		file_buffer.close();
	}
	
	protected static void createGUI() 
	{
		
		frame = new JFrame();
		frame.setMinimumSize(new Dimension(640, 820));
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		frame.setVisible(true);
		
		
		mainPanel = new JPanel();
		
		mainPanel.setLayout(new BorderLayout());
		
		JPanel northPAnel = new JPanel();
		
		JButton loadButton = new JButton("Load");
		
		loadButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				try
				{
					System.out.println("Handling file: " + src_file);
					addSet(src_file, current_set);
					
					System.out.println("Handling file: " + target_file);
					createTargetSet(target_file);
					
					System.out.println("OK!");
					
					System.out.println(current_set.size());
					System.out.println(target_terms_set.size());
					
					src_list = new JList(current_set.toArray());
					target_list = new JList(target_terms_set.toArray());
					
					
					
					//src_list.setPreferredSize(new Dimension(250, 800));
					//target_list.setPreferredSize(new Dimension(250, 800));
					src_list.repaint();
					target_list.repaint();
					westPanel.add(src_list);
					eastPanel.add(target_list);
					
					frame.repaint();
					frame.revalidate();
					
					
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
		
		
		JButton eqvButton = new JButton("EQV");
		eqvButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				
				int[] selected_src =  src_list.getSelectedIndices();
				int[] selected_target = target_list.getSelectedIndices();
				
				for (int i:selected_src)
					for (int j:selected_target)
						System.out.println(src_list.getModel().getElementAt(i) + ";" + target_list.getModel().getElementAt(j) + ";" + "eqv");
			}
		});
		JButton hypoButton = new JButton("HYPO");
		hypoButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				
				int[] selected_src =  src_list.getSelectedIndices();
				int[] selected_target = target_list.getSelectedIndices();
				
				for (int i:selected_src)
					for (int j:selected_target)
						System.out.println(src_list.getModel().getElementAt(i) + ";" + target_list.getModel().getElementAt(j) + ";" + "hypo");
			}
		});
		
		JButton hyperButton = new JButton("HYPER");
		hyperButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				
				int[] selected_src =  src_list.getSelectedIndices();
				int[] selected_target = target_list.getSelectedIndices();
				
				for (int i:selected_src)
					for (int j:selected_target)
						System.out.println(src_list.getModel().getElementAt(i) + ";" + target_list.getModel().getElementAt(j) + ";" + "hyper");
			}
		});
		
		JButton relatedButton = new JButton("RELATED");
		relatedButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				
				int[] selected_src =  src_list.getSelectedIndices();
				int[] selected_target = target_list.getSelectedIndices();
				
				for (int i:selected_src)
					for (int j:selected_target)
						System.out.println(src_list.getModel().getElementAt(i) + ";" + target_list.getModel().getElementAt(j) + ";" + "rel");
			}
		});
		JButton dsjButton = new JButton("DSJ");
		dsjButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				
				int[] selected_src =  src_list.getSelectedIndices();
				int[] selected_target = target_list.getSelectedIndices();
				
				for (int i:selected_src)
					for (int j:selected_target)
						System.out.println(src_list.getModel().getElementAt(i) + ";" + target_list.getModel().getElementAt(j) + ";" + "dsj");
			}
		});
		
		
		northPAnel.add(loadButton);
		northPAnel.add(eqvButton);
		northPAnel.add(hypoButton);
		northPAnel.add(hyperButton);
		northPAnel.add(relatedButton);
		northPAnel.add(dsjButton);
		
		JScrollPane westScrollPanel = new JScrollPane();
		JScrollPane eastScrollPanel = new JScrollPane();
		
		westPanel.setSize(400, 1200);
		eastPanel.setSize(400, 1200);
		
		westScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		westScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		eastScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		eastScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		eastScrollPanel.setViewportView(eastPanel);
		westScrollPanel.setViewportView(westPanel);
		
		eastScrollPanel.setPreferredSize(new Dimension(250, 80));
		westScrollPanel.setPreferredSize(new Dimension(250, 80));
		
		
		
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		
		centerPanel.add(westScrollPanel, BorderLayout.WEST);
		centerPanel.add(eastScrollPanel, BorderLayout.EAST);
		
		
		
		mainPanel.add(northPAnel, BorderLayout.NORTH);
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		
		frame.setContentPane(mainPanel);
		
	}
}
