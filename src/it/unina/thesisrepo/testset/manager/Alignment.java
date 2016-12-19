package it.unina.thesisrepo.testset.manager;


public class Alignment
{
	
	String src;
	String dst;
	double measures[];
	String result;
	String ground;
	
	Alignment(String src, String dst, double[] measures, String result)
	{
		this.src = src;
		this.dst = dst;
		this.measures = measures;
		this.result = result;
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