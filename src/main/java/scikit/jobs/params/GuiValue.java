package scikit.jobs.params;

import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;


abstract public class GuiValue {
	private String _v, _default;
	private boolean _lockable = true;
	private JComponent _editor, _editorAux; 
	// change listeners will be called whenever the value changes. they will always
	// be run from the event dispatch thread
	private Vector<ChangeListener> _listeners = new Vector<ChangeListener>();
	
	
	public GuiValue(Object v) {
		_v = _default = v.toString();
	}
	
	public JComponent getEditor() {
		if (_editor == null)
			_editor = createEditor();
		return _editor;
	}
	
	public JComponent getAuxiliaryEditor() {
		if (_editorAux == null)
			_editorAux = createAuxiliaryEditor();
		return _editorAux;
	}
	
	public void addChangeListener(ChangeListener listener) {
		_listeners.add(listener);
	}
	
	public void setLocked(boolean locked) {
		if (_lockable) {
			if (getEditor() != null)
				getEditor().setEnabled(!locked);
			if (getAuxiliaryEditor() != null)
				getAuxiliaryEditor().setEnabled(!locked);
		}
	}
	
	public void setLockable(boolean lockable) {
		_lockable = lockable;
	}
	
	public void resetValue() {
		setValue(_default);
	}
	
	synchronized public void setValue(String v) {
		if (!_v.equals(v) && testValidity(v)) {
			_v = v;
			invokeFromEventDispatchThread(new Runnable() {
				public void run() {
					for (ChangeListener l : _listeners)
						l.stateChanged(null);
				}
			});
		}
	}
	
	synchronized public String getValue() {
		return _v;
	}
		
	private void invokeFromEventDispatchThread(Runnable r) {
		if (SwingUtilities.isEventDispatchThread())
			r.run();
		else
			SwingUtilities.invokeLater(r);
	}
	
	// -----------------------------------------------------------------------------------------
	// to be implemented by subclass
	
	protected boolean testValidity(String v) {
		return true;
	}
	
	abstract protected JComponent createEditor();
	
	protected JComponent createAuxiliaryEditor() {
		return null;
	}
}	

