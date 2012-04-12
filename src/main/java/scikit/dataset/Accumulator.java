package scikit.dataset;

import java.util.*;

import static java.lang.Math.*;
import scikit.dataset.Bin;

public class Accumulator extends DataSet {	
	protected double _binWidth;
	private SortedMap<Double, Bin> _hash;
    private boolean _errorBars = false;
    
	public Accumulator() {
		this(0);
	}
	
	public Accumulator(double binWidth) {
		_hash = new TreeMap<Double, Bin>();
		_binWidth = binWidth;
	}

	public Accumulator(Accumulator that, double binWidth) {
		this(binWidth);
		_errorBars = that._errorBars;
		
		for (Double k : that.keys()) {
			Bin v1 = _hash.get(k);
			Bin v2 = that._hash.get(k);
			if (v1 == null) {
				v1 = new Bin();
				v1.accum(v2);
				_hash.put(key(k), v1);
			}
			else {
				v1.accum(v2);
			}
		}
	}
	
	public void enableErrorBars(boolean errorBars) {
		_errorBars = true;
	}
	
	public void clear() {
		_hash = new TreeMap<Double, Bin>();
	}
	
	public DatasetBuffer copyData() {
		DatasetBuffer ret = new DatasetBuffer();
		ret._x = new double[_hash.size()];
		ret._y = new double[_hash.size()];
		if (_errorBars)
			ret._errY = new double[_hash.size()];
		int i = 0;
        for (Double k : _hash.keySet()) {
			ret._x[i] = k;
            ret._y[i] = eval(k);
            if (_errorBars)
            	ret._errY[i] = evalError(k);
            i++;
		}
		return ret;	
	}
	
	public Set<Double> keys() {
		return _hash.keySet();
	}
	
	public double maxKey() {
		double res = Double.MIN_VALUE;
		for (double k : keys())
			res = Math.max(res, k);
		return res;
	}
	
	public double eval(double x) {
		Bin val = _hash.get(key(x));
		return (val == null) ? Double.NaN : val.average();
	}
	
	public double evalCount(double x) {
		Bin val = _hash.get(key(x));
		return (val == null) ? Double.NaN : val.count();		
	}
	
	public double evalError(double x) {
		Bin val = _hash.get(key(x));
		return (val == null) ? Double.NaN : val.error();		
	}
	
	public void accum(double x, double y) {
		Bin val = _hash.get(key(x));
		if (val == null) {
			val = new Bin();
			val.accum(y);
			_hash.put(key(x), val);
		}
		else {
			val.accum(y);
		}
	}
	
	// key() gives the unique hash for every bin. it is the double value representing
	// the center of the bin.
	private double key(double x) {
		if (_binWidth == 0)
			return x;
		else {
			double bw = _binWidth;
			double k = bw * rint(x/bw); 
			return k == -0 ? +0 : k;    // +-0 have different representations.  choose +0.
		}
	}
}
