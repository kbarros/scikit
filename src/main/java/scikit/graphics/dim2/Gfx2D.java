package scikit.graphics.dim2;

import java.awt.Color;

import scikit.util.Bounds;


public interface Gfx2D {
	public Bounds pixelBounds();
	public Bounds viewBounds();
	public void setProjection(Bounds bds);
	public void setLineSmoothing(boolean b);
	public void setColor(Color color);
	public void drawPoint(double x, double y);
	public void drawLine(double x1, double y1, double x2, double y2);
	public void drawLines(double[] xys);
	public void drawRect(double x, double y, double w, double h);
	public void fillRect(double x, double y, double w, double h);
	public void drawCircle(double x, double y, double r);
	public void fillCircle(double x, double y, double r);
	public double stringWidth(String str);
	public double stringHeight(String str);
	public void drawString(String str, double x, double y);
}
