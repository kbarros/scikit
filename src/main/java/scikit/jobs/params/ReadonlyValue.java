package scikit.jobs.params;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;


public class ReadonlyValue extends GuiValue {
	public ReadonlyValue() {
		super("-");
	}
	
	public void setLocked(boolean locked) {
		// do nothing
	}
	
	protected JComponent createEditor() {
		final JLabel label = new JLabel(getValue(), SwingConstants.RIGHT);
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) { label.setText(getValue()); }
		});
		Dimension d = label.getPreferredSize();
		d.width = Math.max(d.width, 80);
		label.setPreferredSize(d);
		return label;
	}
}
