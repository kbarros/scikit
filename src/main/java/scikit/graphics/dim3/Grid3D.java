package scikit.graphics.dim3;

import static scikit.util.Utilities.format;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scikit.graphics.ColorChooser;
import scikit.graphics.ColorGradient;
import scikit.graphics.Drawable;
import scikit.numerics.vecmath.Quat4d;
import scikit.numerics.vecmath.VecHelper;
import scikit.util.Array3d;
import scikit.util.Bounds;
import scikit.util.DoubleArray;
import scikit.util.FileUtil;


public class Grid3D extends Scene3D {
	private static final Color CLEAR = new Color(0, 0, 0, 0);
	private static String[] VIEW_STRS = new String[] {"Surface", "Slice"};
	private Grid3DSurfaceView _surfaceView = new Grid3DSurfaceView(this);
	private Grid3DSliceView _sliceView = new Grid3DSliceView(this);
	private Grid3DView[] _views = new Grid3DView[] {_surfaceView, _sliceView};
	private int _curView;
	@SuppressWarnings("rawtypes")
	private JComboBox _viewCombo;
	private ColorChooser _colors = new ColorGradient();
	private int _w, _h, _d; // width, height, depth
	private double[] _data;
	private boolean _autoScale = true;
	private double _lo = 0, _hi = 1;
	
	public Grid3D(String title) {
		super(title);
		_viewCombo.setSelectedIndex(_curView);
	}

	public void clear() {
		// remove data first because super.clear() will repaint() the component
		_w = _h = _d = 0;
		_data = null;
		super.clear();
	}

	public Grid3DSliceView getSliceView() {
		return _sliceView;
	}
	
	public Grid3DSurfaceView getSurfaceView() {
		return _surfaceView;
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

	public void registerData(int w, int h, int d, double[] data) {
		allocateBuffers(w, h, d, data.length);
		System.arraycopy(data, 0, _data, 0, w*h*d);
		findRange();
		animate();
	}
	
	public void registerData(Array3d a3d) {
		registerData(a3d.nx(), a3d.ny(), a3d.nz(), a3d.array());
	}

	public void extractData(double[] dst) {
		System.arraycopy(_data, 0, dst, 0, _data.length);
	}

	public void saveData(String fname) {
		try {
			fname = FileUtil.saveDialog(_component, fname);
			if (fname != null) {
				new Array3d(_w, _h, _d, _data).writeFile(new File(fname));
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void loadData(String fname) {
		try {
			fname = FileUtil.loadDialog(_component, fname);
			if (fname != null) {
				registerData(new Array3d(new File(fname)));
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected JComponent createComponent(JComponent canvas) {
		final JSlider slider = new JSlider(0, 1000, 0);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				_views[_curView].setDisplayParam(slider.getValue()/1000.);
				_canvas.repaint();
			}
		});
		_viewCombo = new JComboBox(VIEW_STRS);
		_viewCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_curView = _viewCombo.getSelectedIndex();
				slider.setValue((int)(1000*_views[_curView].getDisplayParam()));
				_canvas.repaint();
			}
		});
		
		JPanel inputs = new JPanel(new BorderLayout());
		inputs.add(_viewCombo, BorderLayout.WEST);
		inputs.add(slider, BorderLayout.CENTER);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(super.createComponent(canvas), BorderLayout.CENTER);
		panel.add(inputs, BorderLayout.SOUTH);
		return panel;
	}

	protected List<Drawable<Gfx3D>> getAllDrawables() {
		List<Drawable<Gfx3D>> ds = new ArrayList<Drawable<Gfx3D>>();
		ds.add(_gridDrawable);
		ds.addAll(super.getAllDrawables());
		return ds;
	}

	protected List<JMenuItem> getAllPopupMenuItems() {
		List<JMenuItem> ret = new ArrayList<JMenuItem>(super.getAllPopupMenuItems());
		if (_data != null) {
			JMenuItem menuItem = new JMenuItem("Save grid data ...");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveData("grid.dat");
				}
			});
			ret.add(menuItem);
		}
		JMenuItem menuItem = new JMenuItem("Load grid data ...");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadData("");
			}
		});
		ret.add(menuItem);

		return ret;
	}
	
	protected void handleMouseDrag(double dx, double dy, MouseEvent event) {
		if (event.isShiftDown()) {
			Quat4d q = VecHelper.quatFromAxisAngle(RADS_PER_PIXEL*dy, RADS_PER_PIXEL*dx, 0);
			// q = r^-1 * q * r
			Quat4d r = new Quat4d(_rotation);
			q.mul(q, r);
			r.inverse();
			q.mul(r, q);
			_views[_curView].rotateStructure(q);
		}
		else {
			super.handleMouseDrag(dx, dy, event);
		}
	}
	
	protected double getSample(int x, int y, int z) {
		if (x < 0 || x >= _w || y < 0 || y >= _h || z < 0 || z >= _d)
			return Double.NEGATIVE_INFINITY;
		double v = _data[_w*_h*z+_w*y+x];
		return (v - _lo) / (_hi - _lo);
	}

	protected Color getColor(int x, int y, int z) {
		if (x < 0 || x >= _w || y < 0 || y >= _h || z < 0 || z >= _d)
			return CLEAR;
		else
			return _colors.getColor(_data[_w*_h*z+_w*y+x], _lo, _hi);
	}

	protected int[] getDimensions() {
		return new int[] {_w, _h, _d};
	}
	
	private void findRange() {
		if (_autoScale) {
			_lo = DoubleArray.min(_data);
			_hi = DoubleArray.max(_data);
		}
	}

	private void allocateBuffers(int w, int h, int d, int expectedSize) {
		if (w*h*d != expectedSize)
			throw new IllegalArgumentException("Array length " + expectedSize
					+ " does not fit specified shape (" + w + "*" + h + "*" + d + ")");
		if (w != _w || h != _h || d != _d) {
			_w = w;
			_h = h;
			_d = d;
			_data = new double[w*h*d];
		}
	}
	
	private Drawable<Gfx3D> _gridDrawable = new Drawable<Gfx3D>() {
		public void draw(Gfx3D g) {
			if (_data != null) {
				_views[_curView].draw(g);
			}
			g.ortho2D(g.pixelBounds());
			g.setColor(Color.BLACK);
			g.rasterString("lo = "+format(_lo), 10, 10+g.stringHeight(null));
			g.rasterString("hi = "+format(_hi), 10, 10);
			g.perspective3D(g.viewBounds());
		}
		public Bounds getBounds() {
			return new Bounds(0, 1, 0, 1, 0, 1);
		}
	};
}
