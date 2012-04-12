package scikit.dataset;

public class Derivative extends DataSet {
	private DataSet _src;
	
	public boolean invertDependentParameter = false;
	
	public Derivative(DataSet src) {
		_src = src;
	}
	
	public DatasetBuffer copyData() {
		DatasetBuffer dat = _src.copyData();
		DatasetBuffer ret = new DatasetBuffer();
		ret._x = new double[dat.size()-1];
		ret._y = new double[dat.size()-1];
		
		for (int i = 0; i < dat.size()-1; i++) {
			double x1 = dat._x[i];
			double y1 = dat._y[i];
			double x2 = dat._x[i+1];
			double y2 = dat._y[i+1];
			ret._x[i] = (invertDependentParameter ? (y1+y2) : (x1+x2)) / 2;
			ret._y[i] = (y2 - y1) / (x2 - x1);
		}
		return ret;
	}

}
