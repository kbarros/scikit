package scikit.util;

// TODO replace all occurrences by Tuple3d

public class Point implements Cloneable {
	public double x = 0, y = 0, z = 0;
	
	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Point() {
	}
	
	public String toString() {
		return "("+x+","+y+","+z+")";
	}
	
	public Point clone() {
		try {
			return (Point) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}
}
