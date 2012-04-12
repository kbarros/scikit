package scikit.graphics.dim2;

import java.awt.Color;

import scikit.graphics.Drawable;
import scikit.util.Bounds;
import scikit.util.Point;

public class Geom2D {
	public static Drawable<Gfx2D> line(
			final double x1, final double y1, final double x2, final double y2, final Color color) {
		return new Drawable<Gfx2D>() {
			public void draw(Gfx2D g) {
				g.setColor(color);
				g.drawLine(x1, y1, x2, y2);
			}
			public Bounds getBounds() {
				return new Bounds(new Point(x1, y1), new Point(x2, y2));
			}
		};
	}
	
	public static Drawable<Gfx2D> circle(final double x, final double y, final double radius, final Color color) {
		return new Drawable<Gfx2D>() {
			public void draw(Gfx2D g) {
				g.setColor(color);
				g.drawCircle(x, y, radius);
			}
			public Bounds getBounds() {
				return new Bounds(x-radius, x+radius, y-radius, y+radius);
			}
		};
	}
	
	public static Drawable<Gfx2D> rectangle(final Bounds bds, final Color color) {
		return new Drawable<Gfx2D>() {
			public void draw(Gfx2D g) {
				g.setColor(color);
				g.drawRect(bds.xmin, bds.ymin, bds.xmax-bds.xmin, bds.ymax-bds.ymin);
			}
			public Bounds getBounds() {
				return bds;
			}
		};
	}
}
