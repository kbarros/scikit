package scikit.util;



public class DoubleArray {
	
	public static double[] clone(double src[]) {
		if (src == null) {
			return null;
		}
		else {
			double[] dst = new double[src.length];
			copy(src, dst);
			return dst;
		}
	}
	
	/**
	 * Copies contents of src array into dst array.
	 * @param src
	 * @param dst
	 */
	public static void copy(double src[], double dst[]) {
		if (src.length != dst.length)
			throw new IllegalArgumentException("Array lengths don't match.");
		for (int i = 0; i < src.length; i++)
			dst[i] = src[i];
	}
	
	public static double[] slice(double a[], int i1, int i2) {
		if (i1 >= i2)
			return new double[0];
		if (i1 < 0)
			i1 = 0;
		if (i2 > a.length)
			i2 = a.length;
		double ret[] = new double[i2-i1];
		for (int j = 0; j < ret.length; j++) {
			ret[j] = a[i1 + j]; 
		}
		return ret;
	}
	
	public static double min(double a[]) {
		double min = a[0];
		for (double v : a)
			if (v < min) min = v;
		return min;
	}
	
	public static double max(double a[]) {
		double max = a[0];
		for (double v : a)
			if (v > max) max = v;
		return max;
	}
	
	public static void set(double a[], double v) {
		for (int i = 0; i < a.length; i++)
			a[i] = v;
	}
	
	public static void zero(double a[]) {
		set(a, 0);
	}
	
	public static void shift(double a[], double b) {
		for (int i = 0; i < a.length; i++)
			a[i] += b;
	}
	
	public static void scale(double a[], double b) {
		for (int i = 0; i < a.length; i++)
			a[i] *= b;		
	}
	
	public static double sum(double a[]) {
		double sum = 0;
		for (int i = 0; i < a.length; i++)
			sum += a[i];
		return sum;
	}
	
	public static double mean(double a[]) {
		return sum(a)/a.length;
	}
	
	public static double meanSquared(double a[]) {
		return dot(a,a)/a.length;
	}
	
	public static double variance(double a[]) {
		// "compensated" algorithm; reduces numerical error. taken from
		// Wikipedia: Algorithms_for_calculating_variance
		int n = a.length;
		double m = mean(a);
		
		double sum2 = 0;
		double sumc = 0;
		for (int i = 0; i < a.length; i++) {
		    sum2 += (a[i] - m)*(a[i] - m);
		    sumc += (a[i] - m);
		}
		return (sum2 - sumc*sumc/n)/(n - 1);
	}
	
	public static double standardError(double a[], int bins) {
		int n = a.length;
		
		double cg[] = new double[bins];
		for (int b = 0; b < bins; b++) {
			int cnt = 0;
			for (int j = b*n/bins; j < (b+1)*n/bins; j++) {
				cg[b] += a[j];
				cnt++;
			}
			cg[b] /= cnt;
		}
		
		return Math.sqrt(variance(cg) / bins);
	}

	public static double standardError(double a[]) {
		return standardError(a, a.length);
	}

	public static double norm(double a[]) {
		return Math.sqrt(dot(a, a));
	}
	
	public static double dot(double a[], double b[]) {
		if (a.length != b.length)
			throw new IllegalArgumentException("Array lengths don't match.");
		double sum = 0;
		for (int i = 0; i < a.length; i++)
			sum += a[i]*b[i];
		return sum;
	}
	
	public static void normalize(double a[]) {
		scale(a, 1/norm(a));
	}
	
    /**
     * Assigns (dst[i] = src1[i] + src2[i]) for each index i
     * @param src1
     * @param src2
     * @param dst
     */
    public static final void add(double[] src1, double[] src2, double[] dst) {
    	for (int i = 0; i < dst.length; i++)
    		dst[i] = src1[i] + src2[i];
    }

    /**
     * Assigns (dst[i] = src1[i] - src2[i]) for each index i
     * @param src1
     * @param src2
     * @param dst
     */
    public static final void sub(double[] src1, double[] src2, double[] dst) {
    	for (int i = 0; i < dst.length; i++)
    		dst[i] = src1[i] - src2[i];
    }
}
