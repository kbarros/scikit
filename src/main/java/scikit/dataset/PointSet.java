package scikit.dataset;

import scikit.util.DoubleArray;


public class PointSet extends DataSet {
	// TODO dynamicarray
	private double[] _x, _y;
	
	
	public PointSet(double x0, double dx, double[] y) {
		_x = new double[y.length];
		for (int i = 0; i < y.length; i++) {
			_x[i] = x0 + i*dx;
		}
		_y = y;
	}
	
	
	public PointSet(double[] x, double[] y) {
		setXY(x, y);
	}
	
	
	public void setX(double[] x) {
		setXY(x, _y);
	}
	
	public void setY(double[] y) {
		setXY(_x, y);
	}
	
	public void setXY(double[] x, double[] y) {
		if (x.length != y.length)
			throw new IllegalArgumentException("Array sizes are not equal.");
		_x = x;
		_y = y;
	}
	
	
	public DatasetBuffer copyData() {
		DatasetBuffer ret = new DatasetBuffer();
		ret._x = DoubleArray.clone(_x);
		ret._y = DoubleArray.clone(_y);
		return ret;
	}
}
