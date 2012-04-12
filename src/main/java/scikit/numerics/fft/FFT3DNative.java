package scikit.numerics.fft;

import static java.lang.Math.PI;
import jfftw.real.nd.Plan;
import scikit.numerics.fn.Function3D;


public class FFT3DNative extends FFT3D {
	static int flags = Plan.ESTIMATE;
//	static int flags = Plan.MEASURE;
	
	Plan fftForward, fftBackward;
	
	public FFT3DNative(int dim1, int dim2, int dim3) {
		this.dim1 = dim1;
		this.dim2 = dim2;
		this.dim3 = dim3;
		fftForward = new Plan(new int[]{dim3, dim2, dim1}, Plan.REAL_TO_COMPLEX, flags);
		fftBackward = new Plan(new int[]{dim3, dim2, dim1}, Plan.COMPLEX_TO_REAL, flags);
		scratch = new double[2*dim3*dim2*(dim1/2+1)];
		dx1 = dx2 = dx3 = 1;
	}
	

	public void transform(double[] src, MapFn fn) {
		fftForward.transform(1, src, 1, 0, scratch, 1, 0);
		
		double L1 = dim1*dx1;
		double L2 = dim2*dx2;
		double L3 = dim3*dx3;
		double scale = dx1*dx2*dx3; 
			
		int dim1p = dim1/2 + 1;
		for (int x3 = -dim3/2; x3 < dim3/2; x3++) {
			for (int x2 = -dim2/2; x2 < dim2/2; x2++) {
				for (int x1 = 0; x1 <= dim1/2; x1++) {
					int i = dim1p*dim2*((x3+dim3)%dim3) + dim1p*((x2+dim2)%dim2) + x1;
					double k1 = 2*PI*x1/L1;
					double k2 = 2*PI*x2/L2;
					double k3 = 2*PI*x3/L3;
					fn.apply(k1, k2, k3, scale*scratch[2*i+0], scale*scratch[2*i+1]);
				}
			}
		}
	}
	
	public void convolve(double[] src, double[] dst, Function3D fn) {
		double L1 = dim1*dx1;
		double L2 = dim2*dx2;
		double L3 = dim3*dx3;
		double scale = 1.0/(dim1*dim2*dim3);
		
		fftForward.transform(1, src, 1, 0, scratch, 1, 0);
		
		int dim1p = dim1/2 + 1;
		for (int x3 = -dim3/2; x3 < dim3/2; x3++) {
			for (int x2 = -dim2/2; x2 < dim2/2; x2++) {
				for (int x1 = 0; x1 <= dim1/2; x1++) {
					int i = dim1p*dim2*((x3+dim3)%dim3) + dim1p*((x2+dim2)%dim2) + x1;
					double k1 = 2*PI*x1/L1;
					double k2 = 2*PI*x2/L2;
					double k3 = 2*PI*x3/L3;
					double J = scale*fn.eval(k1, k2, k3);
					scratch[2*i+0] *= J;
					scratch[2*i+1] *= J;
				}
			}
		}
		
		fftBackward.transform(1, scratch, 1, 0, dst, 1, 0);		
	}
}
