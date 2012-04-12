package scikit.graphics;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;

public class GLHelper {
	public static GLJPanel createComponent(GLEventListener listener) {
		GLCapabilities capabilities = new GLCapabilities();
		// For some unknown reason, enabling GL "sample buffers" actually make anti-aliased lines
		// look much worse on OS X. I guess it disables custom anti-aliasing code. It's better
		// to use only glEnable(GL.GL_LINE_SMOOTH).
		//
		// capabilities.setSampleBuffers(true);
		// capabilities.setNumSamples(4);
		
		GLJPanel canvas = new GLJPanel(capabilities);
		canvas.addGLEventListener(listener);
		return canvas;
	}
	
	// if GL available:
	//   - returns true/false if GL is accelerated/unaccelerated
	// else
	//   - throw class load exception
	public static boolean testGL() {
		// construction of GLCanvas will throw a class-load error if native libraries are unavailable
		new GLCanvas();
		return new GLCapabilities().getHardwareAccelerated();
	}

}
