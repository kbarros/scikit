package scikit.numerics.fn;

import scikit.util.Pair;

abstract public class C1FunctionND implements FunctionND {
	abstract public Pair<Double,double[]> calculate(double[] p);
	
	public double eval(double[] p) {
		return calculate(p).fst();
	}
	
	public double[] grad(double[] p) {
		return calculate(p).snd();
	}
}
