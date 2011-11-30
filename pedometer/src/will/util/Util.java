package will.util;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math.analysis.integration.UnivariateRealIntegrator;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.interpolation.UnivariateRealInterpolator;


public class Util {
	
	/**
	 * Compute the square
	 * @param d
	 * @return
	 */
	public static Double Square(double d)
	{
		return d * d;
	}
	
	/**
	 * Interpolation Algorithm
	 * @param x
	 * @param y
	 * @return
	 * @throws MathException
	 */
	public static UnivariateRealFunction Interpolation(double[] x, double[] y) throws MathException
	{
		UnivariateRealInterpolator interpolator = new SplineInterpolator();
		UnivariateRealFunction function = interpolator.interpolate(x, y);
		return function;
	}
	
	/**
	 * Integration Algorithm
	 * @return
	 * @throws IllegalArgumentException 
	 * @throws FunctionEvaluationException 
	 * @throws ConvergenceException 
	 */
	public static double Integration(UnivariateRealFunction function, double minCount, double maxCount) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException
	{
		UnivariateRealIntegrator integrator = new SimpsonIntegrator();
		integrator.integrate(function, minCount, maxCount);
		return integrator.getResult();
	}	
}
