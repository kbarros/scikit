package scikit.graphics.dim3;

import static java.lang.Math.sqrt;
import static scikit.numerics.Math2.sqr;

import java.awt.Color;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.sun.opengl.util.GLUT;

import scikit.numerics.vecmath.Quat4d;
import scikit.numerics.vecmath.VecHelper;
import scikit.util.Bounds;
import scikit.util.Point;

public class Gfx3D {
	private final GL _gl;
	private final GLU _glu = new GLU();
	private final GLUT _glut = new GLUT();
	private final GLUquadric _gluq = _glu.gluNewQuadric();
	private final Bounds _pixBds, _viewBds;
	private final Quat4d _rotation; 
	
	private static int FONT = GLUT.BITMAP_8_BY_13;
	private static int FONT_HEIGHT = 13; // pixels
	
	
	public Gfx3D(GLAutoDrawable glDrawable, Scene3D scene) {
		this._gl = glDrawable.getGL();
		_pixBds = new Bounds(0, glDrawable.getWidth(), 0, glDrawable.getHeight());
		_viewBds = scene.viewBounds();
		_rotation = scene.getRotation();
	}
	
	public GL getGL() {
		return _gl;
	}
	
	public Bounds pixelBounds() {
		return _pixBds;
	}
	
	public Bounds viewBounds() {
		return _viewBds;
	}
	
	public Quat4d rotation() {
		return _rotation;
	}
	
	public void ortho2D(Bounds bds) {
		_gl.glDisable(GL.GL_DEPTH_TEST);
		_gl.glDisable(GL.GL_COLOR_MATERIAL);
		_gl.glDisable(GL.GL_LIGHTING);
		
		// set the projection & modelview matrices
		_gl.glMatrixMode(GL.GL_PROJECTION);
		_gl.glLoadIdentity();
		_glu.gluOrtho2D(bds.xmin, bds.xmax, bds.ymin, bds.ymax);
		_gl.glMatrixMode(GL.GL_MODELVIEW);
		_gl.glLoadIdentity();
	}
	
	public void perspective3D(Bounds bds) {
		_gl.glEnable(GL.GL_DEPTH_TEST);
		_gl.glEnable(GL.GL_COLOR_MATERIAL);
		_gl.glEnable(GL.GL_LIGHTING);
		_gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
		
		// get the corner to corner distance of the view bounds cuboid
		double len = sqrt(sqr(_viewBds.getWidth())+sqr(_viewBds.getHeight())+sqr(_viewBds.getDepth()));
		
		// set the projection matrix
		_gl.glMatrixMode(GL.GL_PROJECTION);
		_gl.glLoadIdentity();
		double fovY = 35;
		double aspect = (double)_pixBds.getWidth() / _pixBds.getHeight();
		double zNear = 0.1*len;
		double zFar = 10*len;
		_glu.gluPerspective(fovY, aspect, zNear, zFar);
		
		// set the modelview matrix
		_gl.glMatrixMode(GL.GL_MODELVIEW);
		_gl.glLoadIdentity();
		initializeLights();
		// each sequential operation multiplies the modelview transformation matrix
		// from the left. operations on the scene object occur in reverse order from
		// their specification.
		// step (3): move object away from camera
		_gl.glTranslated(0, 0, -1.5*len);
		// step (2): rotate object about zero
		_gl.glMultMatrixd(VecHelper.getGLMatrix(_rotation), 0);
		// step (1): move object to its center
		Point center = _viewBds.getCenter();
		_gl.glTranslated(-center.x, -center.y, -center.z);
	}
	
	public void setColor(Color color) {
		_gl.glColor4fv(color.getComponents(null), 0);
	}
	
	public void drawCuboid(Bounds bds) {
		_gl.glDisable(GL.GL_LIGHTING);
		_gl.glBegin(GL.GL_LINES);
		double[] xs = {bds.xmin, bds.xmax};
		double[] ys = {bds.ymin, bds.ymax};
		double[] zs = {bds.zmin, bds.zmax};
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 2; k++) {
					if ((i + j + k) % 2 == 0) {
						_gl.glVertex3d(xs[i],   ys[j],   zs[k]);
						_gl.glVertex3d(xs[1-i], ys[j],   zs[k]);
						_gl.glVertex3d(xs[i],   ys[j],   zs[k]);
						_gl.glVertex3d(xs[i],   ys[1-j], zs[k]);
						_gl.glVertex3d(xs[i],   ys[j],   zs[k]);
						_gl.glVertex3d(xs[i],   ys[j],   zs[1-k]);
					}
				}
			}
		}
		_gl.glEnd();
		_gl.glEnable(GL.GL_LIGHTING);
	}
	
	public void drawSphere(Point center, double radius) {
		_gl.glPushMatrix();
		_gl.glTranslated(center.x, center.y, center.z);
		_glu.gluSphere(_gluq, radius, 8, 8);
		_gl.glPopMatrix();
	}
	

	public double stringWidth(String str) {
		return _glut.glutBitmapLength(FONT, str);
	}
	
	public double stringHeight(String str) {
		return FONT_HEIGHT;
	}
	
	public void rasterString(String str, double x, double y) {
		_gl.glPushMatrix();
		_gl.glRasterPos2d(x, y);
		_glut.glutBitmapString(FONT, str); 
		_gl.glPopMatrix();
	}
	
	private void initializeLights() {
		_gl.glEnable(GL.GL_LIGHT1);
		_gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, new float[]{0.2f,0.2f,0.2f,0.2f}, 0);
		_gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, new float[]{0.9f,0.9f,0.9f,0.9f}, 0);
		_gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, new float[]{1,0.5f,1,0}, 0);
	}
}
