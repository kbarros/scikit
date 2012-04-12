package scikit.numerics.vecmath;

public class VecHelper {
	/**
	 * Returns the rotation matrix corresponding to this unit quaternion. It
	 * is stored in row major order: [m00, m10, ...].
	 * 
	 * @return the rotation matrix 
	 */
	public static double[] getGLMatrix(Quat4d q) {
		double x = q.x;
		double y = q.y;
		double z = q.z;
		double w = q.w;
		
		double m00 = (1.0 - 2.0*y*y - 2.0*z*z);
		double m10 = (2.0*(x*y + w*z));
		double m20 = (2.0*(x*z - w*y));

		double m01 = (2.0*(x*y - w*z));
		double m11 = (1.0 - 2.0*x*x - 2.0*z*z);
		double m21 = (2.0*(y*z + w*x));

		double m02 = (2.0*(x*z + w*y));
		double m12 = (2.0*(y*z - w*x));
		double m22 = (1.0 - 2.0*x*x - 2.0*y*y);
		
		return new double[] {
				// this matrix is "visually transposed"
				m00, m10, m20, 0,
				m01, m11, m21, 0,
				m02, m12, m22, 0,
				0,   0,   0,   1,
		};
	}
	
	/**
	 * Returns the quaternion which represents a rotation along the axis of the
	 * vector argument, through an angle given by the magnitude of the vector
	 * argument.
	 * 
	 * @param vx
	 *            the x component of the rotation vector
	 * @param vy
	 *            the y component of the rotation vector
	 * @param vz
	 *            the z component of the rotation vector
	 */
	public static Quat4d quatFromAxisAngle(double vx, double vy, double vz) {
		double norm = Math.sqrt(vx*vx + vy*vy + vz*vz);
		double radians = norm/2;
		double n1 = norm == 0 ? 0 : Math.sin(radians)/norm;
		double n2 = Math.cos(radians);
		return new Quat4d(n1*vx, n1*vy, n1*vz, n2);
	}
	
	/**
	 * Rotates the vector by the quaternion and puts the result back in the vector
	 * 
	 * @param q
	 *            the quaternion specifying the rotation
	 * @param v
	 *            the vector to be rotated
	 */
	public static void rotate(Quat4d q, Vector3d v) {
		// from wikipedia: http://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation
		double t2 =   q.w*q.x;
		double t3 =   q.w*q.y;
		double t4 =   q.w*q.z;
		double t5 =  -q.x*q.x;
		double t6 =   q.x*q.y;
		double t7 =   q.x*q.z;
		double t8 =  -q.y*q.y;
		double t9 =   q.y*q.z;
		double t10 = -q.z*q.z;
		double xp = 2*( (t8 + t10)*v.x + (t6 -  t4)*v.y + (t3 + t7)*v.z ) + v.x;
		double yp = 2*( (t4 +  t6)*v.x + (t5 + t10)*v.y + (t9 - t2)*v.z ) + v.y;
		double zp = 2*( (t7 -  t3)*v.x + (t2 +  t9)*v.y + (t5 + t8)*v.z ) + v.z;
		v.set(xp, yp, zp);		
	}
}
