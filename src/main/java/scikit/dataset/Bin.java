package scikit.dataset;

import static java.lang.Math.sqrt;

public class Bin {
	private double sum = 0;
	private double sum2 = 0;
	private int count = 0;
	
	public void accum(Bin that) {
		sum += that.sum;
		sum2 += that.sum2;
		count += that.count;
	}
	
	public void accum(double value) {
		sum += value;
		sum2 += value*value;
		count += 1;
	}
	
	public double sum() {
		return sum;
	}

	public int count() {
		return count;
	}
	
	public double average() {
		return sum / count;
	}
	
	public double variance() {
		double s1 = sum / count;
		double s2 = sum2 / count;
		return s2 - s1*s1;
	}
	
	// return the standard error correspond to the average() value.
	// uses the central limit theorem and assumes that all accumulated variables 
	// are independent and identically distributed (IID).
	public double error() {
		return sqrt(variance() / count);
	}
}
