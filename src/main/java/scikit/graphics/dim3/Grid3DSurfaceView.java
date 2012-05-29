package scikit.graphics.dim3;

import javax.media.opengl.GL2;

import scikit.numerics.vecmath.Quat4d;

public class Grid3DSurfaceView extends Grid3DView {
	private Grid3D _grid;
	private int[] _dim;
	
	
	public Grid3DSurfaceView(Grid3D grid) {
		_grid = grid;
	}
	
	public void rotateStructure(Quat4d q) {
	}
	
	public void draw(Gfx3D g) {
		_dim = _grid.getDimensions();
		for (int z = 0; z < _dim[0]; z++) { 
			for (int y = 0; y < _dim[1]; y++) {
				for (int x = 0; x < _dim[2]; x++) {
					if (_grid.getSample(x, y, z) >= getCutoff()) {
						g.setColor(_grid.getColor(x, y, z));
						for (int dir = 0; dir < 6; dir++) {
							int xp = x+(int)_normal[dir].x;
							int yp = y+(int)_normal[dir].y;
							int zp = z+(int)_normal[dir].z;
							if (_grid.getSample(xp, yp, zp) < getCutoff())
								drawPanel(g, x, y, z, dir);
						}
					}
				}
			}
		}
	}
	
	private double getCutoff() {
		return getDisplayParam();
	}
	
	private void drawPanel(Gfx3D g, double x, double y, double z, int dir) {
		GL2 gl = g.getGL().getGL2();
		gl.glBegin(GL2.GL_QUADS);
		gl.glNormal3d(_normal[dir].x, _normal[dir].y, _normal[dir].z);
		for (int i = 0; i < 4; i++) {
			gl.glVertex3d(
					(x+0.5*_panel[dir][i].x+0.5)/_dim[0],
					(y+0.5*_panel[dir][i].y+0.5)/_dim[1],
					(z+0.5*_panel[dir][i].z+0.5)/_dim[2]);
		}
		gl.glEnd();
	}
}
