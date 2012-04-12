package scikit.jobs.params;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DirectoryValue extends GuiValue {
	JFileChooser chooser = new JFileChooser();
	
	public DirectoryValue() {
		this(null);
	}
	
	public DirectoryValue(String v) {
		super(defaultDirectory(v));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(getValue()));
	}

	protected boolean testValidity(String v) {
		return (new File(v)).isDirectory();
	}
	
	protected JComponent createEditor() {
		final JButton b = new JButton(getValue());
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (chooser.showDialog(null, "Select") == JFileChooser.APPROVE_OPTION) {
					File dir = chooser.getSelectedFile();
					if (dir != null) {
						if (testValidity(dir.toString()))
							setValue(dir.toString());
						else
							setValue(dir.getParent().toString()); // JFileChooser bug workaround
					}
				}
			}
		});
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				chooser.setCurrentDirectory(new File(getValue()));
				b.setText(getValue());
			}
		});
		b.setPreferredSize(new Dimension(250, b.getPreferredSize().height));
		return b;
	}
	
	private static String defaultDirectory(String v) {
		if (v != null && (new File(v)).isDirectory())
			return v;
		else
			return System.getProperty("user.home");
	}
}
