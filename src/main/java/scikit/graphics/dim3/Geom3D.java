package scikit.graphics.dim3;

import java.awt.Color;

import scikit.graphics.Drawable;
import scikit.util.Bounds;
import scikit.util.Point;

public class Geom3D {
	public static Drawable<Gfx3D> cuboid(final Bounds bds, final Color color) {
		return new Drawable<Gfx3D>() {
			public void draw(Gfx3D g) {
				g.setColor(color);
				g.drawCuboid(bds);
			}
			public Bounds getBounds() {
				return bds;
			}
		};
	}
	
	public static Drawable<Gfx3D> sphere(final Point center, final double radius, final Color color) {
		return new Drawable<Gfx3D>() {
			public void draw(Gfx3D g) {
				g.setColor(color);
				g.drawSphere(center, radius);
			}
			public Bounds getBounds() {
				return new Bounds(
						center.x-radius, center.x+radius,
						center.y-radius, center.y+radius,
						center.z-radius, center.z+radius);
			}
		};
	}
}
