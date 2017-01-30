package it.unina.thesisrepo.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class ReadExample 
{
	public static void main(String[] args) 
	{
		try
		{
			NumberFormat nf_out = NumberFormat.getNumberInstance(Locale.UK);
			nf_out.setMaximumFractionDigits(2);
			nf_out.setMinimumFractionDigits(1);
			
			BufferedReader file_buffer = new BufferedReader(new FileReader("example.csv"));
			String line = "";
			
			while((line = file_buffer.readLine()) != null)
			{
				String[] tokens = line.split("\t");
				
				System.out.print(tokens[0] + " & " +tokens[1] + " & ");
				
				for (int i = 2; i <= 10; i++)
				{
					if (i < 10)
						System.out.print(nf_out.format(Double.parseDouble(tokens[i])) + " & ");
					else
						System.out.print(nf_out.format(Double.parseDouble(tokens[i])) + "\\\\");
				}
				
				System.out.print("\n");
			}
			file_buffer.close();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
}
