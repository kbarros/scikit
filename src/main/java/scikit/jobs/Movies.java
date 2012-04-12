package scikit.jobs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import scikit.jobs.params.ChoiceValue;
import scikit.jobs.params.DirectoryValue;
import scikit.jobs.params.DoubleValue;
import scikit.jobs.params.IntValue;
import scikit.util.Utilities;
import scikit.util.Window;

public class Movies {
	JMenu menu = new JMenu("Movies");
	List<MovieConfig> movies = new ArrayList<MovieConfig>();
	Control control;
	
	public Movies(Control c) {
		this.control = c;
		rebuildMenu();
	}
	
	public void saveImages() {
		for (MovieConfig mc : movies) {
			mc.saveImage();
		}
	}
	
	public void removeAllMovies() {
		movies.clear();
		rebuildMenu();
	}
	
	private void rebuildMenu() {
		menu.removeAll();
		
		if (movies.size() > 0) {
			for (final MovieConfig mc : movies) {
				JMenuItem item = new JMenuItem("Edit '" + mc.window.getValue() + "'...");
				item.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						mc.editor.setVisible(true);
					}
				});
				menu.add(item);
			}
			menu.addSeparator();
		}
		
		JMenuItem createItem = new JMenuItem("New Movie...");
		createItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		    	MovieConfig mc = new MovieConfig();
				movies.add(mc);
				mc.editor.setVisible(true);
			}
		});
		menu.add(createItem);
		
		if (movies.size() > 0) {
			JMenuItem clearItem = new JMenuItem("Clear Movies");
			clearItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeAllMovies();
				}
			});
			menu.add(clearItem);
		}
	}
	
	private String[] getWindowTitles() {
		Window[] windows = control.getWindows();
		List<String> titles = new ArrayList<String>();
		titles.add(" - ");
		for (Window w : windows)
			titles.add(w.getTitle());
		return titles.toArray(new String[0]);
	}
	
	private Window getWindowFromTitle(String title) {
		Window[] windows = control.getWindows();
		for (Window w : windows)
			if (w.getTitle().equals(title))
				return w;
		return null;
	}
	
	
	private class MovieConfig {
		DecimalFormat fmt = new DecimalFormat("0000");
		int saveCount = 0;
		double lastSaveTime = Double.NEGATIVE_INFINITY;
		
		ChoiceValue window;
		DirectoryValue directory;
		IntValue width, height;
		DoubleValue startTime, endTime, delayTime;
		JFrame editor;
		
		public MovieConfig() {
			window = new ChoiceValue(getWindowTitles());
			directory = new DirectoryValue();
			width = new IntValue(300);
			height = new IntValue(300);
			startTime = new DoubleValue(0);
			endTime = new DoubleValue(0);
			delayTime = new DoubleValue(0);
			
			editor = createEditor();
		}
		
		
		private JFrame createEditor() {
			final JFrame editor = new JFrame("Movie Capture");		
	     	
	    	JButton removeButton = new JButton("Remove Movie");
	    	removeButton.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			movies.remove(MovieConfig.this);
	    			rebuildMenu();
	    			editor.setVisible(false);
	    		}
		    });
	    	removeButton.setEnabled(true);
	    	
	    	JButton acceptButton = new JButton("Ok");
	    	acceptButton.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			rebuildMenu();
	    			editor.setVisible(false);
	    		}
		    });
	    	acceptButton.setEnabled(true);
	    	
	    	JPanel buttonPane = new JPanel();
	     	buttonPane.add(removeButton);
	    	buttonPane.add(acceptButton);

			GridLayout gl = new GridLayout(7, 2);
			gl.setHgap(4);
			gl.setVgap(4);
			JPanel options = new JPanel();
			options.setLayout(gl);
			options.add(new JLabel("Window to capture:"));
			options.add(window.getEditor());
			options.add(new JLabel("Output directory:"));
			options.add(directory.getEditor());
			options.add(new JLabel("Image width (pixels):"));
			options.add(width.getEditor());
			options.add(new JLabel("Image height (pixels):"));
			options.add(height.getEditor());
			options.add(new JLabel("Start time:"));
			options.add(startTime.getEditor());
			options.add(new JLabel("End time:"));
			options.add(endTime.getEditor());
			options.add(new JLabel("Capture delay time:"));
			options.add(delayTime.getEditor());

			JPanel panel = new JPanel(new BorderLayout());
			panel.setOpaque(true);
	        panel.add(options, BorderLayout.CENTER);
	        panel.add(buttonPane, BorderLayout.PAGE_END);
	    	panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

			editor.getContentPane().add(panel);
	        editor.getRootPane().setDefaultButton(acceptButton);
			editor.pack();
			Utilities.staggerFrame(editor);
			Utilities.addFrameShortcuts(editor);
			
			return editor;
		}
		
		public void saveImage() {
			double time = control.getJob().sim().getTime();
			if (isValid(time)) {
				int w = width.getInt();
				int h = height.getInt();
				Window win = getWindowFromTitle(window.getValue());
				String dir = directory.getValue();
				File file = new File(dir+File.separator+fmt.format(saveCount)+".png");
				try {
					 ImageIO.write(win.getImage(w, h), "png", file);
				} catch (IOException e) {
				}
				lastSaveTime = time;
				saveCount += 1;
			}
		}

		private boolean isValid(double time) {
			return
				getWindowFromTitle(window.getValue()) != null &&
				new File(directory.getValue()).isDirectory() &&
				width.getInt() > 0 &&
				height.getInt() > 0 &&
				Double.isNaN(time) ||
				(time >= startTime.getDouble() &&
				time < endTime.getDouble() &&
				time - lastSaveTime > delayTime.getDouble());
		}
	}
}
