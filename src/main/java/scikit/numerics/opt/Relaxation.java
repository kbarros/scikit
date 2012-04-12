package scikit.numerics.opt;

import static scikit.util.DoubleArray.dot;

import java.util.ArrayList;

import scikit.numerics.Jama.Matrix;
import scikit.numerics.fn.C1FunctionND;
import scikit.util.Pair;

public class Relaxation extends Optimizer<C1FunctionND> {
	double dt;
	
	public Relaxation(int dim, double dt) {
		super(dim);
		this.dt = dt;
	}
	
	public void initialize(double[] p) {
		super.initialize(p);
	}

	public void step() {
		double[] d_f = df_constrained(p);
		for (int i = 0; i < _dim; i++) {
			p[i] += - dt * d_f[i];
		}
	}
	
	public void setStepSize(double dt) {
		this.dt = dt;
	}
	
	/**
	 *  Returns the gradient of the objective function after applying the constraints
	 *  through appropriate Lagrange multipliers.
	 *  @return the constrained gradient of the objective function
	 */
	protected double[] df_constrained(double[] p) {
		double[] d_f = _f.grad(p);
		ArrayList<Constraint> cs = _constraints;		
		if (cs.size() > 0) {
			double[][] d_c = new double[cs.size()][];
			double[] c = new double[cs.size()];
			for (int i = 0; i < cs.size(); i++) {
				Pair<Double, double[]> r = cs.get(i).calculate(p);
				c[i] = r.fst();
				d_c[i] = r.snd();
			}
			double[][] m = new double[cs.size()][cs.size()];
			for (int i = 0; i < cs.size(); i++) {
				for (int j = i; j < cs.size(); j++) {
					m[i][j] = m[j][i] = dot(d_c[i], d_c[j]); 
				}
			}
			Matrix M = new Matrix(m);

			Matrix B = new Matrix(cs.size(), 1);
			for (int i = 0; i < cs.size(); i++) {
				double stiffness = cs.get(i).stiffness();
				B.set(i, 0, c[i]*stiffness*p.length/dt - dot(d_f, d_c[i]));
			}
			Matrix Lambda = M.solve(B);

			for (int i = 0; i < cs.size(); i++)
				for (int x = 0; x < p.length; x++)
					d_f[x] += Lambda.get(i, 0) * d_c[i][x];
		}
		return d_f;
	}
	
	public double dc_dt(double[] p, Constraint c) {
		ArrayList<Constraint> cs = _constraints;
		if (!cs.contains(c)) {
			throw new IllegalArgumentException("Constraint not contained");				
		}
		else {
			cs.remove(c);
			double[] d_f = df_constrained(p);
			double[] d_c = c.grad(p);
			cs.add(c);
			return -dot(d_f, d_c)/p.length;
		}
	}
}
