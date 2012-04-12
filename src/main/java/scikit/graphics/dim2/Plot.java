package scikit.graphics.dim2;

import static java.lang.Math.log10;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import scikit.dataset.DataSet;
import scikit.dataset.DatasetBuffer;
import scikit.graphics.Drawable;
import scikit.util.Bounds;
import scikit.util.FileUtil;


public class Plot extends Scene2D {
	private JLabel _xlabel, _ylabel;
	protected ArrayList<RegisteredData> _datas = new ArrayList<RegisteredData>();
	// log-scale drawing is handled as follows:
	//  - all registered Datasets are reinterpreted (x,y)->(log x,log y)
	//  - the viewbounds are calculated using transformed Datasets
	//  - the TickMarks Drawable changes its mode
	//  - Drawables which are not Datasets are hidden, since non-linear warping
	//    can't be accurately represented
	protected boolean _logScaleX = false, _logScaleY = false;
	protected boolean _errorBarsX = false, _errorBarsY = true;
	
	public Plot(String title) {
		super(title);
		_visibleBoundsBufferScale = 1.1;
	}
	
	public Plot(String title, String xlabel, String ylabel) {
		super(title);
		setAxisLabels(xlabel, ylabel);
	}
	
	public void animate() {
		// if it is invalid to display the system on a log scale then use a linear scale.
		// in this case, clear current view bounds so that super.animate() can start fresh.
		Bounds bds = dataBounds();
		if (bds.xmin == Double.NEGATIVE_INFINITY) {
			_logScaleX = false;
			_curBounds = new Bounds();
		}
		if (bds.ymin == Double.NEGATIVE_INFINITY) {
			_logScaleY = false;
			_curBounds = new Bounds();
		}
		// do not draw geometric primitives on a log scale, since they won't be correctly
		// transformed
		_suppressDrawables = _logScaleX || _logScaleY;
		// ready to go!
		super.animate();
	}
	
	public void clear() {
		// remove data sets first because super.clear() will cause a drawAll() operation
		_datas.clear();
		super.clear();
	}
	
	/**
	 * Sets the plot view to optionally display the x and/or y coordinates on a logarithmic
	 * scale. Also animates the display.
	 * 
	 * @param logScaleX True if the x coordinate should be displayed on a logarithmic scale 
	 * @param logScaleY True if the y coordinate should be displayed on a logarithmic scale
	 */
	public void setLogScale(boolean logScaleX, boolean logScaleY) {
		if (logScaleX != _logScaleX || logScaleY != _logScaleY) {
			_logScaleX = logScaleX;
			_logScaleY = logScaleY;
			// clear current view bounds
			_zoomed = false;
			_curBounds = new Bounds();
			// calculate new view bounds and redisplay
			animate();
		}
	}
	
	/**
	 * Gets the dataset which is registered by the String <code>name</code>. If no such dataset
	 * exists, then return null
	 * @param name
	 * @return The dataset registered under <code>name</code>
	 */
	public DataSet getDataset(String name) {
		for (final RegisteredData d : _datas) {
			if (d._name.equals(name)) {
				return d._data;
			}	
		}
		return null;
	}
	
	/**
	 * Registers the dataset corresponding to <code>name</code> to display points. If a dataset
	 * with the same name is already registered, it will be replaced by this one. Also animates
	 * the display.
	 * 
	 * @param name The name of the dataset
	 * @param data The dataset to be registered
	 * @param color The color of the dataset
	 */
	public void registerPoints(String name, DataSet data, Color color) {
		registerDataset(name, data, color, RegisteredData.Style.MARKS);
	}

	/**
	 * Registers the dataset corresponding to <code>name</code> to display lines. If a dataset
	 * with the same name is already registered, it will be replaced by this one. Also animates
	 * the display.
	 * 
	 * @param name The name of the dataset
	 * @param data The dataset to be registered
	 * @param color The color of the dataset
	 */
	public void registerLines(String name, DataSet data, Color color) {
		registerDataset(name, data, color, RegisteredData.Style.LINES);
	}
	
