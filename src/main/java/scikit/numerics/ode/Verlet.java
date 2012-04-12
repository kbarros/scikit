//Derived from Open Source Physics library, released under the GPL version 2.

package scikit.numerics.ode;

/**
 * Verlet:  A velocity Verlet method ODE solver.
 *
 * The velocity Verlet algorithm is a second order and simplectic. It is equivalent
 * to Leap-Frog, but is self starting.
 *
 * x(n+1) = x(n) + v(n)* dt + a(n)*dt*dt/2
 * a_est=F(x(n+1),v(n),t)/m
 * v(n+1) = v(n) + (a(n)+a_est)*dt/2
 *
 * CAUTION! This implementation assumes that the state variables alternate
 * between position and velocity with the last variable being time.
 * That is, the  state vector is ordered as follows:
 *
 * x1, d x1/dt, x2, d x2/dt, x3, d x3/dt ..... xN, d xN/dt, (+ extra eqns)
 * 
 * where N is numPhaseEqn.
 * 
 * CAUTION! Call initialize() whenever the state array is changed in such a way that
 * getRate() will return something new.  
 */
public class Verlet extends AbstractODESolver {
	private double[] rate1; // stores the initial rate
	private double[] rate2; // used to compute the estimated the acceleration at x(n+1).
	private int numPhaseEqn;

	/**
	 * Constructs the velocity Verlet ODESolver for a system of ordinary differential equations.
	 * Calls the initialize() method with dt = 0.1
	 *
	 * @param ode the system of differential equations.
	 */
	public Verlet(ODE ode, int numPhaseEqn) {
		super(ode);
		this.numPhaseEqn = numPhaseEqn;
	}

	/**
	 * Initializes the ODE solver.
	 *
	 * The rate array is allocated.  The number of differential equations is
	 * determined by invoking getState().length on the ODE. The initial acceleration
	 * is determined by calling getRate().
	 *
	 * @param stepSize
	 */
	public void initialize(double stepSize) {
		super.initialize(stepSize);
		rate1 = new double[numEqn];
		rate2 = new double[numEqn];
		// rate2 should contain result of getRate() from last time step
		ode.getRate(ode.getState(), rate2);
	}


	/**
	 * Steps (advances) the differential equations by the stepSize.
	 *
	 * The ODESolver invokes the ODE's getState method to obtain the initial state of the system.
	 * The ODESolver advances the solution and copies the new state into the
	 * state array at the end of the solution step.
	 *
	 * @return the step size
	 */
	public double step() {
		// state[]: x1, d x1/dt, x2, d x2/dt .... xN, d xN/dt, (+ extra eqns)
		double[] state = ode.getState();
		if(state.length != numEqn) {
			initialize(stepSize);
		}

		// swap rate1 and rate2 arrays so that rate1 contains rate from last time step
		double[] temp = rate1;
		rate1 = rate2;
		rate2 = temp;

		double dt = stepSize;

		// x(n+1) <- x(n) + v(n) dt + a(n) dt^2 / 2
		for(int i = 0; i < numPhaseEqn; i += 2)
			state[i] += state[i+1]*dt + rate1[i+1]*dt*dt/2;
		
		// a(n+1) <- F(x(n+1)]/m 
		ode.getRate(state, rate2);
		
		// v(n+1) <- v(n) + (a(n) + a(n+1)) dt / 2
		for(int i = 0; i < numPhaseEqn; i += 2)
			state[i+1] += (rate1[i+1]+rate2[i+1])*dt/2.0;
		
		// use euler algorithm to update variables which are not part of the phase space
		for (int i = numPhaseEqn; i < numEqn; i++)
			state[i] += stepSize*rate2[i];
		
		return stepSize;
	}
}
