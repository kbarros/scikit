package scikit.numerics.fft.managed;
/** Computes the FFT of 3 dimensional complex, double precision data.
 * The data is stored in a 1-dimensional array in generalized "Row-Major" order.
 * The physical layout in the array data, of the mathematical data d[i,j,k] is as follows:
 *<PRE>
 *    Re(d[i1,i2,i3]) = data[2*(i1*dim3*dim2 + i2*dim3 + i3)]
 *    Im(d[i1,i2,i3]) = data[2*(i1*dim3*dim2 + i2*dim3 + i3)+1]
 *</PRE>
 * The transformed data is returned in the original data array in 
 * <a href="package-summary.html#wraparound">wrap-around</A> order along each dimension.
 *
 * @author Kipton Barros
 * @author not subject to copyright.
 */ 
public class ComplexDouble3DFFT {
	int dim1, dim2, dim3;
	ComplexDoubleFFT dim1FFT, dim2FFT, dim3FFT;

	/** Create an FFT for transforming nrows*ncols points of Complex, double precision
	 * data. */
	public ComplexDouble3DFFT(int dim1, int dim2, int dim3) {
		this.dim1 = dim1;
		this.dim2 = dim2;
		this.dim3 = dim3;
		dim1FFT = new ComplexDoubleFFT_Mixed(dim1);
		dim2FFT = (dim1 == dim2 ? dim1FFT : new ComplexDoubleFFT_Mixed(dim2));
		dim3FFT = (dim2 == dim3 ? dim2FFT : new ComplexDoubleFFT_Mixed(dim3));
	}

	protected void checkData(double data[]){
		if (2*dim1*dim2*dim3 > data.length)
			throw new IllegalArgumentException("The data array is too small for "+
					dim1+"x"+dim2+"x"+dim3+", data.length="+data.length);
	}

	/** Compute the Fast Fourier Transform of data leaving the result in data.
	 * The array data must be dimensioned 2*dim1*dim2*dim3, consisting of
	 * alternating real and imaginary parts. */
	public void transform(double data[]) {
		checkData(data);
		for (int i1 = 0; i1 < dim1; i1++)
			for (int i2 = 0; i2 < dim2; i2++)
				dim3FFT.transform(data, 2*(i1*dim3*dim2+i2*dim3), 2);
		for (int i1 = 0; i1 < dim1; i1++)
			for (int i3 = 0; i3 < dim3; i3++)
				dim2FFT.transform(data, 2*(i1*dim3*dim2+i3), 2*dim3);
		for (int i2 = 0; i2 < dim2; i2++)
			for (int i3 = 0; i3 < dim3; i3++)
				dim1FFT.transform(data, 2*(i2*dim3+i3), 2*dim3*dim2);
	}

	/** Return data in wraparound order.
	 * @see <a href="package-summary.html#wraparound">wraparound format</A> */
	public double[] toWraparoundOrder(double data[]) {
		return data;
	}

	/** Compute the (unnormalized) inverse FFT of data, leaving it in place.*/
	public void backtransform(double data[]) {
		checkData(data);
		for (int i2 = 0; i2 < dim2; i2++)
			for (int i3 = 0; i3 < dim3; i3++)
				dim1FFT.backtransform(data, 2*(i2*dim3+i3), 2*dim3*dim2);
		for (int i1 = 0; i1 < dim1; i1++)
			for (int i3 = 0; i3 < dim3; i3++)
				dim2FFT.backtransform(data, 2*(i1*dim3*dim2+i3), 2*dim3);
		for (int i1 = 0; i1 < dim1; i1++)
			for (int i2 = 0; i2 < dim2; i2++)
				dim3FFT.backtransform(data, 2*(i1*dim3*dim2+i2*dim3), 2);
	}

	/** Return the normalization factor.  
	 * Multiply the elements of the backtransform'ed data to get the normalized inverse.*/
	public double normalization(){
		return 1.0 / (dim1*dim2*dim3);
	}

	/** Compute the (normalized) inverse FFT of data, leaving it in place.*/
	public void inverse(double data[]) {
		backtransform(data); 
		double norm = normalization();
		for(int i = 0; i < 2*dim1*dim2*dim3; i++) {
			data[i] *= norm; 
		}
	}
}