	/**
	 * Registers the dataset corresponding to <code>name</code> to display bars. If a dataset
	 * with the same name is already registered, it will be replaced by this one. Also animates
	 * the display.
	 * 
	 * @param name The name of the dataset
	 * @param data The dataset to be registered
	 * @param color The color of the dataset
	 */
	public void registerBars(String name, DataSet data, Color color) {
		registerDataset(name, data, color, RegisteredData.Style.BARS);
	}
	
	public void setAxisLabels(String xstr, String ystr) {
		_xlabel.setText(xstr);
		_ylabel.setIcon(new VTextIcon(_ylabel, ystr, VTextIcon.ROTATE_LEFT));
	}
	
	protected JComponent createComponent(JComponent canvas) {
		_ylabel = new JLabel();
		_xlabel = new JLabel();
		_xlabel.setHorizontalAlignment(JLabel.CENTER);
		
		JPanel component = new JPanel(new BorderLayout());
		component.add(super.createComponent(canvas), BorderLayout.CENTER);
		component.add(_xlabel, BorderLayout.SOUTH);
		component.add(_ylabel, BorderLayout.WEST);
		return component;
	}
	
	protected List<Drawable<Gfx2D>> getAllDrawables() {
		List<Drawable<Gfx2D>> ds = new ArrayList<Drawable<Gfx2D>>();
		ds.add(new TickMarks(this));
		ds.addAll(_datas);
		ds.addAll(super.getAllDrawables());
		return ds;
	}
	
	protected List<JMenuItem> getAllPopupMenuItems() {
		List<JMenuItem> ret = new ArrayList<JMenuItem>(super.getAllPopupMenuItems());
		
		// add log/linear scale menu items
		JMenuItem itemX = new JMenuItem(_logScaleX ? "Set Linear in X" : "Set Logarithmic in X");
		JMenuItem itemY = new JMenuItem(_logScaleY ? "Set Linear in Y" : "Set Logarithmic in Y");
		itemX.setEnabled(_logScaleX || dataBounds().xmin > 0);
		itemY.setEnabled(_logScaleY || dataBounds().ymin > 0);
		itemX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setLogScale(!_logScaleX, _logScaleY);
			}
		});
		itemY.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setLogScale(_logScaleX, !_logScaleY);
			}
		});
		ret.add(itemX);
		ret.add(itemY);
		
		// add save dataset menu items
		for (final RegisteredData d : _datas) {
			JMenuItem menuItem = new JMenuItem("Save '" + d._name + "' ...");
			menuItem.setForeground(d._color);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveDataset(d._data, d._name+".txt");
				}
			});
			ret.add(menuItem);
		}
		return ret;
	}
	
	private void registerDataset(String name, DataSet data, Color color, RegisteredData.Style style) {
		RegisteredData dw = new RegisteredData(this, name, data, color, style);
		// if the list contains an element with the same name as 'dataset',
		// replace that element with 'dataset'
		if (_datas.contains(dw))
			_datas.set(_datas.indexOf(dw), dw);
		// otherwise, add 'dataset' to the end of the list
		else
			_datas.add(dw);
		animate();
	}
	
	private void saveDataset(DataSet data, String fname) {
		try {
			fname = FileUtil.saveDialog(_component, fname);
			if (fname != null) {
				PrintWriter pw = FileUtil.pwFromString(fname);
				FileUtil.writeColumns(pw, data.copyData().columns());
				pw.close();
			}
		} catch (IOException e) {}
	}
}

class RegisteredData implements Drawable<Gfx2D> {
	enum Style {LINES, MARKS, BARS};
	
	Plot _plot;
	String _name;
	DataSet _data;
	Color _color;
	Style _style;

	public RegisteredData(Plot plot, String name, DataSet data, Color color, Style style) {
		_plot = plot;
		_name = name;
		_data = data;
		_color = color;
		_style = style;
	}
	
