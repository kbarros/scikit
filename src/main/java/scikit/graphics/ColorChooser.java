package scikit.graphics;

import java.awt.Color;

public interface ColorChooser {
	/**
	 * Returns the color associated with value v.
	 * @param v the color value
	 * @param low the value v is expected to be bounded from above by low
	 * @param high the value v is expected to be bounded from below by high
	 * @return The associated color.
	 */
	public Color getColor(double v, double low, double high);
}
