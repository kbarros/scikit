package scikit.numerics.fft;

import java.lang.reflect.InvocationTargetException;

import scikit.numerics.fn.Function3D;

public abstract class FFT3D {
	public interface MapFn {
		public void apply(double k1, double k2, double k3, double re, double im);
	};
	
	public int dim1, dim2, dim3;
	protected double[] scratch;
	protected double dx1, dx2, dx3;
	
	abstract public void transform(double[] src, MapFn fn);
	abstract public void convolve(double[] src, double[] dst, Function3D fn);
	
	public static FFT3D create(int dim1, int dim2, int dim3) {
		try {
			Class<?> c = Class.forName("scikit.numerics.fft.FFT3DNative");
			return (FFT3D)c.getConstructor(int.class, int.class, int.class).newInstance(dim1, dim2, dim3);
		}
		catch (InvocationTargetException e) {
			System.out.println(e.getCause());
		}
		catch (Exception e) {
			System.out.println(e);
		}
		return new FFT3DManaged(dim1, dim2, dim3);
	}
	
	public void setLengths(double L1, double L2, double L3) {
		dx1 = L1/dim1;
		dx2 = L2/dim2;
		dx3 = L3/dim3;
	}
}