	public void draw(Gfx2D g) {
		Bounds bds = expBounds(g.viewBounds());
		DatasetBuffer pts = _data.copyPartial(1000, bds);
		
		drawErrorBarsY(g, pts);
		drawMarks(g, pts);
	}
	
	public Bounds getBounds() {
		Bounds bds = logBounds(_data.getBounds());
		if (_style == Style.BARS) {
			bds.ymin = min(bds.ymin, 0);
			bds.ymax = max(bds.ymax, 0);
		}
		if (_plot._logScaleY && _style == Style.BARS)
			throw new IllegalArgumentException("Can't draw bars with vertical logscale.");
		return bds;
	}
	
	// implement a special form of equality: two "dataset drawables" are equal
	// when their names are equal.
	public boolean equals(Object data) {
		if (data instanceof RegisteredData)
			return _name.equals(((RegisteredData)data)._name);
		else
			return false;
	}
	
	private void drawErrorBarsY(Gfx2D g, DatasetBuffer pts) {
		if (!_plot._errorBarsY || !pts.hasErrorY())
			return;

		//int red = _color.getRed();
		//int green = _color.getGreen();
		//int blue = _color.getBlue();
		//int alpha = _color.getAlpha();
		//g.setColor(new Color(red, green, blue, alpha*2/3));
		g.setColor(Color.RED);
		
		for (int i = 0; i < pts.size(); i++) {
			double y_lo = pts.y(i) - pts.errorY(i);
			double y_hi = pts.y(i) + pts.errorY(i);
			g.drawLine(tx(pts,i), ty(y_lo), tx(pts,i), ty(y_hi));
		}
	}
	
	
	private double tx(double x) {
		return (_plot._logScaleX) ? log10(x) : x;
	}
	
	private double tx(DatasetBuffer pts, int i) {
		return tx(pts.x(i));
	}
	
	private double ty(double y) {
		return (_plot._logScaleY) ? log10(y) : y;
	}
	
	private double ty(DatasetBuffer pts, int i) {
		return ty(pts.y(i));
	}

	private void drawMarks(Gfx2D g, DatasetBuffer pts) {
		g.setColor(_color);
		for (int i = 0; i < pts.size(); i++) {
			switch (_style) {
			case MARKS:
				g.drawPoint(tx(pts,i), ty(pts,i));
				break;
			case LINES:
				if (i >= 1)
					g.drawLine(tx(pts,i-1), ty(pts,i-1), tx(pts,i), ty(pts,i));
				break;
			case BARS:
				g.drawLine(tx(pts,i), ty(pts,i), tx(pts,i), 0);
				break;
			}
		}
	}

	private Bounds expBounds(Bounds in) {
		double xmin = _plot._logScaleX ? pow(10, in.xmin) : in.xmin;
		double xmax = _plot._logScaleX ? pow(10, in.xmax) : in.xmax;
		double ymin = _plot._logScaleY ? pow(10, in.ymin) : in.ymin;
		double ymax = _plot._logScaleY ? pow(10, in.ymax) : in.ymax;
		return new Bounds(xmin, xmax, ymin, ymax);
	}
	
	private Bounds logBounds(Bounds in) {
		// we use (xmin,xmax == +inf,-inf) to represent the absence of bounds;
		// taking, e.g., max(_,0) preserves this convention in log space.
		// furthermore, using max(_,0) guarantees bounds will not be NaN.
		double xmin = _plot._logScaleX ? log10(max(in.xmin,0)) : in.xmin;
		double xmax = _plot._logScaleX ? log10(max(in.xmax,0)) : in.xmax;
		double ymin = _plot._logScaleY ? log10(max(in.ymin,0)) : in.ymin;
		double ymax = _plot._logScaleY ? log10(max(in.ymax,0)) : in.ymax;
		return new Bounds(xmin, xmax, ymin, ymax);		
	}
}

