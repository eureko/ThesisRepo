package it.unina.thesisrepo.matchers;

import edu.smu.tspell.wordnet.Synset;

public class SynsetUtility 
{
		
	static public int calculateSynonimy(Synset[] synsets1, Synset[] synsets2)
	{
		int result = 0;
		int length1 = synsets1.length;
		int length2 = synsets2.length;
		
		if (length1 <= length2)
		{
			for (int i = 0; i < length1 && result == 0; i++)
			{
				for (int j = 0; j < length2 && result == 0; j++)
				{
					if (synsets1[i].toString().compareToIgnoreCase(synsets2[j].toString()) == 0)
						result = 1;
				}
			}
		}
		else
		{
			for (int i = 0; i < length2 && result == 0; i++)
			{
				for (int j = 0; j < length1; j++)
				{
					if (synsets2[i].toString().compareToIgnoreCase(synsets1[j].toString()) == 0)
						result = 1;
				}
			}
		}
		return result;
			
	}
	
	static public float calculateCosynonimy(Synset[] synsets1, Synset[] synsets2)
	{
		int sum = 0;
		float res;
		int length1 = synsets1.length;
		int length2 = synsets2.length;
		
		if (length1 <= length2)
		{
			for (int i = 0; i < length1; i++)
			{
				for (int j = 0; j < length2; j++)
				{
					if (synsets1[i].getDefinition().compareToIgnoreCase(synsets2[j].getDefinition()) == 0)
						sum++;
				}
			}
		}
		else
		{
			for (int i = 0; i < length2; i++)
			{
				for (int j = 0; j < length1; j++)
				{
					if (synsets2[i].getDefinition().compareToIgnoreCase(synsets1[j].getDefinition()) == 0)
						sum++;
				}
			}
		}
		
		return  (float)sum/(float)(length1 - sum + length2);
	}
	
	
	
	/*static public String getType(Synset synset)
	{
		
		if (synset.getType() == 1)
			return "Noun";
		else
			return "";
	}*/
}
