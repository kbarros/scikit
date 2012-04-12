package scikit.graphics;

import java.awt.Color;
import static java.lang.Math.*;

public class ColorGradient implements ColorChooser {
	private static double _colors[][] = {
			{1-1.0,     0, 0, 0},
			{1-0.98,    10, 0, 50},
			{1-0.95,    20, 0, 80},
			{1-0.85,    61, 0, 130}, // blue
			{1-0.7,    121, 20, 150}, // blue
			{1-0.5,    190, 40, 90}, // solid red
			{1-0.35,   215, 90, 40}, // red
			{1-0.15,   235, 195, 80}, // yellow
			{1-0,      255, 255, 255}
	};
	private static int WHEEL_SIZE = 512;
	private static Color wheel[] = new Color[WHEEL_SIZE];
	static {
        for (int i = 0; i < WHEEL_SIZE; i++) {
            double a = (double)i / WHEEL_SIZE;
            // get color for value 'a'
            int j = 0;
            while (a >= _colors[j+1][0])
                j++;
            double v = (a - _colors[j][0]) / (_colors[j+1][0] - _colors[j][0]);
            int r = (int) (_colors[j][1]*(1-v) + _colors[j+1][1]*v);
            int g = (int) (_colors[j][2]*(1-v) + _colors[j+1][2]*v);
            int b = (int) (_colors[j][3]*(1-v) + _colors[j+1][3]*v);
            wheel[i] = new Color(r, g, b);
        }
    }
	
	public Color getColor(double v, double lo, double hi) {
		v = (v - lo) / (hi - lo);
		int c = (int) (WHEEL_SIZE*v);
		return wheel[min(max(c, 0), WHEEL_SIZE-1)];
	}
}
