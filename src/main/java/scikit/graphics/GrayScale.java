package scikit.graphics;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;

public class GrayScale implements ColorChooser {
	private static int WHEEL_SIZE = 256;
	private static Color wheel[] = new Color[WHEEL_SIZE];

	static {
        for (int i = 0; i < WHEEL_SIZE; i++) {
            wheel[i] = new Color(i, i, i);
        }
    }

	public Color getColor(double v, double lo, double hi) {
		v = (v - lo) / (hi - lo);
		int c = (int) (WHEEL_SIZE*v);
		return wheel[min(max(c, 0), WHEEL_SIZE-1)];
	}
}
