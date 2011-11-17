package ouc.pdr.activity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import ouc.pdr.util.file.FileIO;
import ouc.pdr.util.util.Util;
import will.stepcounter.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HandleDataActivity extends Activity
{
	/**
	 * File Name of Accelerometer Data
	 */
	private static final String ACCELEROMETERFILE = "/sdcard/adata1.txt";
	
	/**
	 * File Name of Primitive Accelerometer Data
	 */
	private static final String PRIMITIVEACCELEROMETERFILE = "/sdcard/padata1.txt";
	
	/**
	 * File Name of Transformed Accelerometer Data
	 */
	private static final String TRANSFORMEDACCELEROMETERFILE ="/sdcard/newadata1.txt";
	
	/**
	 * File Name of Magnetic Data
	 */
	private static final String ORIENTATIONFILE = "/sdcard/odata1.txt";
	
	/**
	 * File Name of Stride Length Data
	 */
	@SuppressWarnings("unused")
	private static final String STRIDELENGTH = "/sdcard/stridelength.txt";

	/**
	 * File Name of Condition 1
	 */
	@SuppressWarnings("unused")
	private static final String C1 = "/sdcard/c1.txt";
	
	/**
	 * File Name of Condition 2 
	 */
	@SuppressWarnings("unused")
	private static final String C2 = "/sdcard/c2.txt";
	
	/**
	 * File Name of Condition 3
	 */
	@SuppressWarnings("unused")
	private static final String C3 = "/sdcard/c3.txt";
	
	/**
	 * Local acceleration variance
	 */
	@SuppressWarnings("unused")
	private static final String LOCALACCELERATIONVARIANCE = "/sdcard/localaccelerationvariance.txt";
	
	/**
	 * Minimum threshold of Accelerometer used for condition C1
	 */
	private static final float thamin = 9;
	
	/**
	 * Max threshold of Accelerometer used for condition C1
	 */
	private static final float thamax = 11;
	
	/**
	 * Threshold of the local acceleration variance
	 */
	private static final float thlav = (float) 2.5;
	
	/**
	 * Max threshold of Magnetic used for condition C3
	 */
	private static final float thm = 50;
	
	/**
	 * The size of the averaging window
	 */
	private static final int W = 15;
	
	/**
	 * Continuous number of 1 or 0
	 */
	private static final int BLOCKSIZE = 13;
	
	/**
	 * Parameter used for calculating stride length
	 */
	private static final float K = (float) 1.42;
	
	/**
	 * Flag for continuous 1 in condition C2
	 */
	private static final boolean ONE = true;
	
	/**
	 * Flag for continuous 0 in condition C2 
	 */
	private static final boolean ZERO = false;
	
	private Button calculateStepNumBtn = null;
	private TextView stepNumber = null;
	private TextView totalLength = null;
	
	private Button accelerationTransformBtn = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data);
		
		accelerationTransformBtn = (Button) findViewById(R.id.accelerationTransformBtn);		
		calculateStepNumBtn = (Button) findViewById(R.id.calculateStepNumBtn);
		
        stepNumber = (TextView) findViewById(R.id.stepNum);
        totalLength = (TextView) findViewById(R.id.totalLength);
        
        accelerationTransformBtn.setOnClickListener(new AccelerationTransformBtnClickListener());
        
        calculateStepNumBtn.setOnClickListener(new CalculateStepNumBtnClickListener());
		
	}
	/**
	 * Run when the AccelerationTransform Button is clicked
	 * @author Administrator
	 *
	 */
	class AccelerationTransformBtnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub			
				try {
					if(getAccelerometers().isEmpty()) {
						Toast.makeText(getBaseContext(), "没有加速度数据，无法转换", Toast.LENGTH_SHORT).show();
					}
					else {
					FileIO.WriteToFile(TRANSFORMEDACCELEROMETERFILE, getTransformedAcceleration());
					Toast.makeText(getBaseContext(), "加速度转换成功", Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}		
	}
	
	/**
	 * Run when the Calculate Step Number Button is clicked
	 * @author Administrator
	 *
	 */
	class CalculateStepNumBtnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub			
			int stepNum = 0;
			try {
				stepNum = CountingSteps();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			stepNumber.setText("总共走过" + String.valueOf(stepNum*2) + "步");									
		}
    }
	
	/**
	 * Get the value of accelerometer from File ACCELEROMETERFILE
	 * @return
	 * @throws IOException
	 */
	public List<float[]> getPrimitiveAccelerometers() throws IOException
	{
		String line = "";
		List<float[]> accelerometers = new ArrayList<float[]>();
		FileInputStream fr = new FileInputStream(PRIMITIVEACCELEROMETERFILE);
		BufferedReader br = new BufferedReader(new InputStreamReader(fr));
		while ((line = br.readLine()) != null)
		{
			float[] data = new float[3];
			StringTokenizer st = new StringTokenizer(line, " ");
			data[0] = Float.parseFloat(st.nextToken());
			data[1] = Float.parseFloat(st.nextToken());
			data[2] = Float.parseFloat(st.nextToken());
			accelerometers.add(data);
		}
		return accelerometers;
	}
	
	/**
	 * Get the value of accelerometer from File ACCELEROMETERFILE
	 * @return
	 * @throws IOException
	 */
	public List<float[]> getAccelerometers() throws IOException
	{
		String line = "";
		List<float[]> accelerometers = new ArrayList<float[]>();
		FileInputStream fr = new FileInputStream(ACCELEROMETERFILE);
		BufferedReader br = new BufferedReader(new InputStreamReader(fr));
		while ((line = br.readLine()) != null)
		{
			float[] data = new float[3];
			StringTokenizer st = new StringTokenizer(line, " ");
			data[0] = Float.parseFloat(st.nextToken());
			data[1] = Float.parseFloat(st.nextToken());
			data[2] = Float.parseFloat(st.nextToken());
			accelerometers.add(data);
		}
		return accelerometers;
	}
	
	/**
	 * Get the value of magnetic from File MAGNETICFILE
	 * @return
	 * @throws IOException
	 */
	public List<float[]> getOrientation() throws IOException
	{
		String line = "";
		List<float[]> orientation = new ArrayList<float[]>();
		FileInputStream fr = new FileInputStream(ORIENTATIONFILE);
		BufferedReader br = new BufferedReader(new InputStreamReader(fr));
		while ((line = br.readLine()) != null)
		{
			float[] data = new float[3];
			StringTokenizer st = new StringTokenizer(line, " ");
			data[0] = Float.parseFloat(st.nextToken());
			data[1] = Float.parseFloat(st.nextToken());
			data[2] = Float.parseFloat(st.nextToken());
			orientation.add(data);
		}
		return orientation;
	}
	
	/**
	 * Compute the magnitude of the acceleration for every sample
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Float> getMagnitudeOfAcceleration() throws IOException
	{
		// acc: acceleration of x, y, z
		float[] acc = new float[3];
		// macc: magnitude of the acceleration
		float macc = 0;
		List<Float> magnitudeAcc = new ArrayList<Float>();
		List<float[]> data = new ArrayList<float[]>();
		data = getPrimitiveAccelerometers();
		Iterator<float[]> it = data.iterator();
		while (it.hasNext())
		{
			acc = it.next();
			macc = (float) Math.sqrt(acc[0] * acc[0] + acc[1] * acc[1] + acc[2] * acc[2]);
			magnitudeAcc.add(macc);
		}
		return magnitudeAcc;
	}
	
	/**
	 * Compute the magnitude of the orientation for every sample
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Float> getMagnitudeOfOrientation() throws IOException
	{
		// occ: orientation of x, y, z
		float[] occ = new float[3];
		// oocc: magnitude of the orientation
		float oocc = 0;
		List<Float> magnitudeOcc = new ArrayList<Float>();
		List<float[]> data = new ArrayList<float[]>();
		data = getOrientation();
		Iterator<float[]> it = data.iterator();
		while (it.hasNext())
		{
			occ = it.next();
			oocc = (float) Math.sqrt(occ[0] * occ[0] + occ[1] * occ[1] + occ[2] * occ[2]);
			magnitudeOcc.add(oocc);
		}
		return magnitudeOcc;
	}
	
	/**
	 * Compute the local mean acceleration value
	 * @return
	 * @throws IOException
	 */
	public List<Float> getLocalMeanAcceleration() throws IOException
	{
		// macc: magnitude of the acceleration
		List<Float> macc = new ArrayList<Float>();
		macc = getMagnitudeOfAcceleration();
		// lmacc: local mean acceleration value
		List<Float> lmacc = new ArrayList<Float>();
		float sum = 0;
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
	 * Compute the local acceleration variance, to highlight the foot activity
	 * and to remove gravity
	 * @return
	 * @throws IOException
	 */
	public List<Float> getLocalAccelerationVariance() throws IOException
	{
		// macc: magnitude of the acceleration
		List<Float> macc = new ArrayList<Float>();
		macc = getMagnitudeOfAcceleration();
		// lmacc: local mean acceleration value
		List<Float> lmacc = new ArrayList<Float>();
		lmacc = getLocalMeanAcceleration();
		// lav: local acceleration variance
		List<Float> lav = new ArrayList<Float>();
		float sum = 0;
		for (int i = 0, size = macc.size(); i < size; i++)
		{
			if (i < W || size - i <= W)
				lav.add((float) Math.sqrt(macc.get(i)));
			else if (i >= W && size - i > W)
			{
				sum = Util.Square(macc.get(i) - lmacc.get(i));
				for (int j = 1; j <= W; j++)
				{
					sum += Util.Square(macc.get(i - j) - lmacc.get(i - j)) + Util.Square(macc.get(i + j) - lmacc.get(i + j));
				}
				lav.add((float) Math.sqrt(sum / (2 * W + 1)));
			}
		}
		return lav;
	}
	
	/**
	 * compute the condition C1
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Integer> getC1() throws IOException
	{
		List<Float> data = new ArrayList<Float>();
		data = getMagnitudeOfAcceleration();
		List<Integer> c1 = new ArrayList<Integer>();
		Iterator<Float> it = data.iterator();
		Float flag;
		while (it.hasNext())
		{
			flag = it.next();
			if (flag > thamin && flag < thamax)
			{
				c1.add(1);
			} 			
			else
			{
				c1.add(0);
			}
		}
		return c1;
	}
	
	/**
	 * compute the condition C2
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Integer> getC2() throws IOException
	{
		List<Float> data = new ArrayList<Float>();
		data = getLocalAccelerationVariance();
		List<Integer> c2 = new ArrayList<Integer>();
		Iterator<Float> it = data.iterator();
		float flag;
		while (it.hasNext())
		{
			flag = it.next();
			if (flag > thlav)
			{
				c2.add(1);
			} 			
			else
			{
				c2.add(0);
			}
		}
		return c2;
	}
	
	/**
	 * compute the condition C3
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Integer> getC3() throws IOException
	{
		List<Float> data = new ArrayList<Float>();
		data = getMagnitudeOfOrientation();
		List<Integer> c3 = new ArrayList<Integer>();
		Iterator<Float> it = data.iterator();
		float flag;
		while (it.hasNext())
		{
			flag = it.next();
			if (flag > thm)
			{
				c3.add(1);
			} 			
			else
			{
				c3.add(0);
			}
		}
		return c3;
	}
	
	/**

	 * Counting steps
	 * 
	 * @throws IOException
	 */
	public int CountingSteps() throws IOException
	{
		int numOne = 0; //Count the continuous number of 1
		int numZero = 0; //Count the continuous number of 0
		int stepNum = 0; //The step number
		float strideLength = 0; //The length of the stride
		float sum = 0; //The total length
		float maxA = 0;
		float minA = 0;
		float a = 0;
		float b = 0;
		boolean flag =false;
		
		List<Float> strideLengthList = new ArrayList<Float>();
		
		List<Integer> data = getC2();
		List<Float> accelerometerdata = new ArrayList<Float>();
		accelerometerdata = getMagnitudeOfAcceleration();
		int size = data.size();
		for(int i = 0, j = 1; i <= size-2 && j <= size-1; i++,j++) 
		{
			flag = isOne(data.get(i));
			if((data.get(i) == data.get(j)) && flag == ONE)
			{				
				numOne++;
			}
			if((data.get(i) == data.get(j)) && flag == ZERO) 
			{
				numZero++;	
			}
			if(data.get(i) != data.get(j)) {
				if(numOne > BLOCKSIZE && numZero > BLOCKSIZE) {
					stepNum++;
					numOne = 0;
					numZero = 0;
					maxA = accelerometerdata.get(j);
					minA = accelerometerdata.get(j);
					for(int k = -W; k < W; k++) {
						a = accelerometerdata.get(j);
						b = accelerometerdata.get(j+k);
						maxA = a > b ? a : b;
						minA = a < b ? a : b;		
					}
					strideLength = (float) (K * Math.pow(maxA  - minA, 0.25));
					sum += strideLength;
					strideLengthList.add(strideLength);
				}
				totalLength.setText("走过的路程为：" + String.valueOf(sum)+"米");
			}
		}
		return stepNum;
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
	 * Transform the accelerometer's data from Body System to World Coordinate System
	 * @return
	 * @throws IOException
	 */
	public List<float[]> getTransformedAcceleration() throws IOException {
		
		List<float[]> acc = new ArrayList<float[]>();
		acc = getAccelerometers();		
		List<float[]> ori = new ArrayList<float[]>();
		ori = getOrientation();
		int size = acc.size();
		List<float[]> nacc = new ArrayList<float[]>();
		for(int i = 0; i < size; i++)
		{
			float[] R = {0, 0, 0, 0, 0, 0, 0, 0, 0};			
			float[] temp = {0, 0, 0};
			float[] a = {0, 0, 0};
			float[] o = {0, 0, 0};
			a = acc.get(i);
			o = ori.get(i);
			float c0 = (float) Math.cos(o[0]*Math.PI/180);
			float c1 = (float) Math.cos(o[1]*Math.PI/180);
			float c2 = (float) Math.cos(o[2]*Math.PI/180);
			float s0 = (float) Math.sin(o[0]*Math.PI/180);
			float s1 = (float) Math.sin(o[1]*Math.PI/180);
			float s2 = (float) Math.sin(o[2]*Math.PI/180);
			R[0] = c0*c2;
			R[1] = s0*c1+s1*s2*c0;
			R[2] = s0*s1-c1*s2*c0;
			R[3] = -c2*s0;
			R[4] = c1*c0-s1*s2*s0;
			R[5] = s1*c0+c1*s2*s0;
			R[6] = s2;
			R[7] = -s1*c2;
			R[8] = c1*c2;			

			temp[0] = (float) (a[0]*R[0] + a[1]*R[1] + a[2]*R[2]);
			temp[1] = (float) (a[0]*R[3] + a[1]*R[4] + a[2]*R[5]);
			temp[2] = (float) (a[0]*R[6] + a[1]*R[7] + a[2]*R[8]);
			nacc.add(temp);
		}
		return nacc;
	}	

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		Log.i("HandleDataActivity", "HandleDataActivity onDestroy()......");
		super.onDestroy();
	}

}