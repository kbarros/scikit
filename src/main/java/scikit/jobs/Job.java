package scikit.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class Job {
	private static Job current;
	
	private Simulation sim;
	private Control control;
	private Thread thread;
	private Cooperation coop = new Cooperation();
	
	private enum State {STEP, RUN, STOP, KILL};
	private State state;
	
	private long lastYield, yieldDelay = 10;
	private long lastAnimate, animateDelay = 50;	
	private boolean throttleAnimation = false;

	
	public Job(Simulation sim, Control control) {
		this.sim = sim;
		this.control = control;
		current = this;
	}
	
	/**
	 * Performs one step of the simulation. The GUI thread will wait while
	 * the simulation thread runs. The step is completed when the simulation thread calls
	 * the static method Job.animate().
	 */
	public void step() {
		state = State.STEP;
		if (thread == null)
			createThread();
		else
			wake();
	}
	
	/**
	 * Runs the simulation until it is explicitly stopped. The GUI thread will wait while
	 * the simulation thread runs.
	 */
	public void start() {
		state = State.RUN;
		if (thread == null)
			createThread();
		else
			wake();
	}
	
	/**
	 * Stops the simulation execution after the completion of this "simulation step",
	 * when the simulation thread calls the static method Job.animate(). 
	 */
	public void stop() {
		state = State.STOP;
	}
	
	/**
	 * Schedules the thread to be killed after the completion of this "simulation step".
	 * This will cause a ThreadDeath exception to be thrown in the simulation thread. 
	 */
	public void kill() {
		state = State.KILL;
		wake();
	}
	
	/**
	 * Wakes the simulation thread if it is stopped, in order that it can animate. This
	 * can be useful if, for example, an external parameter has been changed and the displays
	 * need to be updated.
	 */
	public void wake() {
		if (thread != null)
			coop.triggerProcessingLoop();		
	}

	/**
	 * Force a delay of 50 ms between each simulation step 
	 * @param b
	 */
	public void throttleAnimation(boolean b) {
		throttleAnimation = b;
	}
	
	/**
	 * Returns the underlying Simulation object for this Job. 
	 */
	public Simulation sim() {
		return sim;
	}
	
	/**
	 * To be called from the simulation thread. Registers that the simulation thread has
	 * completed a step. Calls the <code>animate</code> method of the simulation.
	 * Wakes the GUI thread, which has been locked during execution
	 * of the simulation thread. 
	 * The simulation thread either continues running or stops based on
	 * the Job state.
	 */
	public static void animate() {
		current()._animate();
	}
	private void _animate() {
		if (Thread.currentThread() != thread) {
			throw new IllegalThreadStateException("Job.animate() must be called from simulation thread.");
		}
		
		// perform processing relevant to the step completion from the GUI thread
		// after sim.animate() has executed
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				control.processStepCompletion();
			}
		});
		
		switch (state) {
		case RUN:
			long timeUntilAnimate = lastAnimate + animateDelay - System.currentTimeMillis();
			if (throttleAnimation) {
				sim.animate();
				lastAnimate = System.currentTimeMillis();
				coop.sleep(Math.max(timeUntilAnimate, 0));
			}
			else if (timeUntilAnimate < 0) {
				sim.animate();
				lastAnimate = System.currentTimeMillis();
				_yield();
			}
			break;
		}
		// during sleep() or yield() the user might have stopped the simulation. therefore
		// we enter a new switch statement.
		switch (state) {
		case STEP:
		case STOP:
			state = State.STOP;
			do {
				sim.animate();
				coop.pass(); // stop the simulation thread; make the GUI thread active
			} while (state == State.STOP);
			break;
		}
		// during pass() the user might have reset the simulation.
		switch (state) {
		case KILL:
			throw new ThreadDeath();
		}
	}
	
	/**
	 * To be called from the simulation thread. Wakes the GUI thread, which has
	 * been locked during execution of the simulation thread. It is necessary to periodically call either
	 * <code>Job.animate()</code> or <code>Job.yield()</code> to ensure that the GUI does not hang.
	 */
	public static void yield() {
		current()._yield();
	}
	private void _yield() {
		if (Thread.currentThread() != thread) {
			throw new IllegalThreadStateException("Job.yield() must be called from simulation thread.");
		}
		if (System.currentTimeMillis() - lastYield > yieldDelay) {
			// give the GUI thread a chance to run.  an alternative to sleep(0) is the sequence
			//    coop.triggerProcessingLoop();
			//    coop.pass();
			// but this exhibited bugs on the Linux and Windows platforms: certain operations
			// (such as graphical window updates) do not get a chance to run. using sleep(0)
			// seems to be a cross platform solution (with maybe a slight performance penalty).
			coop.sleep(0);
			lastYield = System.currentTimeMillis();
		}
	}
	
	/**
	 * To be called from the simulation thread. Sends a signal to the GUI thread to stop running the
	 * simulation. The simulation will continue running until the "simulation step" is completed.
	 */
	public static void signalStop() {
		current()._signalStop(); 
	}
	public void _signalStop() {
		if (Thread.currentThread() != thread) {
			throw new IllegalThreadStateException("Job.halt() must be called from simulation thread.");
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				control.clickStopButton();
			}
		});
		_yield();
	}
	
	private static Job current() {
		return current;
	}

	private String detailedErrorMessage(Exception e) {
		StringWriter stringWriter = new StringWriter();
	    PrintWriter printWriter = new PrintWriter(stringWriter);
	    e.printStackTrace(printWriter);
	    return stringWriter.getBuffer().toString();
	}
	
	private void createThread() {
		assert (thread == null);
		thread = new Thread(new Runnable() {
			String errMsg = null;
			public void run() {
				try {
					// registering causes the GUI thread to wait during simulation execution
					coop.register();
					// perform the simulation, periodically yielding to the GUI thread. if
					// the simulation is externally killed a thread death error will be
					// thrown.
					sim.run();
					// simulation has finished. "pass" to yield control to the GUI thread.
					// if the GUI thread returns control, then perform an animation, and again
					// "pass".
					while (true) {
						control.clickStopButton();
						control.disableRunButtons();
						sim.animate();
						coop.pass();
						if (current().state == State.KILL)
							throw new ThreadDeath();
					}
				}
				catch (Exception e) {
					errMsg = detailedErrorMessage(e);
				}
				finally {
					// we could reach here due to a bug in the simulation (an Exception)
					// or because the user killed the job (ThreadDeath error). in either case,
					// we must now return the Job to its initial state.
					sim.clear();
					coop.unregister();
					thread = null;
					// display possible execution exception in full detail for debugging
					if (errMsg != null) {
						System.err.println(errMsg);
						JOptionPane.showMessageDialog(null, errMsg, "Error Occurred in Simulation", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		thread.start();
	}
}
