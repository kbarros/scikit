package scikit.util;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;


public class Utilities {
	public final static int OPTIMAL_FRAME_SIZE = 300;

	static DecimalFormat df1 = new DecimalFormat("0.#######");
	static DecimalFormat df2 = new DecimalFormat("0.#######E0");
	static public String format(double x) {
		return (abs(x) > 0.0001 && abs(x) < 10000 || x == 0 ? df1 : df2).format(x);
	}
	static DecimalFormat if4 = new DecimalFormat("0000");
	static public String formatI4(double x) {
		return if4.format(x);
	}
	
	public static double periodicOffset(double L, double dx) {
		if (dx >  L/2) return dx-L;
		if (dx < -L/2) return dx+L;
		return dx;
	}
	
	public static int[] integerSequence(int n) {
		int ret[] = new int[n];
		for (int i = 0; i < n; i++)
			ret[i] = i;
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> asList(T o1) {
		return Arrays.asList(o1);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> asList(T o1, T o2) {
		return Arrays.asList(o1, o2);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> asList(T o1, T o2, T o3) {
		return Arrays.asList(o1, o2, o3);
	}
	
	
	private static int _framePosition = 100;
	
	public static void staggerFrame(JFrame frame) {
		frame.setLocation(_framePosition, _framePosition);
		_framePosition += 24;
	}
	
	public static void addFrameShortcuts(final JFrame frame) {
	      JRootPane rootPane = frame.getRootPane();
	      int menuShortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	      KeyStroke w = KeyStroke.getKeyStroke(KeyEvent.VK_W, menuShortcutKey);
	      ActionListener listener = new ActionListener() {
	    	  public void actionPerformed(ActionEvent e) {
	    		  if("ACTION_CLOSE".equals(e.getActionCommand())) {
	    			  frame.setVisible(false);
	    		  }
	    	  }
	      };
	      rootPane.registerKeyboardAction(listener, "ACTION_CLOSE", w, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	
	public static JFrame frame(Component comp, String title) {
		JFrame frame = new JFrame(title);
		staggerFrame(frame);
		addFrameShortcuts(frame);
		frame.getContentPane().add(comp);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		return frame;
	}
	
	public static void frame(Window... ws) {
		for (Window w : ws)
			frame(w.getComponent(), w.getTitle());
	}
	
	public static JFrame frameTogether(String title, Window... ws) {
		int n = ws.length;
		int cols = (int)ceil(sqrt(n));
		int rows = (int)ceil((double)n/cols);
		int hgap = 2, vgap = 2;
		JPanel panel = new JPanel(new GridLayout(rows, cols, hgap, vgap));
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		for (Window w : ws) {
			JPanel item = new JPanel(new BorderLayout());
			item.setBorder(BorderFactory.createTitledBorder(w.getTitle()));
			item.add(w.getComponent());
			panel.add(item);
		}
		// adjust panel's preferred size to be closer to OPTIMAL_FRAME_SIZE
		Dimension d = panel.getPreferredSize();
		double w = d.getWidth(), h = d.getHeight();
		double opt = OPTIMAL_FRAME_SIZE;
		if (max(w, h) > opt) {
			double scale = (0.4*(max(w,h) - opt) + opt) / max(w,h);
			panel.setPreferredSize(new Dimension((int)(w*scale), (int)(h*scale)));
		}
		return frame(panel, title);
	}

	
	/**
	 * Returns true if the component is showing. If the component can be detected
	 * as iconified (e.g. minimized to the dock), then the component is not considered
	 * to be showing.
	 * @param c the component to test
	 * @return true if the component is showing
	 */
	public static boolean isComponentShowing(Component c) {
		if (c.isShowing()) {
			Component p = SwingUtilities.getRoot(c);
			if (p instanceof Frame) {
				int state = ((Frame)p).getExtendedState();
				boolean iconified = (state & Frame.ICONIFIED) == Frame.ICONIFIED;
				return !iconified;
			}
			else {
				return true;
			}
		}
		else {
			return false;
		}
	}
	
/*
import javax.imageio.ImageIO;
im = sim.heatPlot.getImage(500,200);
ImageIO.write(im, "png", new File("/home/kbarros/test.png"));
*/
	public static BufferedImage captureJComponentImage(JComponent component, int width, int height) {
		boolean oldOpaque = component.isOpaque();
		int oldWidth = component.getWidth();
		int oldHeight = component.getHeight();
		component.setOpaque(true);
		component.setSize(width, height);
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		component.paint(g2d);
		g2d.dispose();
		
		component.setOpaque(oldOpaque);
		component.setSize(oldWidth, oldHeight);
		return image;
	}
}
