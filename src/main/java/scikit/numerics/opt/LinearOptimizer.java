package scikit.numerics.opt;

import static java.lang.Math.*;
import scikit.numerics.fn.C1Function1D;
import scikit.util.Pair;

public class LinearOptimizer {
	protected C1Function1D _f;
	static final double GOLDEN = (1+sqrt(5))/2; // = 1.618...
	static final double TOL = 3e-8; // sqrt of double precision, see N.R. discussion
	static final double EPS = 1e-12;
	
	public void setFunction(C1Function1D f) {
		_f = f;
	}
	
	public Pair<Double,Double> optimize(double x) {
		double x1 = x;
		double f1 = _f.eval(x1);
		double alpha = - _f.deriv(x);
		double x2 = x1 + alpha;
		double f2 = _f.eval(x2);
		boolean tooClose = (2*abs(f2-f1) < TOL*(abs(f2)+abs(f1)+EPS));
		double factor = tooClose ? 10 : 0.1;
		for (int i = 0; i < 10; i++) {
			if (f1 > f2)
				return optimize(bracket(x1, f1, x2, f2));
			alpha *= factor;
			x2 = x1 + alpha;
			f2 = _f.eval(x2);
		}
		throw new IllegalStateException("Invalid derivative information.");
	}
	
	public Pair<Double,Double> optimize(double[] bracket) {
		double x0 = bracket[0];
		double xm = bracket[1];
		double x3 = bracket[2];
		double x1, x2;
		if (abs(xm-x0) < abs(xm-x3)) {
			x1 = xm;
			x2 = (xm-x3)/GOLDEN + x3;
		}
		else {
			x1 = (xm-x0)/GOLDEN + x0; 
			x2 = xm;
		}
		double f1 = _f.eval(x1);
		double f2 = _f.eval(x2);
		while(abs(x3-x0) > TOL*(abs(x1)+abs(x2))) {
			if (f2 < f1) {
				x0 = x1;
				x1 = x2;
				x2 = (x2-x3)/GOLDEN + x3;
				f1 = f2;
				f2 = _f.eval(x2);
			}
			else {
				x3 = x2;
				x2 = x1;
				x1 = (x1-x0)/GOLDEN + x0;
				f2 = f1;
				f1 = _f.eval(x1);
			}
		}
		return f1 < f2 ? new Pair<Double,Double>(x1,f1) : new Pair<Double,Double>(x2,f2);
	}
	
	protected double[] bracket(double x1, double f1, double x2, double f2) {
		assert (f1 > f2);
		while (abs(x2-x1) < 1e10) {
			double x3 = x2 + GOLDEN*(x2-x1);
			double f3 = _f.eval(x3);
			if (f2 < f3) {
				return new double[] {min(x1, x3), x2, max(x1, x3)};
			}
			else {
				assert(f3 < f2);
				x1 = x2;
				f1 = f2;
				x2 = x3;
				f2 = f3;
			}
		}
		throw new IllegalStateException("Could not bracket a minimum.");
	}
}
