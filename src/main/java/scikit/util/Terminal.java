package scikit.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import bsh.Capabilities;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;
import bsh.Capabilities.Unavailable;
import bsh.util.JConsole;

public class Terminal {
	private Interpreter interpreter;
	private JConsole console;
	@SuppressWarnings("unused")
	private Object banner = new Object() {
		public void printBanner() {
			console.print(
					"SciKit",
					new Font("SansSerif", Font.BOLD, 12), 
					new Color(20,100,20));
			console.print(" scikit.googlecode.com\n",
					new Color(20,20,100));
			console.print("Type 'help();' for usage.\n\n",
					new Font("Monaco", Font.ITALIC, 8));
		}
		
		public void help() {
			println(help);
		}
		
		public void source() {
			String fname;
			try {
				fname = FileUtil.loadDialog(console, "Load Beanshell Source");
				if (fname != null)
					interpreter.source(fname);
			}
			catch (IOException e) { e.printStackTrace(); }
			catch (EvalError e) { e.printStackTrace(); }
		}
	};
	
	public String help = "Based on the BeanShell scripting language.\n  www.beanshell.org";
	
	
	public Terminal() {
		console = new JConsole();
		console.setPreferredSize(new Dimension(800, 600));
		interpreter = new Interpreter(console);
		interpreter.setShowResults(true);
		NameSpace namespace = interpreter.getNameSpace();
		namespace.importStatic(Math.class);
		namespace.importStatic(scikit.numerics.Math2.class);
		namespace.importStatic(scikit.util.DoubleArray.class);
		namespace.importStatic(Commands.class);
		namespace.importObject(banner);
		new Thread(interpreter).start();
		try {
			Capabilities.setAccessibility(true);
		} catch (Unavailable exc) {
			System.err.print("Beanshell reflection not available.");
		}
	}
	
	public void print(Object obj) {
		console.print(obj, new Color(20,20,100));
	}
	
	public void println(Object obj) {
		print(obj + "\n");
	}
	
	public void importObject(Object o) {
		interpreter.getNameSpace().importObject(o);
	}
	
	public JConsole getConsole() {
		return console;
	}
	
	public Interpreter getInterpreter() {
		return interpreter;
	}
	
	public void runApplication() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = Utilities.frame(console, "Console");
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			}
		});
	}
	
	public static void main(String[] args) {
		new Terminal().runApplication();
	}
}
