package scikit.numerics.fn;

import scikit.util.Pair;

abstract public class C1Function1D implements Function1D {
	abstract public Pair<Double,Double> calculate(double x);
	
	public double eval(double x) {
		return calculate(x).fst();
	}
	
	public double deriv(double x) {
		return calculate(x).snd();
	}
}
