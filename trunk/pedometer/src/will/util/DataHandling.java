package will.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

public class DataHandling {
	
	/**
	 * The size of the averaging window
	 */
	private static final int W = 15;
	
	/**
	 * Continuous number of 1 or 0
	 */
	private static final int BLOCKSIZE = 8;
	
	/**
	 * Parameter for Weiberg SL Algorithm
	 */
	private static final float K = (float) 0.45;
	
	/**
	 * Flag for continuous 1 in C2
	 */
	private static final boolean ONE = true;
	
	/**
	 * Flag for continuous 0 in C2 
	 */
	private static final boolean ZERO = false;
	
	public static List<double[]> velocityList = new ArrayList<double[]>();
	
	public static List<double[]> displacementList = new ArrayList<double[]>();
	
	public static List<double[]> rotatedAccelerometer = new ArrayList<double[]>();
	
	/**
	 * Compute the magnitude of the acceleration for every sample
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<Double> getMagnitudeOfAcceleration(List<double[]> accelerometer)
	{
		// acc: acceleration of x, y, z
		double[] acc = new double[4];
		// macc: magnitude of the acceleration
		double macc = 0;
		List<Double> magnitudeAcc = new ArrayList<Double>();
		Iterator<double[]> it = accelerometer.iterator();
		while (it.hasNext())
		{
			acc = it.next();
			macc = Math.sqrt(acc[0] * acc[0] + acc[1] * acc[1] + acc[2] * acc[2]);
			magnitudeAcc.add(macc);
		}
		return magnitudeAcc;
	}
	
	/**
	 * Compute the local mean acceleration
	 * @param magnitudeOfAcceleration
	 * @return
	 */
	public static List<Double> getLocalMeanAcceleration(List<Double> magnitudeOfAcceleration)
	{
		// macc: magnitude of the acceleration
		List<Double> macc = new ArrayList<Double>();
		macc = magnitudeOfAcceleration;
		// lmacc: local mean acceleration value
		List<Double> lmacc = new ArrayList<Double>();
		double sum = 0.;
		for (int i = 0, size = macc.size(); i < size; i++)
		{
			if (i < W || size - i <= W)
				lmacc.add(macc.get(i));
			else if (i >= W && size - i > W)
			{
				sum = macc.get(i);
				for (int j = 1; j <= W; j++)
				{
					sum += macc.get(i + j) + macc.get(i - j);
				}
				lmacc.add(sum / (2 * W + 1));
			}
		}
		return lmacc;
	}
	
	/**
	 * Get the average local mean acceleration
	 * @param localMeanAcceleration
	 * @return
	 */
	public static double getAverageLocalMeanAcceleration(List<Double> localMeanAcceleration) 
	{
		double sum = 0;
		double avg = 0;
		Iterator<Double> it = localMeanAcceleration.iterator();
		int size = localMeanAcceleration.size();
		while(it.hasNext())
		{
			sum += it.next();
		}
		avg = sum/size;
		return avg;
	}
	
	/**
	 * Compute condition for step counting
	 * @param threshold
	 * @param magnitudeOfAcceleration
	 * @return
	 */
	public static List<Integer> getCondition(double threshold, List<Double> magnitudeOfAcceleration)
	{
		List<Double> data = new ArrayList<Double>();
		data = getLocalMeanAcceleration(magnitudeOfAcceleration);
		List<Integer> condition = new ArrayList<Integer>();
		Iterator<Double> it = data.iterator();
		double flag;
		while (it.hasNext())
		{
			flag = it.next();
			if (flag > threshold)
			{
				condition.add(1);
			} 			
			else
			{
				condition.add(0);
			}
		}
		return condition;
	}
	
	/**
	 * Judge whether the data is equals 1, if true, return 1, else return 0
	 */
	public static boolean isOne(int data) {
		if(data == 1)
			return ONE;
		else return ZERO;
	}
	
