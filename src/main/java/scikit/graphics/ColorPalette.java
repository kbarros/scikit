package scikit.graphics;

import java.awt.Color;
import java.util.HashMap;

public class ColorPalette implements ColorChooser {
	HashMap<Integer,Color> colors = new HashMap<Integer,Color>();
	
	/**
	 * Sets the integer value to be associated with the color
	 * @param value
	 * @param color
	 */
	public void setColor(int value, Color color) {
		colors.put(value, color);
	}
	
	public Color getColor(double value, double lo, double hi) {
		return colors.containsKey((int)value) ? colors.get((int)value) : Color.BLACK;
	}
}
