package scikit.graphics.dim3;

import static java.lang.Math.rint;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import scikit.numerics.vecmath.Quat4d;
import scikit.numerics.vecmath.VecHelper;
import scikit.numerics.vecmath.Vector3d;


public class Grid3DSliceView extends Grid3DView {
	public int wallResolution = 128;
	public int sliceResolution = 256;

	private final int CUBE_SIDES=6;
	private final int PANELS=7;
	
	private Quat4d _rotation = new Quat4d(0, 0, 0, 1);
	private Grid3D _grid;
	private int[] _dim;
	
	public Grid3DSliceView(Grid3D grid) {
		_grid = grid;
	}
	
	public void rotateStructure(Quat4d q) {
		_rotation.mul(q, _rotation);
		_rotation.normalize();
	}

	public void draw(Gfx3D g) {
		_dim = _grid.getDimensions();
		g.setColor(Color.WHITE);
		
		GL2 gl = g.getGL().getGL2();
		gl.glEnable(GL.GL_TEXTURE_2D);
		int[] textures = buildTextures(g);
		
		for (int side = 0; side < CUBE_SIDES; side++) {
			Polygon p = new Polygon(
					_panel[side][0], _panel[side][1], _panel[side][2], _panel[side][3]);
			Vector3d n = new Vector3d(0, 0, 1);
			VecHelper.rotate(_rotation, n);
			p.intersect(n, getDepth());
			gl.glBindTexture(GL.GL_TEXTURE_2D, textures[side]);
			p.draw(gl, _normal[side]);
		}
		
		double r3 = sqrt(3);
		Vector3d v0 = new Vector3d(-r3, -r3, getDepth());
		Vector3d v1 = new Vector3d(+r3, -r3, getDepth());
		Vector3d v2 = new Vector3d(+r3, +r3, getDepth());
		Vector3d v3 = new Vector3d(-r3, +r3, getDepth());
		Vector3d n = new Vector3d(0, 0, 1);
		VecHelper.rotate(_rotation, v0);
		VecHelper.rotate(_rotation, v1);
		VecHelper.rotate(_rotation, v2);
		VecHelper.rotate(_rotation, v3);
		VecHelper.rotate(_rotation, n);
		Polygon p = new Polygon(v0, v1, v2, v3);
		for (int side = 0; side < CUBE_SIDES; side++) {
			p.intersect(_normal[side], 1);
		}
		gl.glBindTexture(GL.GL_TEXTURE_2D, textures[CUBE_SIDES]);
		p.draw(gl, n);
		
		gl.glDeleteTextures(PANELS, textures, 0);
		gl.glDisable(GL.GL_TEXTURE_2D);		

		p.drawOutline(gl);
	}
	
	private double getDepth() {
		 return (getDisplayParam()*2-1)*0.96;
	}
	
	private void putColor(ByteBuffer buffer, Color c) {
		buffer.put((byte)c.getRed());
		buffer.put((byte)c.getGreen());
		buffer.put((byte)c.getBlue());
		buffer.put((byte)c.getAlpha());
	}
	