	/**
	 * Counting Steps and calculate stride length
	 * @param magnitudeOfAcceleration
	 * @return
	 */
	public static double[] getStepNumsAndTotalLength(List<double[] > accelerometer)
	{
		List<Double> magnitudeOfAcceleration = getMagnitudeOfAcceleration(accelerometer);
		List<Double> localMeanAcceleration = getLocalMeanAcceleration(magnitudeOfAcceleration);
		double averageLocalMeanAcceleration = getAverageLocalMeanAcceleration(localMeanAcceleration); 
		double threshold = averageLocalMeanAcceleration + 0.5;
		List<Integer> condition = getCondition(threshold, magnitudeOfAcceleration);
		
		double stepNumAndtotalLength[] = {0, 0};
		
		int numOne = 0; //Count the continuous number of 1
		int numZero = 0; //Count the continuous number of 0
		int stepNum = 0; //The step number
		double strideLength = 0; //The length of the stride
		float totalLength = 0; //The total length
		boolean flag = false;  //Judge the point i is whether 1 or 0
		
		int size = condition.size();
		//Detecting steps by counting the continuous numbers of 1 and 0
		for(int i = 0, j = 1; i <= size-2 && j <= size-W; i++,j++) 
		{
			flag = isOne(condition.get(i));
			if((condition.get(i) == condition.get(j)) && flag == ONE)
			{				
				numOne++;
			}
			if((condition.get(i) == condition.get(j)) && flag == ZERO) 
			{
				numZero++;	
			}
			if((condition.get(i) != condition.get(j)) && j > W && j < size - W) {
				if(numOne > BLOCKSIZE && numZero > BLOCKSIZE) {
					stepNum++;
					numOne = 0;
					numZero = 0;
					strideLength = CalculateStrideLength(getMax(magnitudeOfAcceleration, j), 
							getMin(magnitudeOfAcceleration, j));
					totalLength += strideLength;
				}
			}
		}
		stepNumAndtotalLength[0] = stepNum;
		stepNumAndtotalLength[1] = totalLength;
		return stepNumAndtotalLength;
	}
	
	public static List<double[]> CalculateVelocityAndDisplacement(List<double[]> primitiveAccelerometer, List<double[] > accelerometer, List<double[]> orientation) throws MathException, IOException
	{
		List<Double> magnitudeOfAcceleration = getMagnitudeOfAcceleration(primitiveAccelerometer);
		List<Double> localMeanAcceleration = getLocalMeanAcceleration(magnitudeOfAcceleration);
		double averageLocalMeanAcceleration = getAverageLocalMeanAcceleration(localMeanAcceleration); 
		double threshold = averageLocalMeanAcceleration + 0.5;
		List<Integer> condition = getCondition(threshold, magnitudeOfAcceleration);
		
		rotatedAccelerometer = getRotatedAccelerometer(accelerometer, orientation);		
		
		int numOne = 0; //Count the continuous number of 1
		int numZero = 0; //Count the continuous number of 0
		boolean flag = false;  //Judge the point i is whether 1 or 0
		
		int size = condition.size();
		//Detecting steps by counting the continuous numbers of 1 and 0
		for(int i = 0, j = 1; i <= size-2 && j <= size-W; i++,j++) 
		{
			
			flag = isOne(condition.get(i));
			if((condition.get(i) == condition.get(j)) && flag == ONE)
			{				
				numOne++;
			}
			if((condition.get(i) == condition.get(j)) && flag == ZERO) 
			{
				numZero++;	
			}
			if((condition.get(i) != condition.get(j)) && j > W && j < size - W) {
				if(numOne > BLOCKSIZE && numZero > BLOCKSIZE) {
					double ct = primitiveAccelerometer.get(i-numOne-numZero)[3];
					double nt = primitiveAccelerometer.get(i)[3];					
					velocityList.addAll(IntegrationForVelocity(rotatedAccelerometer, ct, nt));
					displacementList.addAll(IntegrationForDisplacement(velocityList, ct, nt));
					numOne = 0;
					numZero = 0;
				}
			}	
		}
		return velocityList;
	}
	
	public static List<double[]> IntegrationForVelocity(List<double[]> accelerometer, double min, double max) throws MathException
	{
		List<double[]> velocityL = new ArrayList<double[]>();
		int size = accelerometer.size();
		double[] accX = new double[size];
		accX = ListToArray(accelerometer, 0);
		double[] accY = new double[size];
		accY = ListToArray(accelerometer, 1);
		double[] accZ = new double[size];
		accZ = ListToArray(accelerometer, 2);
		double[] accT = new double[size];
		accT = ListToArray(accelerometer, 3);
		UnivariateRealFunction functionaccX = Util.Interpolation(accT, accX);
		UnivariateRealFunction functionaccY = Util.Interpolation(accT, accY);
		UnivariateRealFunction functionaccZ = Util.Interpolation(accT, accZ);
		
		
			double velocity[] = {0, 0, 0, 0};
			velocity[0] = Util.Integration(functionaccX, min, max);
			velocity[1] = Util.Integration(functionaccY, min, max);
			velocity[2] = Util.Integration(functionaccZ, min, max);
			velocity[3] = max;
			velocityL.add(velocity);
		
		return velocityL;
	}
	
