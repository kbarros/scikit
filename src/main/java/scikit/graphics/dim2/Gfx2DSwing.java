package scikit.graphics.dim2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import scikit.util.Bounds;


public class Gfx2DSwing implements Gfx2D {
	private final Graphics2D _engine;
	private final Bounds _pixBds, _viewBds;
	private Bounds _proj;
	
	public Gfx2DSwing(Graphics2D engine, JComponent component, Scene2D scene) {
		_engine = engine;
		_pixBds = new Bounds(0, component.getWidth(), 0, component.getHeight());
		_proj = _viewBds = scene.viewBounds();
	}
	
	public Graphics2D engine() {
		return _engine;
	}
	
	public Bounds viewBounds() {
		return _viewBds;
	}
	
	public Bounds pixelBounds() {
		return _pixBds;
	}
	
	public void setProjection(Bounds proj) {
		// incoming points are transformed from datBds (data coordinates) 
		// into pixBds (pixel coordinates)
		_proj = proj;
	}

	private int transX(double x) {
		return (int) (_pixBds.xmax * (x - _proj.xmin) / _proj.getWidth());
	}
	
	private int transY(double y) {
		return (int) (_pixBds.getHeight() - _pixBds.ymax * (y - _proj.ymin) / _proj.getHeight());
	}
	
	private int offsetX(double w) {
		return (int) (w * _pixBds.getWidth() / _proj.getWidth());
	}
	
	private int offsetY(double h) {
		return (int) (h * _pixBds.getHeight() / _proj.getHeight());
	}
	
	public void setLineSmoothing(boolean b) {
		// this functionality is not necessary in the Swing engine -- its application
		// in GL is to avoid sub-pixel rendering (which causes line smearing)
	}
	
	public void setColor(Color color) {
		_engine.setColor(color);
	}
	
	public void drawPoint(double x, double y) {
		int pix = 4;
		_engine.fillRect((int)transX(x)-pix/2, (int)transY(y)-pix/2, pix, pix);
	}
	
	public void drawLine(double x1, double y1, double x2, double y2) {
		_engine.drawLine(transX(x1), transY(y1), transX(x2), transY(y2));
	}

	public void drawLines(double[] xys) {
		for (int i = 2; i < xys.length; i += 2)
			drawLine(xys[i-2], xys[i-1], xys[i+0], xys[i+1]);
	}

	public void drawRect(double x, double y, double w, double h) {
		_engine.drawRect(transX(x), transY(y)-offsetY(h), offsetX(w), offsetY(h));
	}

	public void fillRect(double x, double y, double w, double h) {
		_engine.fillRect(transX(x), transY(y)-offsetY(h), offsetX(w), offsetY(h));
	}
	
	public void drawCircle(double x, double y, double r) {
		int w = offsetX(2*r);
		int h = offsetY(2*r);
		_engine.drawOval(transX(x)-w/2, transY(y)-h/2, w, h);
	}

	public void fillCircle(double x, double y, double r) {
		int w = offsetX(2*r);
		int h = offsetY(2*r);
		_engine.fillOval(transX(x)-w/2, transY(y)-h/2, w, h);
	}

	public double stringWidth(String str) {
		double pix2dat = _proj.getWidth() / _pixBds.getWidth();
		return _engine.getFontMetrics().getStringBounds(str, _engine).getWidth() * pix2dat;
	}
	
	public double stringHeight(String str) {
		double pix2dat = _proj.getHeight() / _pixBds.getHeight();
		double fudge = 0.7;
		return _engine.getFontMetrics().getStringBounds(str, _engine).getHeight() * pix2dat * fudge;
	}

	public void drawString(String str, double x, double y) {
		_engine.drawString(str, transX(x), transY(y));
	}

	public static JComponent createComponent(final Scene2D scene) {
		final JComponent component = new JComponent() {
			private static final long serialVersionUID = 1L;
			public void paintComponent(java.awt.Graphics engine) {
				super.paintComponent(engine);
				engine = engine.create();
				// when drawing images (e.g. renderImage()), keep scaled pixel boundaries
				// crisp.  on some platforms (e.g. OS X), this hint must be applied
				// before any drawing occurs
				((Graphics2D)engine).setRenderingHint(
						RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				// anti-aliasing on linux and windows doesn't look that great. oh well.
				 ((Graphics2D)engine).setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
                		RenderingHints.VALUE_ANTIALIAS_ON);
				 scene.drawAll(new Gfx2DSwing((Graphics2D)engine, this, scene));
				 engine.dispose();
			}
		};
		return component;
	}
	
	public void drawImage(BufferedImage image, double x1, double y1, double x2, double y2) {
    	int x1p = transX(x1);
    	int y1p = transY(y1);
    	int x2p = transX(x2);
    	int y2p = transY(y2);
		int w = image.getWidth();
		int h = image.getHeight();
        _engine.drawImage(image, x1p, y1p, x2p, y2p, 0, 0, w, h, null);
	}
}
