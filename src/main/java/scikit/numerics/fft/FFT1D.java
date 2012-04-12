package scikit.numerics.fft;

import static java.lang.Math.PI;
import scikit.numerics.fft.managed.ComplexDoubleFFT;
import scikit.numerics.fft.managed.ComplexDoubleFFT_Mixed;
import scikit.numerics.fn.Function1D;

public class FFT1D {
	public interface MapFn {
		public void apply(double k, double re, double im);
	};
	
	int dim1;
	ComplexDoubleFFT fft;
	double[] scratch;
	double dx1;
	
	public FFT1D(int dim1) {
		this.dim1 = dim1;
		fft = new ComplexDoubleFFT_Mixed(dim1);
		scratch = new double[2*dim1];
		dx1 = 1;
	}
	
	public void setLength(double L1) {
		dx1 = L1/dim1;
	}
	
	public void transform(double[] src, MapFn fn) {
		double L1 = dim1*dx1;
		for (int i = dim1-1; i >= 0; i--) {
			scratch[2*i+0] = src[i]*dx1;
			scratch[2*i+1] = 0;
		}
		
		fft.transform(scratch);
		scratch = fft.toWraparoundOrder(scratch);
		
		for (int x = -dim1/2; x < dim1/2; x++) {
			int i = (x+dim1)%dim1;
			double k = 2*PI*x/L1;
			fn.apply(k, scratch[2*i+0], scratch[2*i+1]);
		}
	}
	
	public void convolve(double[] src, double[] dst, Function1D fn) {
		double L1 = dim1*dx1;
		for (int i = dim1-1; i >= 0; i--) {
			scratch[2*i+0] = src[i]*dx1;
			scratch[2*i+1] = 0;
		}
		
		fft.transform(scratch);
		scratch = fft.toWraparoundOrder(scratch);

		for (int x = -dim1/2; x < dim1/2; x++) {
			int i = (x+dim1)%dim1;
			double k = 2*PI*x/L1;
			double J = fn.eval(k);
			scratch[2*i+0] *= J;
			scratch[2*i+1] *= J;
		}
		
		fft.backtransform(scratch);
		for (int i = 0; i < dim1; i++) {
			dst[i] = scratch[2*i+0] / L1;
		}
	}
	
	public double[] getScratch() {
		return scratch;
	}
}
