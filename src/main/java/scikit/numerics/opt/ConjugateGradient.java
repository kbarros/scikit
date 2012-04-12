package scikit.numerics.opt;

import static scikit.util.DoubleArray.dot;
import static java.lang.Math.sqrt;
import scikit.numerics.fn.C1FunctionND;
import scikit.numerics.fn.C1Function1D;
import scikit.util.Pair;


public class ConjugateGradient extends Optimizer<C1FunctionND> {
	double[] temp;
	double[] g, h;
	double fp;
	LinearOptimizer linOpt;

	public ConjugateGradient(int dim, LinearOptimizer linOpt) {
		super(dim);
		this.linOpt = linOpt;
		temp = new double[_dim];
		g = new double[_dim];
		h = new double[_dim];
	}
	
	public void initialize(double[] p) {
		super.initialize(p);
		fp = _f.eval(p);
		double[] d_f = _f.grad(p);
		for (int i = 0; i < _dim; i++) {
			g[i] = h[i] = -d_f[i];
		}
	}
	
	public void step() {
		// double fp2 = linmin(p, g); // uncomment for steepest descent
		double fp2 = linmin(p, h);
		if (objectiveConverged(fp, fp2)) {
			_finished = true;
			return;
		}
		fp = fp2;
		double[] d_f = _f.grad(p);
		double gg = dot(g, g);
		if (gg == 0) {
			_finished = true;
			return;
		}
		double gamma = 0;
		for (int i = 0; i < _dim; i++)
			gamma += (-d_f[i]-g[i])*(-d_f[i]);
		gamma /= gg;
		for (int i = 0; i < _dim; i++) {
			g[i] = -d_f[i];
			h[i] = -d_f[i] + gamma*h[i];
		}
	}
	
	// given a n-dimensional point p and a direction dir, moves and resets p
	// to where the function fn is takes a minimum along the direction dir from
	// p. returns the value of fn at the returned location p.
	double linmin(final double[] p, final double[] dir) {
		C1Function1D f_lin = new C1Function1D() {
			public Pair<Double,Double> calculate(final double x) {
				for (int i = 0; i < _dim; i++)
					temp[i] = p[i] + x*dir[i];
				Pair<Double,double[]> r = _f.calculate(temp);
				double df = dot(r.snd(),dir)/sqrt(dot(dir,dir));
				return new Pair<Double,Double>(r.fst(), df);
			}
		};
		linOpt.setFunction(f_lin);
		Pair<Double,Double> r = linOpt.optimize(0);
		for (int i = 0; i < _dim; i++)
			p[i] += r.fst()*dir[i];
		return r.snd();
	}
}
