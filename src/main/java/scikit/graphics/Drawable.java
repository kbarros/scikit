package scikit.graphics;

import scikit.util.Bounds;

public interface Drawable<T> {
	public void draw(T graphics);
	public Bounds getBounds();
}
