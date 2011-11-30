package will.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.MathException;

import will.util.DataHandling;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class PedometerService extends Service {
	
	private static final String TAG = "PedometerService";
	SensorManager sm;
	Sensor accelerometer;
	Sensor orientation;
	SensorEventListener sensorEventlistener;

	private long curTime = 0;
	private double lastTime = 0.;
	
	public PowerManager.WakeLock wakeLock;
	
	int stepNum = 0;
	double totalLength = 0;
	
	final static long firstTime = System.currentTimeMillis();

	double[] temp = {0, 0, 0, 0};

	public List<double[]> primitiveAccelerometerList = new ArrayList<double[]>();
	public List<double[]> accelerometerList = new ArrayList<double[]>();
	public List<double[]> orientationList = new ArrayList<double[]>();
	
	public List<double[]> offlineList = new ArrayList<double[]>();
	
	// Binder given to clients
    private final IBinder mBinder = new PedometerBinder();
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	/**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class PedometerBinder extends Binder {
    	public PedometerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PedometerService.this;
        }
    }
       
	public void SensorEvent()
	{
		Log.i(TAG, "PDRService onSensorEvent()......");
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		orientation = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		sensorEventlistener = new SensorEventListener()
		{

			public void onAccuracyChanged(Sensor sensor, int accuracy)
			{
				// TODO Auto-generated method stub
			}

			public void onSensorChanged(SensorEvent event) 
			{
				// TODO Auto-generated method stub
				
				double[] accelerometerValues = new double[4];
				double[] orientationValues = new double[4];
				double[] stepNumAndstrideLength = {0, 0};
				curTime = System.currentTimeMillis();
				switch(event.sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
					accelerometerValues[0] = event.values[0];
					accelerometerValues[1] = event.values[1];
					accelerometerValues[2] = event.values[2];
					curTime = System.currentTimeMillis();
					accelerometerValues[3] = (double)(curTime - firstTime) / 1000;
					temp = accelerometerValues;
					if(lastTime != accelerometerValues[3])
					{
						primitiveAccelerometerList.add(accelerometerValues);
						lastTime = accelerometerValues[3];
					}
					if(primitiveAccelerometerList.size() > 300)
					{
						stepNumAndstrideLength = DataHandling.getStepNumsAndTotalLength(primitiveAccelerometerList);
						stepNum += stepNumAndstrideLength[0];
						totalLength += stepNumAndstrideLength[1];
						
						offlineList.addAll(primitiveAccelerometerList.subList(35, 300));
						for(int i = 0; i < 265; i++)
						{
							primitiveAccelerometerList.remove(0);
						}	
					}
					break;
				case Sensor.TYPE_ORIENTATION:					
					orientationValues[0] = event.values[0];
					orientationValues[1] = event.values[1];
					orientationValues[2] = event.values[2];
					curTime = System.currentTimeMillis();
					orientationValues[3] = (double)(curTime - firstTime) / 1000;
					if(lastTime != orientationValues[3])
					{
						accelerometerList.add(temp);
						orientationList.add(orientationValues);
						lastTime = orientationValues[3];
					}
					break;
				default:
					break;
				}
			}
		};
		sm.registerListener(sensorEventlistener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		sm.registerListener(sensorEventlistener, orientation, SensorManager.SENSOR_DELAY_FASTEST);
	}
    
    public void StopSensor()
    {
    	sm.unregisterListener(sensorEventlistener);
    }
    
    public int getStepNum() 
    {
    return stepNum;
    }
    
    public double getTotalLength()
    {
    return totalLength;
    }
    
    public double getStrideLength()
    {
    	return getTotalLength()/getStepNum()*2;
    }
    
    public double getVelocity()
    {
    	return getTotalLength()/(curTime-firstTime)*1000*3600/1000;
    }
    
    public double[] getAccuracyStepNumAnsTotalLength()
    {
    	return DataHandling.getStepNumsAndTotalLength(offlineList);
    }
    
    public double getAccuracyStrideLength()
    {
    	return getAccuracyStepNumAnsTotalLength()[1]/getAccuracyStepNumAnsTotalLength()[0]*2;
    }
    
    public double getAccuracyVelocity()
    {
    	return getAccuracyStepNumAnsTotalLength()[1]/(curTime-firstTime)*1000*3600/1000;
    }
    
    public List<double[]> getVelocity1() throws MathException, IOException
    {   	
			return DataHandling.CalculateVelocityAndDisplacement(offlineList, accelerometerList, orientationList);
    }
    
    public List<double[]> getRotatedAccelerometer() throws MathException
    {
    	return DataHandling.getRotatedAccelerometer(accelerometerList, orientationList);
    }
    
    public List<Double> getLocalMeanAccelerometer()
    {
    	return DataHandling.getLocalMeanAcceleration(DataHandling.getMagnitudeOfAcceleration(offlineList));
    }
    
    /**
     * Reset all data
     */
    public void Reset()
    {
    	stepNum = 0;
    	totalLength = 0;
    	offlineList.clear();
    	primitiveAccelerometerList.clear();
    	orientationList.clear();
    	accelerometerList.clear();
    	DataHandling.velocityList.clear();
    	DataHandling.displacementList.clear();
    }
    
    public void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int wakeFlags;
        wakeFlags = PowerManager.PARTIAL_WAKE_LOCK;                
        wakeLock = pm.newWakeLock(wakeFlags, TAG);
        wakeLock.acquire();
    }
}
