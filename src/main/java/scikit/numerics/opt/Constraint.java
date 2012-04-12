package scikit.numerics.opt;

import scikit.numerics.fn.C1FunctionND;

abstract public class Constraint extends C1FunctionND {
	abstract public double stiffness();
}
