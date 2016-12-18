package it.unina.thesisrepo.ontoadapters;

import java.io.BufferedReader;
import java.io.FileReader;

public class EurocodeTaxonomyHandler 
{
	public static void main(String[] args) 
	{
		try
		{
			BufferedReader file_buffer = new BufferedReader(new FileReader("./ontologies/src/Eurocode.txt"));
		    
			String line;
			line = file_buffer.readLine(); // Read comment line
			
			while((line = file_buffer.readLine()) != null)
			{
				String[] splited = line.split("\\s+");
				System.out.println(line.substring(splited[0].length()).trim());
				//System.out.println(splited[0]);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
