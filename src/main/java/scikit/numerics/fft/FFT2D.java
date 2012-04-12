package scikit.numerics.fft;

import static java.lang.Math.PI;
import scikit.numerics.fft.managed.ComplexDouble2DFFT;
import scikit.numerics.fn.Function2D;

// TODO: copy interface from FFT3D
public class FFT2D {
	public interface MapFn {
		public void apply(double k1, double k2, double re, double im);
	};
	
	public int dim1, dim2;
	ComplexDouble2DFFT fft;
	double[] scratch;
	double dx1, dx2;
	
	public FFT2D(int dim1, int dim2) {
		this.dim1 = dim1;
		this.dim2 = dim2;
		fft = new ComplexDouble2DFFT(dim1, dim2);
		scratch = new double[2*dim1*dim2];
		dx1 = dx2 = 1;
	}
	
	public void setLengths(double L1, double L2) {
		dx1 = L1/dim1;
		dx2 = L2/dim2;
	}
	
	public void transform(double[] src, double[] dst) {
		for (int i = dim1*dim2-1; i >= 0; i--) {
			dst[2*i+0] = src[i]*dx1*dx2;
			dst[2*i+1] = 0;
		}
		fft.transform(dst);
		fft.toWraparoundOrder(dst);
	}
	
	public void backtransform(double[] src, double[] dst) {
		double L1 = dim1*dx1;
		double L2 = dim2*dx2;
		fft.backtransform(src);
		for (int i = 0; i < dim1*dim2; i++) {
			dst[i] = src[2*i+0] / (L1*L2);
		}
	}
	
	public void transform(double[] phi, MapFn fn) {
		// TODO: replace following with
		// transform(phi, scratch);
		for (int i = dim1*dim2-1; i >= 0; i--) {
			scratch[2*i+0] = phi[i]*dx1*dx2;
			scratch[2*i+1] = 0;
		}
		fft.transform(scratch);
		scratch = fft.toWraparoundOrder(scratch);
		
		double L1 = dim1*dx1;
		double L2 = dim2*dx2;
		for (int x2 = -dim2/2; x2 < dim2/2; x2++) {
			for (int x1 = -dim1/2; x1 < dim1/2; x1++) {
				int i = dim1*((x2+dim2)%dim2) + (x1+dim1)%dim1;
				double k1 = 2*PI*x1/L1;
				double k2 = 2*PI*x2/L2;
				fn.apply(k1, k2, scratch[2*i+0], scratch[2*i+1]);
			}
		}
	}
	
	// It is OK if src and dst are the same array.
	public void convolve(double[] src, double[] dst, Function2D fn) {
		// TODO: replace following with
		// transform(phi, scratch);
		for (int i = dim1*dim2-1; i >= 0; i--) {
			scratch[2*i+0] = src[i]*dx1*dx2;
			scratch[2*i+1] = 0;
		}
		fft.transform(scratch);
		scratch = fft.toWraparoundOrder(scratch);

		double L1 = dim1*dx1;
		double L2 = dim2*dx2;
		for (int x2 = -dim2/2; x2 < dim2/2; x2++) {
			for (int x1 = -dim1/2; x1 < dim1/2; x1++) {
				int i = dim1*((x2+dim2)%dim2) + (x1+dim1)%dim1;
				double k1 = 2*PI*x1/L1;
				double k2 = 2*PI*x2/L2;
				double J = fn.eval(k1, k2);
				scratch[2*i+0] *= J;
				scratch[2*i+1] *= J;
			}
		}
		
		// TODO: replace following with
		// backtransform(scratch, dst);
		fft.backtransform(scratch);
		for (int i = 0; i < dim1*dim2; i++) {
			dst[i] = scratch[2*i+0] / (L1*L2);
		}
	}
	
	public void convolve(double[] src, double[] dst, double[] fn) {
		double L1 = dim1*dx1;
		double L2 = dim2*dx2;
		for (int i = dim1*dim2-1; i >= 0; i--) {
			scratch[2*i+0] = src[i]*dx1*dx2;
			scratch[2*i+1] = 0;
		}
		
		fft.transform(scratch);
		scratch = fft.toWraparoundOrder(scratch);
		
		for (int i = 0; i < dim1*dim2; i++) {
			scratch[2*i+0] *= fn[i];
			scratch[2*i+1] *= fn[i];
		}
		
		fft.backtransform(scratch);
		for (int i = 0; i < dim1*dim2; i++) {
			dst[i] = scratch[2*i+0] / (L1*L2);
		}
	}
	
	
	public double[] buildFourierArray(Function2D fn) {
		double[] ret = new double[dim1*dim2];
		double L1 = dim1*dx1;
		double L2 = dim2*dx2;
		for (int x2 = -dim2/2; x2 < dim2/2; x2++) {
			for (int x1 = -dim1/2; x1 < dim1/2; x1++) {
				int i = dim1*((x2+dim2)%dim2) + (x1+dim1)%dim1;
				double k1 = 2*PI*x1/L1;
				double k2 = 2*PI*x2/L2;
				ret[i] = fn.eval(k1, k2);
			}
		}
		return ret;
	}
	
	public void buildFourierArray(double[] ret, Function2D fn) {
		if (ret.length != dim1*dim2) {
			throw new IllegalArgumentException("Bad param array");
		}
		double L1 = dim1*dx1;
		double L2 = dim2*dx2;
		for (int x2 = -dim2/2; x2 < dim2/2; x2++) {
			for (int x1 = -dim1/2; x1 < dim1/2; x1++) {
				int i = dim1*((x2+dim2)%dim2) + (x1+dim1)%dim1;
				double k1 = 2*PI*x1/L1;
				double k2 = 2*PI*x2/L2;
				ret[i] = fn.eval(k1, k2);
			}
		}
	}

	public void buildFourierArrayFromReal(double[] ret, Function2D fn) {
		if (ret.length != dim1*dim2) {
			throw new IllegalArgumentException("Bad param array");
		}
		for (int x2 = -dim2/2; x2 < dim2/2; x2++) {
			for (int x1 = -dim1/2; x1 < dim1/2; x1++) {
				int i = dim1*((x2+dim2)%dim2) + (x1+dim1)%dim1;
				ret[i] = fn.eval(dx1*x1, dx2*x2);
			}
		}
		transform(ret, scratch);
		for (int i=0; i < dim1*dim2; i++) {
			ret[i] = scratch[2*i+0];
		}
	}

	public double[] getScratch() {
		return scratch;
	}
}
