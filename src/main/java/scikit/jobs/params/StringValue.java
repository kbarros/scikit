package scikit.jobs.params;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;



public class StringValue extends GuiValue {
	final private Color lightGreen = new Color(0.85f, 1f, 0.7f);
	final private Color lightRed   = new Color(1f, 0.7f, 0.7f);

	public StringValue(Object v) {
		super(v);
	}

	protected JComponent createEditor() {
		final JTextField field = new JTextField(getValue());
		
		field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { fieldTextEvaluated(field); }
		});
		
		field.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e)  {}
			public void focusLost(FocusEvent e) { fieldTextEvaluated(field); }
		});
		
		field.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e)  {}
			public void insertUpdate(DocumentEvent e)  { fieldTextInput(field); }
			public void removeUpdate(DocumentEvent e) { fieldTextInput(field); }
		});
		
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				String s = getValue();
				if (!field.getText().equals(s))
					field.setText(s);
			}
		});
		
		Dimension d = field.getPreferredSize();
		d.width = Math.max(d.width, 80);
		field.setPreferredSize(d);
		field.setHorizontalAlignment(JTextField.RIGHT);
		return field;
	}
	
	
	// called when the user has entered a final string value
	private void fieldTextEvaluated(JTextField field) {
		setValue(field.getText());
		field.setText(getValue()); // if entered value was invalid, update field to it's previous value
		field.setBackground(Color.WHITE);	
	}
	
	// called while the user is inputting the string value
	private void fieldTextInput(JTextField field) {
		if (field.getText().equals(getValue()))
			field.setBackground(Color.WHITE);
		else
			field.setBackground(testValidity(field.getText()) ? lightGreen : lightRed);
	}
}
