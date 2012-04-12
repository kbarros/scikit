package scikit.jobs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scikit.jobs.params.GuiValue;
import scikit.util.FileUtil;
import scikit.util.Terminal;
import scikit.util.Utilities;
import scikit.util.Window;
import bsh.EvalError;


public class Control {
	private JPanel _panel;
	private Job _job;
	private JButton _startStopButton;
	private JButton _stepButton;	
	private JButton _resetButton;
	private Movies _movies = new Movies(this);
	private List<JFrame> _frames = new ArrayList<JFrame>();
	private List<Window> _windows = new ArrayList<Window>();
	
	public Control(Simulation sim) {
		_job = new Job(sim, this);
		sim.load(this);
		
		JComponent paramPane = createParameterPane();
		JPanel buttonPanel = createButtonPanel();
		_panel = new JPanel();
		_panel.setLayout(new BorderLayout());
		_panel.add(paramPane, BorderLayout.CENTER);
		_panel.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public Control(Simulation sim, String title) {
		this(sim);
		
		JFrame frame = new JFrame(title);
		Utilities.staggerFrame(frame);
		frame.getContentPane().add(_panel);
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		frame.setJMenuBar(createMenuBar());
		frame.pack();
		frame.setVisible(true);
	}
	
	
	/**
	 * Returns the Job corresponding to this control.
	 * @return the corresponding job
	 */
	public Job getJob() {
		return _job;
	}
	
	/**
	 * Gets all simulation windows which are registered with this control.
	 * @return All simulation windows
	 */
	public Window[] getWindows() {
		return _windows.toArray(new Window[0]);
	}
	
	/**
	 * Creates frames for one or more windows. Manages each window.
	 * @param ws The windows
	 */
	public void frame(Window... ws) {
		for (Window w : ws) {
			JFrame f = Utilities.frame(w.getComponent(), w.getTitle());
			f.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			_frames.add(f);
			_windows.add(w);
		}
	}
	
	/**
	 * Creates a single frame for multiple windows. Manages each window.
	 * @param f
	 */
	public void frameTogether(String title, Window... ws) {
		JFrame f = Utilities.frameTogether(title, ws);
		f.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		_frames.add(f);
		for (Window w : ws) {
			_windows.add(w);
		}
	}
	
	/**
	 * Performs necessary processing following the completion of a simulation step.
	 */
	public void processStepCompletion() {
		_movies.saveImages();
	}
	
	/**
	 * If the simulation is running, programmatically click the "Stop" button.
	 */
	public void clickStopButton() {
		if (_startStopButton.getText().equals("Stop"))
			_startStopButton.doClick();
	}

	/**
	 * Enable "start/stop" and "step" buttons
	 */
	public void disableRunButtons() {
		_startStopButton.setEnabled(false);
		_stepButton.setEnabled(false);
	}

	
	private ActionListener _actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String str = e.getActionCommand();
			if (str.equals("Start")) {
				_job.start();
				_job.sim().params.setLocked(true);
				_startStopButton.setText("Stop");
				_resetButton.setText("Reset");
				_stepButton.setEnabled(false);
			}
			if (str.equals("Stop")) {
				_job.stop();
				_startStopButton.setText("Start");
				_stepButton.setEnabled(true);
			}
			if (str.equals("Step")) {
				_job.step();
				_job.sim().params.setLocked(true);
				_resetButton.setText("Reset");
			}
			if (str.equals("Reset")) {
				_job.kill();
				_job.sim().params.setLocked(false);
				_startStopButton.setText("Start");
				_resetButton.setText("Defaults");
				_startStopButton.setEnabled(true);
				_stepButton.setEnabled(true);
				if (_movies != null)
					_movies.removeAllMovies();
			}
			if (str.equals("Defaults")) {
				_job.sim().params.resetValues();
			}
		}
	};
	
	
	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		
		// add primary simulation execution buttons
		JPanel subPanel = new JPanel();
		JButton b1, b2, b3;
		b1 = new JButton("Start");
		b2 = new JButton("Step");
		b3 = new JButton("Defaults");
		b1.addActionListener(_actionListener);
		b2.addActionListener(_actionListener);
		b3.addActionListener(_actionListener);
		subPanel.add(b1);
		subPanel.add(b2);
		subPanel.add(b3);
		_startStopButton = b1;
		_stepButton = b2;
		_resetButton = b3;
		buttonPanel.add(subPanel, BorderLayout.CENTER);
		
		// add buttons corresponding to user defined flags 
		if (_job.sim().flags.size() > 0) {
			subPanel = new JPanel();
			for (final String s : _job.sim().flags) {
				JButton b = new JButton(s);
				b.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						_job.sim().flags.add(s);
						_job.wake();
					}
				});
				subPanel.add(b);
			}
			buttonPanel.add(subPanel, BorderLayout.SOUTH);
			_job.sim().flags.clear();
		}
		
		return buttonPanel;
	}
	
	
	private JComponent createParameterPane () {
		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		JPanel panel = new JPanel(grid);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 0;
  		c.anchor = GridBagConstraints.NORTH;
		c.gridy = 0;
		c.insets = new Insets(2, 2, 2, 2);
		
		// add parameters
		for (final String k : _job.sim().params.keys()) {
			JLabel label = new JLabel(k + ":", SwingConstants.RIGHT);
			c.gridx = 0;
			c.weightx = 0;
			c.gridwidth = 1;
			grid.setConstraints(label, c);
			panel.add(label);
			
			// add primary editor for parameter
			GuiValue v = _job.sim().params.getValue(k);
			JComponent editor = v.getEditor();
			c.gridx = 1;
			c.weightx = 1;
			c.gridwidth = GridBagConstraints.REMAINDER;
			grid.setConstraints(editor, c);
			panel.add(editor);
			
			// possible add auxiliary editor
			JComponent editorAux = v.getAuxiliaryEditor();
			if (editorAux != null) {
				c.gridy++;
				grid.setConstraints(editorAux, c);
				panel.add(editorAux);
			}
			
			// wake job when parameter value has changed
			v.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					_job.wake();
				}
			});
			
			c.gridy++;
		}
		
		// if the control is too large to fit on the screen, it is useful to have
		// a scroll pane. the disadvantage is that you don't always want the scroll
		// pane to appear when minimizing the control
		return new JScrollPane(panel);
	}
	
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());
		menuBar.add(createSimulationMenu());
		return menuBar;
	}
	
	private JMenu createFileMenu() {
		JMenuItem terminalItem = new JMenuItem("New Terminal");
		terminalItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createConsole();
			}
		});
		
		JMenuItem openParamsItem = new JMenuItem("Open Params");
		openParamsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openParams();
			}
		});
		JMenuItem saveParamsItem = new JMenuItem("Save Params");
		saveParamsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveParams();
			}
		});
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(terminalItem);
		fileMenu.add(_movies.menu);
		fileMenu.add(new JSeparator());
		fileMenu.add(openParamsItem);
		fileMenu.add(saveParamsItem);
		return fileMenu;
	}
	
	private JMenu createSimulationMenu() {
		JMenu simulationMenu = new JMenu("Simulation");
		for (final JFrame f : _frames) {
			JMenuItem item = new JMenuItem(f.getTitle()+" Display");
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					f.setVisible(true);
				}
			});
			simulationMenu.add(item);
		}
		
		if (_frames.size() > 0) {
			simulationMenu.addSeparator();
		}
		
		JMenu throttleMenu = new JMenu("Speed Throttle");
		ButtonGroup throttleGroup = new ButtonGroup();
		JMenuItem throttleOnItem = new JRadioButtonMenuItem("On");
		JMenuItem throttleOffItem = new JRadioButtonMenuItem("Off");
		throttleOnItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_job.throttleAnimation(true);
			}
		});
		throttleOffItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_job.throttleAnimation(false);
			}
		});
		throttleOnItem.setSelected(false);
		throttleOffItem.setSelected(true);
		throttleGroup.add(throttleOnItem);
		throttleGroup.add(throttleOffItem);
		throttleMenu.add(throttleOnItem);
		throttleMenu.add(throttleOffItem);
		
		simulationMenu.add(throttleMenu);
		
		return simulationMenu;
	}
	
	private void saveParams() {
		try {
			String fname = FileUtil.saveDialog(_panel, "params.txt");
			if (fname != null) {
				PrintWriter pw = FileUtil.pwFromString(fname);
				pw.write(_job.sim().params.toString());
				pw.close();
			}
		} catch (IOException exc) {}
	}
	
	private void openParams() {
		System.out.println("open params");
	}
	
	private void createConsole() {
		Terminal term = new Terminal();
		Utilities.frame(term.getConsole(), "Console");
		try {
			term.getInterpreter().set("sim", _job.sim());
		} catch (EvalError exc) {
			System.err.println("Beanshell evaluation error.");
		}
	}
}