	public static List<double[]> IntegrationForDisplacement(List<double[]> velocity, double min, double max) throws IllegalArgumentException, MathException
	{
		List<double[]> displacementL = new ArrayList<double[]>();
		int size = velocity.size();
		double[] velX = new double[size];
		velX = ListToArray(velocity, 0);
		double[] velY = new double[size];
		velY = ListToArray(velocity, 1);
		double[] velT = new double[size];
		velT = ListToArray(velocity, 3);
		UnivariateRealFunction functionvelX = Util.Interpolation(velT, velX);
		UnivariateRealFunction functionvelY = Util.Interpolation(velT, velY);
		
		double[] displacement = {0, 0, 0};
		displacement[0] = Util.Integration(functionvelX, min+0.02, max);
		displacement[1] = Util.Integration(functionvelY, min+0.02, max);
		displacement[2] = max;
		displacementL.add(displacement);

		return displacementL;
	}
	
	public static List<double[]> getRotatedAccelerometer(List<double[] > accelerometer, List<double[]> orientation) throws MathException
	{
		List<double[]> rotatedAccelerometer = new ArrayList<double[]>();	
		rotatedAccelerometer.addAll(RotationMatrix.getTransformedAcceleration(accelerometer, orientation));
		return rotatedAccelerometer;
	}
	/**
	 * Calculate strideLength using Weiberg SL Algorithm
	 * @param maxA
	 * @param minA
	 * @return
	 */
	public static double CalculateStrideLength(double maxA, double minA)
	{
		return K * Math.pow(maxA - minA, 0.25);
	}
	
	/**
	 * Transform a List to array
	 * @param a
	 * @return
	 */
	public static double[] ListToArray(List<double[]> b, int flag)
	{
		int size = b.size();
		double[] a = new double[size];		
		for(int i = 0; i < size; i++)
		{
			a[i] = b.get(i)[flag];
		}
		return a;
	}
	
	/**
	 * Get max accelerometer from -W ~ +W samples around point j
	 * @param magnitudeOfAcceleration
	 * @param j
	 * @return
	 */
	public static double getMax(List<Double> magnitudeOfAcceleration, int j)
	{
		double a = magnitudeOfAcceleration.get(j);
		double b = 0;
		double maxA = 0;
		for(int k = -W; k < W; k++) {						
			b = magnitudeOfAcceleration.get(j+k);
			maxA = a > b ? a : b;
		}
		return maxA;
	}
	
	/**
	 * Get min accelerometer from -W ~ +W samples around point j
	 * @param magnitudeOfAcceleration
	 * @param j
	 * @return
	 */
	public static double getMin(List<Double> magnitudeOfAcceleration, int j)
	{
		double a = magnitudeOfAcceleration.get(j);
		double b = 0;
		double minA = 0;
		for(int k = -W; k < W; k++) {						
			b = magnitudeOfAcceleration.get(j+k);
			minA = a < b ? a : b;
		}
		return minA;
	}
	
	/**
	 * Integration for velocity
	 * @param magnitudeOfAcceleration
	 * @return
	 * @throws MathException 
	 * @throws IllegalArgumentException 
	 * @throws FunctionEvaluationException 
	 * @throws ConvergenceException 
	 */
	public static double[] getVelocity(List<double[]> accelerometer) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException, MathException
	{
		double[] velocity = {0., 0.};
		int size = accelerometer.size();
		double[] accX = new double[size];
		double[] accY = new double[size];
		for(int i = 0; i < size; i++)
		{
			accX[i] = accelerometer.get(i)[3];
			accY[i] = accelerometer.get(i)[0];
		}
		double time = accelerometer.get(size-1)[3];
		velocity[0] = Util.Integration(Util.Interpolation(accX, accY), accX[0], accX[accX.length-1]);	
		velocity[1] = time;
		return velocity;
	}
}