	private void writePixels(Gfx3D g, int side, int npix, ByteBuffer buffer) {
		Vector3d v0, v1, v3, n;
		if (side < CUBE_SIDES) {
			v0 = _panel[side][0];
			v1 = _panel[side][1];
			v3 = _panel[side][3];
			n = _normal[side];
			// before rotation, the camera is looking down the negative z axis.  if the
			// normal vector of a side of the cube dotted with the z axis is negative,
			// then that side is not visible to the camera. however, because of perspective,
			// the same cannot be said of an internal cube slice.
			Vector3d normal = new Vector3d(n);
			VecHelper.rotate(g.rotation(), normal);
			if (normal.dot(new Vector3d(0, 0, 1)) <= 0)
				return;
		}
		else {
			double r3 = sqrt(3);
			v0 = new Vector3d(-r3, -r3, getDepth());
			v1 = new Vector3d(+r3, -r3, getDepth());
			v3 = new Vector3d(-r3, +r3, getDepth());
			n = new Vector3d(0, 0, 1);
			VecHelper.rotate(_rotation, v0);
			VecHelper.rotate(_rotation, v1);
			VecHelper.rotate(_rotation, v3);
			VecHelper.rotate(_rotation, n);
		}
		
		buffer.clear();
		for (int py = 0; py < npix; py++) {
			for (int px = 0; px < npix; px++) {
				double gx = v0.x + ((v1.x-v0.x)*px+(v3.x-v0.x)*py)/(npix-1);
				double gy = v0.y + ((v1.y-v0.y)*px+(v3.y-v0.y)*py)/(npix-1);
				double gz = v0.z + ((v1.z-v0.z)*px+(v3.z-v0.z)*py)/(npix-1);
				int ix = (int)rint(0.5*(gx+1)*(_dim[0]-1));
				int iy = (int)rint(0.5*(gy+1)*(_dim[1]-1));
				int iz = (int)rint(0.5*(gz+1)*(_dim[2]-1));
				putColor(buffer, _grid.getColor(ix, iy, iz));
			}
		}
		buffer.flip();
		g.getGL().glTexImage2D(
				GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, npix, npix,
				0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
	}
	
	private int[] buildTextures(Gfx3D g) {
		GL gl = g.getGL();

		int[] textures = new int[PANELS];
		gl.glGenTextures(PANELS, textures, 0);
		
		int wr = wallResolution;
		int sr = sliceResolution;
		int mr = Math.max(wr, sr);
		int[] npix = new int[] {wr, wr, wr, wr, wr, wr, sr};
		
		ByteBuffer buffer = com.jogamp.opengl.util.GLBuffers.newDirectByteBuffer(mr*mr*4);
		for (int side = 0; side < PANELS; side++) {
			gl.glBindTexture(GL.GL_TEXTURE_2D, textures[side]);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			writePixels(g, side, npix[side], buffer);
		}
		
		return textures;
	}
}

class Polygon {
	Vector3d t0, t1, t3;
	ArrayList<Vector3d> vs;
	
	public Polygon(Vector3d v0, Vector3d v1, Vector3d v2, Vector3d v3) {
		t0 = v0;
		t1 = v1;
		t3 = v3;
		vs = new ArrayList<Vector3d>();
		vs.add(v0);
		vs.add(v1);
		vs.add(v2);
		vs.add(v3);
	}
	
	public void intersect(Vector3d planeNormal, double depth) {
		boolean[] test = new boolean[vs.size()];
		for (int i = 0; i < vs.size(); i++)
			test[i] = vs.get(i).dot(planeNormal) < depth;

		ArrayList<Vector3d> res = new ArrayList<Vector3d>();
		for (int i = 0; i < vs.size(); i++) {
			if (test[i])
				res.add(vs.get(i));
			if (test[i] != test[(i+1)%vs.size()]) {
				Vector3d v1 = vs.get(i);
				Vector3d v2 = vs.get((i+1)%vs.size());
				double v1n = v1.dot(planeNormal);
				double v2n = v2.dot(planeNormal);
				double alpha = (depth - v1n) / (v2n - v1n);
				res.add(new Vector3d(
						(1-alpha)*v1.x+alpha*v2.x,
						(1-alpha)*v1.y+alpha*v2.y,
						(1-alpha)*v1.z+alpha*v2.z));
			}
		}
		vs = res;
	}
	
	public void draw(GL2 gl, Vector3d normal) {
		Vector3d t10 = new Vector3d(), t30 = new Vector3d();
		t10.sub(t1, t0);
		t30.sub(t3, t0);
		double n10 = t10.dot(t10);
		double n30 = t30.dot(t30);
		
		gl.glBegin(GL2.GL_POLYGON);
		gl.glNormal3d(normal.x, normal.y, normal.z);
		for (int i = 0; i < vs.size(); i++) {
			Vector3d v = vs.get(i);
			Vector3d tv0 = new Vector3d();
			tv0.sub(v, t0);
			gl.glTexCoord2d(tv0.dot(t10)/n10, tv0.dot(t30)/n30);
			gl.glVertex3d(0.5*v.x+0.5, 0.5*v.y+0.5, 0.5*v.z+0.5);
		}
		gl.glEnd();
	}
	
	public void drawOutline(GL2 gl) {
		gl.glDisable(GL2.GL_LIGHTING);
		
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glColor3d(0, 1, 1);
		for (int i = 0; i < vs.size(); i++) {
			Vector3d v = new Vector3d(vs.get(i));
			v.scale(1+1e-3);
			gl.glVertex3d(0.5*v.x+0.5, 0.5*v.y+0.5, 0.5*v.z+0.5);
		}
		gl.glEnd();
		gl.glEnable(GL2.GL_LIGHTING);
	}
}
