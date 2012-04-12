package scikit.util;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;


public class FileUtil {
	
	public static File directoryDialog(String fname) throws IOException {
		JFileChooser chooser = new JFileChooser(fname);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.showDialog(null, "Select Directory");
		File dir = chooser.getSelectedFile();
		if (dir == null || dir.isDirectory())
			return dir;
		else
			return dir.getParentFile();
	}
	
	public static String fileDialog(Component comp, String fname, String title, int type) throws IOException {
		for (; comp != null; comp = comp.getParent()) {
			if (comp instanceof Frame) {
				FileDialog d = new FileDialog((Frame)comp, title, type);
				d.setFile(fname);
				d.setVisible(true);
				String file = d.getFile();
				String dir = d.getDirectory();
				return file == null ? null : dir+file;
			}
		}
		throw new IOException();
	}
	
	public static String saveDialog(Component comp, String fname) throws IOException {
		return fileDialog(comp, fname, "Save", FileDialog.SAVE);
	}
	
	public static String loadDialog(Component comp, String fname) throws IOException {
		return fileDialog(comp, fname, "Load", FileDialog.LOAD);
	}
	
	/** 
	 * Gets an empty directory, creating it if necessary. The first choice for the
	 * pathname is "parent/dir" but if this directory already exists and is not empty,
	 * then a variation on the pathname will be chosen.
	 *  
	 * @param parent
	 * @param dir
	 * @return The empty directory
	 */
	public static File getEmptyDirectory(String parent, String dir) {
		if (!new File(parent).isDirectory())
			throw new IllegalArgumentException();
		String sep = File.separator;
		File target = new File(parent+sep+dir);
		int cnt = 1;
		while ((target.isDirectory() && target.list().length > 0) || target.isFile()) {
			target = new File(parent+sep+dir+"-"+cnt);
			cnt++;
		}
		if (!target.isDirectory()) {
			target.mkdir();
		}
		return target;
	}
	
	public static PrintWriter pwFromString(String fname) throws IOException {
		return new PrintWriter(new FileWriter(new File(fname)));				
	}
	
	public static BufferedReader brFromString(String fname) throws IOException {
		return new BufferedReader(new FileReader(new File(fname)));
	}
	
	public static DataOutputStream dosFromString(String fname) throws IOException {
		return new DataOutputStream(new FileOutputStream(new File(fname)));	
	}
	
	public static DataInputStream disFromString(String fname) throws IOException {
		return new DataInputStream(new FileInputStream(new File(fname)));	
	}
	
	public static double readDoubleLittleEndian(DataInput dis) throws IOException {
		long accum = 0;
		for (int shiftBy=0; shiftBy<64; shiftBy+=8) {
			// must cast to long or shift done modulo 32
			accum |= ((long)(dis.readByte() & 0xff)) << shiftBy;
		}
		return Double.longBitsToDouble(accum);
	}

	public static float readFloatLittleEndian(DataInput dis) throws IOException {
		int accum = 0;
		for (int shiftBy=0; shiftBy<32; shiftBy+=8) {
			// must cast to long or shift done modulo 32
			accum |= (dis.readByte() & 0xff ) << shiftBy;
		}
		return Float.intBitsToFloat(accum);
	}

	public static int readIntLittleEndian(DataInput dis) throws IOException {
		return Integer.reverseBytes(dis.readInt());
	}

	public static long readLongLittleEndian(DataInput dis) throws IOException {
		return Long.reverseBytes(dis.readLong());
	}
	
	public static void writeOctaveGrid(PrintWriter pw, double[] data, int cols, double dx) throws IOException {
    	if (cols < 1)
    		throw new IllegalArgumentException();
		pw.println("#name: dx\n#type: scalar");
		pw.println(dx);
		pw.println("#name: grid\n#type: matrix");
		pw.println("#rows: "+data.length/cols);
		pw.println("#columns: "+cols);		
		writeColumns(pw, data, cols);
	}
	
	
    public static void writeColumns(PrintWriter pw, double[]... data) throws IOException {
    	if (data.length == 0)
    		throw new IllegalArgumentException();
		for (int i = 0; i < data[0].length; i++) {
			for (int j = 0; j < data.length; j++) {
				pw.print(data[j][i] + " ");
			}
			pw.println();
		}
    }
    
    public static void writeColumns(PrintWriter pw, double[] data, int cols) throws IOException {
    	if (cols < 1)
    		throw new IllegalArgumentException();
		for (int i = 0; i < data.length; i++) {
			pw.print(data[i]);
			if ((i+1) % cols == 0)
				pw.println();
			else
				pw.print(' ');
		}
    }
    
    public static void dumpString(String fname, String str) {
    	try {
    		PrintWriter pw = pwFromString(fname);
    		pw.write(str);
    		pw.close();
    	}
    	catch (IOException e) {
    		System.out.println(e.toString());
    	}
    }
    
    public static void dumpColumns(String fname, double[]... cols) {
    	try {
    		PrintWriter pw = pwFromString(fname);   		
    		writeColumns(pw, cols);
    		pw.close();
    	}
    	catch (IOException e) {
    		System.out.println(e.toString());
    	}
    }
}
