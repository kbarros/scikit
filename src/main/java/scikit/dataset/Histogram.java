package scikit.dataset;




public class Histogram extends Accumulator {
    private double _fullSum = 0;
	private boolean _normalizing = false;
	
	public Histogram(double binWidth) {
		super(binWidth);
	}
	
	public Histogram(Histogram that, double binWidth) {
		super(that, binWidth);
		_fullSum = that._fullSum;
		_normalizing = that._normalizing;
	}
    
    public void setNormalizing(boolean norm) {
        _normalizing = norm;
    }
	
	public double eval(double x) {
		double ret = super.eval(x)*evalCount(x);
		if (_normalizing)
            ret /= (_binWidth * _fullSum);
		return ret;
	}
	
	public double evalError(double x) {
		double ret = super.evalError(x)*evalCount(x);
		if (_normalizing)
            ret /= (_binWidth * _fullSum);
		return ret;
	}
	
	
	public void accum(double x, double y) {
		super.accum(x, y);
		_fullSum += y;
	}
	
	public void accum(double x) {
		accum(x, 1.0);
	}
}
