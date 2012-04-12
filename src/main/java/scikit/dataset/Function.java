package scikit.dataset;

import static java.lang.Math.*;
import scikit.util.Bounds;

abstract public class Function extends DataSet {
	Bounds bds = new Bounds();
	int N = 1024;
	
	/** Creates a new function which has no bounds of its own. */
	public Function() {
	}
	
	/** Creates a new function with given x-bounds. The y-bounds will be determined
	 * at construction time by evaluating x values in the range provided. */
	public Function(double xmin, double xmax) {
		bds.xmin = xmin;
		bds.xmax = xmax;
        bds.ymin = bds.ymax = eval(xmin);
        for (double x = xmin; x < xmax; x += (xmax-xmin)/100) {
            bds.ymin = min(bds.ymin, eval(x));
            bds.ymax = max(bds.ymax, eval(x));
        }
	}
    
    public Bounds getBounds() {
        return bds;
    }
    
	public DatasetBuffer copyData() {
		return copyPartial(N, bds);
	}
	
	public DatasetBuffer copyPartial(int N, Bounds bds) {
		DatasetBuffer ret = new DatasetBuffer();
		ret._x = new double[N];
		ret._y = new double[N];
		for (int i = 0; i < N; i++) {
			double x = (bds.xmax - bds.xmin) * i / (N-1) + bds.xmin;
			ret._x[i] = x;
			ret._y[i] = eval(x);
		}
		return ret;
	}
	
	public abstract double eval(double x);
}
