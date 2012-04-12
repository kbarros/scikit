package scikit.graphics;

import static scikit.util.Utilities.OPTIMAL_FRAME_SIZE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import scikit.util.Bounds;
import scikit.util.Utilities;
import scikit.util.Window;


abstract public class Scene<T> implements Window {
	protected JComponent _component; // contains scene and possible other GUI objects
	protected JComponent _canvas;    // the canvas on which scene is drawn
	protected List<Drawable<T>> _drawables = new ArrayList<Drawable<T>>();
	protected JPopupMenu _popup = new JPopupMenu();
	protected Timer _animateTimer = new Timer(50, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			animate();
			System.out.println("timeout!");
		}
	});
	// if true, suppress inclusion of _drawables in the return value of getAllDrawables()
	protected boolean _suppressDrawables = false;

	private String _title;
	
	public Scene(String title) {
		_canvas = createCanvas();
		_canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) { maybeShowPopup(e); }
			public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
		});
		_component = createComponent(_canvas);
		_component.setPreferredSize(new Dimension(OPTIMAL_FRAME_SIZE, OPTIMAL_FRAME_SIZE));
		_title = title;
	}
	
	public String getTitle() {
		return _title;
	}

	/**
	 * Gets the GUI component object for this scene 
	 */
	public JComponent getComponent() {
		return _component;
	}
	
	/** Removes all drawables object from the scene leaving the state of the scene (such as
	 * view bounds) unmodified.
	 */
	public void clearDrawables() {
		_drawables.clear();
		animate();
	}
	
	/** Adds drawables objects to the scene. */
	public void addDrawable(Drawable<T> drawable) {
		_drawables.add(drawable);
		animate();
	}
	
	/** Sets the scene's drawable objects to be the specified list. */
	public void setDrawables(List<Drawable<T>> drawables) {
		_drawables.clear();
		_drawables.addAll(drawables);
		animate();
	}
	
	/** Animates the scene. This is to be called periodically. Repaints the canvas component and
	 * possibly performs other tasks with low computational cost.
	 */
	public void animate() {
		_canvas.repaint();
	}
	
	/** Completely clears the scene to it's initial state by removing all drawables and
	 * resetting the view bounds. */
	public void clear() {
		_drawables.clear();
		_canvas.repaint();
	}
	
	/**
	 * Returns the minimal bounds which contains all data in the scene.
	 * @return the data bounds
	 */
	public Bounds dataBounds() {
		Bounds bounds = new Bounds();
		for (Drawable<T> d : getAllDrawables())
			bounds = (Bounds)bounds.union(d.getBounds());
		return bounds;
	}
	
	/**
	 * Capture the image which is to be displayed on the canvas object.
	 * @return the canvas image
	 */
	public BufferedImage getImage(int width, int height) {
		return Utilities.captureJComponentImage(_canvas, width, height);
	}

	/**
	 * Returns the portion of the scene volume which is currently being viewed.
	 * @return the view bounds
	 */
	abstract public Bounds viewBounds();
	
	/**
	 * Draws all objects in the scene
	 * @param g the graphics engine
	 */
	abstract protected void drawAll(T g);
	
	/**
	 * Creates the canvas GUI component on which the scene will be drawn.  This object
	 * may display a pop-up menu when requested.
	 * @return the canvas GUI component
	 */
	abstract protected JComponent createCanvas();
	
	/**
	 * Creates a wrapper around the canvas component which may contain additional GUI
	 * content.
	 * @param canvas
	 * @return a component which wraps the canvas object
	 */
	protected JComponent createComponent(JComponent canvas) {
		JPanel component = new JPanel(new BorderLayout());
		component.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.GRAY)));
		component.add(canvas);
		return component;
	}
	
	/**
	 * Gets a list of all drawable objects contained in the scene.
	 * @return the list of all drawable objects
	 */
	protected List<Drawable<T>> getAllDrawables() {
		return _suppressDrawables ? new ArrayList<Drawable<T>>() : _drawables;
	}
	
	/**
	 * Gets all menu items to be included when the user opens a popup menu from the GUI canvas
	 * @return the list of popup menu items
	 */
	protected List<JMenuItem> getAllPopupMenuItems() {
		return new ArrayList<JMenuItem>();
	}
	
	/**
	 * Starts the timer thread which periodically calls the animate() method.
	 */
	protected void startAnimateTimer() {
		if (!_animateTimer.isRunning()) {
			_animateTimer.start();
		}
	}
	
	/**
	 * Stops the timer thread which periodically calls the animate() method.
	 */
	protected void stopAnimateTimer() {
		if (_animateTimer.isRunning())
			_animateTimer.stop();
	}
	
	private void maybeShowPopup(MouseEvent e) {
		List<JMenuItem> items = getAllPopupMenuItems();
		if (e.isPopupTrigger() && items.size() > 0) {
			_popup.removeAll();
			for (JMenuItem item : items)
				_popup.add(item);
			_popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
