package scikit.numerics.opt;

import java.util.Collections;
import java.util.Vector;

import scikit.numerics.fn.FunctionND;
import scikit.util.DoubleArray;


class Pt implements Comparable<Pt> {
	double[] x;
	double y;
	
	public Pt(double[] x, double y) {
		this.x = x;
		this.y = y;
	}

	public int compareTo(Pt o) {
		return Double.compare(y, o.y);
	}
}

public class NelderMead extends Optimizer<FunctionND> {
	double _scale;
	int _dim;
	double[] _p;
	Vector<Pt> _pts;
	
	// temporary arrays
	double[] _centroid;
	double[] _reflect;
	
	public NelderMead(int dim, double scale) {
		super(dim);
		_scale = scale;
	}
	
	public void initialize(double[] p) {
		super.initialize(p);
		_dim = p.length;
		_p = p;
		_pts = new Vector<Pt>();
		
		_reflect = new double[_dim];
		_centroid = new double[_dim];
		
		for (int i = 0; i < _dim+1; i++) {
			double[] p_i = DoubleArray.clone(p);
			if (i < _dim)
				p_i[i] += _scale;
			_pts.add(new Pt(p_i, _f.eval(p_i)));
		}
		Collections.sort(_pts);
	}
	
	public void step() {
		Pt pt = _pts.get(_dim);
		generateReflection(pt, -1);
		
		if (pt.y > _pts.get(_dim-1).y) {
			// the reflected point is worse than the second worst point.
			// look for an intermediate lower point (i.e. a one dimensional contraction).
			generateReflection(pt, 0.5);
			if (pt.y > _pts.get(_dim-1).y) {
				// still haven't improved on the second worst point. shrink all
				// points toward the best point
				contract(_pts.get(0));
			}
		}
		else if (pt.y < _pts.get(0).y) {
			// the reflected point is now the best known. try an additional extrapolation
			// to grow the simplex.
			generateReflection(pt, 2.);
		}
		
		// _pts[_dim] may no longer be the worst point; insert it into the correct position.
		Collections.sort(_pts);
	}
	
	
	// build centroid from all points excluding 'pt'
	private void getCentroid(Pt pt) {
		DoubleArray.zero(_centroid);
		for (Pt pt2 : _pts) {
			if (pt2 != pt)
				DoubleArray.add(_centroid, pt2.x, _centroid);
		}
		DoubleArray.scale(_centroid, 1./_dim);
	}
	
	// reflect across centroid by magnitude 'mag'
	private void generateReflection(Pt pt, double mag) {
		getCentroid(pt);
		for (int j = 0; j < _dim; j++)
			_reflect[j] = _centroid[j]*(1.-mag) + pt.x[j]*mag;
		
		double ytry = _f.eval(_reflect);
		if (ytry < pt.y) {
			DoubleArray.copy(_reflect, pt.x);
			pt.y = ytry;
		}
	}
	
	private void contract(Pt pt) {
		for (Pt pt2 : _pts) {
			if (pt2 != pt) {
				DoubleArray.add(pt.x, pt2.x, pt2.x);
				DoubleArray.scale(pt2.x, 0.5);
				pt2.y = _f.eval(pt2.x);
			}
		}
	}
}
