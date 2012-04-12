package scikit.graphics.dim2;

import static scikit.util.Utilities.format;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import scikit.graphics.ColorChooser;
import scikit.graphics.ColorGradient;
import scikit.graphics.Drawable;
import scikit.util.Bounds;
import scikit.util.DoubleArray;
import scikit.util.FileUtil;


public class Grid extends Scene2D {
	private ColorChooser _colors = new ColorGradient();
	private BufferedImage _image = null;
	private int _w = 0, _h = 0;
	private double[] _data = null;
    private int[] _pixelArray = null;
    private boolean _autoScale = true;
    private boolean _drawRange = false;
    private double _lo = 0, _hi = 1;
    
    
	public Grid(String title) {
		super(title);
	}
	
	public void clear() {
		// remove data first because super.clear() will cause a drawAll() operation
		_w = _h = 0;
		_image = null;
		_data = null;
		super.clear();
	}

	public void setColors(ColorChooser colors) {
		_colors = colors;
	}
	
	public void setAutoScale() {
		_autoScale = true;
	}
	
	public void setScale(double lo, double hi) {
		_autoScale = false;
		_lo = lo;
		_hi = hi;
	}
	
	public void setDrawRange(boolean b) {
		_drawRange = b;
	}
	
	public void registerData(int w, int h, double[] data) {
		allocateBuffers(w, h, data.length);
		System.arraycopy(data, 0, _data, 0, w*h);
		rasterizeImage();
		animate();
    }
	
	public void registerData(int w, int h, int[] data) {
		allocateBuffers(w, h, data.length);
		for (int i = 0; i < data.length; i++)
			_data[i] = data[i];
		rasterizeImage();
		animate();
	}
	
	// Override getImage() to return the "native" pixel-map image
	public BufferedImage getImage(int width, int height) {
		return _image;
	}
	
	public BufferedImage getImage() {
		return _image;
	}
	
	protected JComponent createCanvas() {
		return Gfx2DSwing.createComponent(this);
	}
	
	protected void drawBackground(Gfx2D g) {
		// looks better without background
	}
	
	protected List<Drawable<Gfx2D>> getAllDrawables() {
		List<Drawable<Gfx2D>> ds = new ArrayList<Drawable<Gfx2D>>();
		ds.add(_gridDrawable);
		ds.addAll(super.getAllDrawables());
		return ds;
	}
	
	protected List<JMenuItem> getAllPopupMenuItems() {
		List<JMenuItem> ret = new ArrayList<JMenuItem>(super.getAllPopupMenuItems());

		JMenuItem menuItem = new JCheckBoxMenuItem("Display range", _drawRange);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_drawRange = !_drawRange;
				animate();
			}
		});
		ret.add(menuItem);
		
		if (_data != null) {
			menuItem = new JMenuItem("Save grid data ...");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveData("grid.txt");
				}
			});
			ret.add(menuItem);
			menuItem = new JMenuItem("Save image ...");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveImage("grid.png");
				}
			});
			ret.add(menuItem);
		}
		return ret;
	}
	
	private void allocateBuffers(int w, int h, int expectedSize) {
		if (w*h == 0)
			throw new IllegalArgumentException("Illegal specified shape (" + w + "*" + h + ")");
		if (w*h > expectedSize)
			throw new IllegalArgumentException("Array length " + expectedSize
					+ " does not fit specified shape (" + w + "*" + h + ")");
		if (w != _w || h != _h) {
    		_w = w;
    		_h = h;
    		_data = new double[w*h];
    		_pixelArray = new int[w*h*3];
    		_image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    	}
	}
	
	private void findRange() {
		if (_autoScale) {
			_lo = DoubleArray.min(_data);
			_hi = DoubleArray.max(_data);
		}
	}
	
	private void rasterizeImage() {
		findRange();
		// draw pixels
		int pixelArrayOffset = 0;
		for (int y = 0; y < _h; y++) {
			for (int x = 0; x < _w; x++) {
				Color color = _colors.getColor(_data[_w*y+x], _lo, _hi);
				_pixelArray[pixelArrayOffset++] = color.getRed();
				_pixelArray[pixelArrayOffset++] = color.getGreen();
				_pixelArray[pixelArrayOffset++] = color.getBlue();
			}
		}
		// copy pixels into image
		WritableRaster raster = _image.getRaster();
		raster.setPixels(0, 0, _w, _h, _pixelArray);
	}
	
	private Drawable<Gfx2D> _gridDrawable = new Drawable<Gfx2D>() {
		public void draw(Gfx2D g) {
	        if (_image != null) {
	        	((Gfx2DSwing)g).drawImage(_image, 0, 0, 1, 1);
	        	
	        	if (_drawRange) {
	        		g.setProjection(g.pixelBounds()); // draw strings at fixed pixels
	        		String str1 = "lo = "+format(_lo);
	        		String str2 = "hi = "+format(_hi);
	        		double border = 4;
	        		double offset = 4;
	        		double w = Math.max(g.stringWidth(str1), g.stringWidth(str2));
	        		double h = g.stringHeight("");
	        		g.setColor(new Color(1f, 1f, 1f, 0.5f));
	        		g.fillRect(offset, offset, 2*border+w, 3*border+2*h);
	        		g.setColor(Color.BLACK);
	        		g.drawString(str1, offset+border, offset+h+2*border);
	        		g.drawString(str2, offset+border, offset+border);
	        		g.setProjection(g.viewBounds()); // return to scene coordinates
	        	}
	        }
		}
		public Bounds getBounds() {
			return new Bounds(0, 1, 0, 1);
		}
	};
	
	private void saveData(String fname) {
		try {
			fname = FileUtil.saveDialog(_component, fname);
			if (fname != null) {
				PrintWriter pw = FileUtil.pwFromString(fname);
				FileUtil.writeOctaveGrid(pw, _data, _w, 1);
				pw.close();
			}
		} catch (IOException e) {}
	}

	private void saveImage(String fname) {
		try {
			fname = FileUtil.saveDialog(_component, fname);
			if (fname != null) {
				ImageIO.write(_image, "png", new File(fname));
			}
		} catch (IOException e) {}
	}
}
