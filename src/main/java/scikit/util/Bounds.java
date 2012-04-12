package scikit.util;

import static java.lang.Math.*;
import scikit.numerics.vecmath.Vector3d;

public class Bounds {
	private final double INF = Double.POSITIVE_INFINITY;
	public double xmin = INF, xmax = -INF;
	public double ymin = INF, ymax = -INF;
	public double zmin = INF, zmax = -INF;
	
	public Bounds() {}
	
	public Bounds(double xmin, double xmax, double ymin, double ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}
	
	public Bounds(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax) {
		this(xmin, xmax, ymin, ymax);
		this.zmin = zmin;
		this.zmax = zmax;
	}
	
	public Bounds(Point... pts) {
		for (Point pt : pts) {
			xmin = min(xmin, pt.x);
			xmax = max(xmax, pt.x);
			ymin = min(ymin, pt.y);
			ymax = max(ymax, pt.y);
			zmin = min(zmin, pt.z);
			zmax = max(zmax, pt.z);
		}
	}
	
	public Bounds clone() {
		return new Bounds(xmin, xmax, ymin, ymax, zmin, zmax);
	}
	
	public String toString() {
		return "["+xmin+"::"+xmax+" , "+ymin+"::"+ymax+" , "+zmin+"::"+zmax+"]";
	}
	
	public Bounds union(Bounds... bs) {
		Bounds ret = clone();
		for (Bounds b : bs) {
			ret.xmin = min(ret.xmin, b.xmin);
			ret.xmax = max(ret.xmax, b.xmax);
			ret.ymin = min(ret.ymin, b.ymin);
			ret.ymax = max(ret.ymax, b.ymax);
			ret.zmin = min(ret.zmin, b.zmin);
			ret.zmax = max(ret.zmax, b.zmax);
		}
		return ret;
	}
	
	/**
	 * Creates a new bounds object with width and height scaled by factor. Does not change the
	 * bounds center.
	 * @param factor
	 * @return Scaled bounds
	 */
	public Bounds scale(double factor) {
		double s = (factor-1)/2;
		double w = max(getWidth(), 0);
		double h = max(getHeight(), 0);
		double d = max(getDepth(), 0);
		return new Bounds(xmin-w*s, xmax+w*s, ymin-h*s, ymax+h*s, zmin-d*s, zmax+d*s);
	}
	
	/**
	 * Creates a new bounds object which is translated by the displacement d
	 * @param d
	 * @return Translated bounds
	 */
	public Bounds translate(Vector3d d) {
		return new Bounds(
				xmin+d.x, xmax+d.x,
				ymin+d.y, ymax+d.y,
				zmin+d.z, zmax+d.z);
	}
	
	/**
	 * Returns the center of bounds, obtained by averaging the min and max values
	 * in each dimension.
	 * @return the bounds center
	 */
	public Point getCenter() {
		return new Point((xmin+xmax)/2., (ymin+ymax)/2., (zmin+zmax)/2.);
	}
	
	/**
	 * Returns the volume of the bounds.
	 * @return the bounds volume
	 */
	public double getVolume() {
		return getWidth() * getHeight() * getDepth();
	}
	
	/**
	 * Returns the bounds width (xmax - xmin).
	 * @return the bounds width
	 */
	public double getWidth() {
		return xmax - xmin;
	}
	
	/**
	 * Returns the bounds height (ymax - ymin).
	 * @return the bounds height
	 */
	public double getHeight() {
		return ymax - ymin;
	}
	
	/**
	 * Returns the bounds depth (zmax - zmin).
	 * @return the bounds depth
	 */
	public double getDepth() {
		return zmax - zmin;
	}
}
