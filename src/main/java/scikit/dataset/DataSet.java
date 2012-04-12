package scikit.dataset;

import scikit.util.Bounds;
import scikit.util.DoubleArray;


abstract public class DataSet {
	/** Returns data width: xmin, xmax, ymin, ymax */
	public Bounds getBounds() {
		DatasetBuffer data = copyData();
		if (data.size() == 0)
			return new Bounds();
		else {
			return new Bounds(
					DoubleArray.min(data._x),
					DoubleArray.max(data._x),
					DoubleArray.min(data._y),
					DoubleArray.max(data._y)
			);
		}
	}
	
	/** Returns a copy of this dataset in the format [x1, y1, x2, y2, ...] */
	abstract public DatasetBuffer copyData();
	
	/** Returns a copy of the subset of this data within range */
	public DatasetBuffer copyPartial(int N, Bounds bds) {
		return copyData();
	}
}
