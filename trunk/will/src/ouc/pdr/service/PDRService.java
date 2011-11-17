package ouc.pdr.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ouc.pdr.util.file.FileIO;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class PDRService extends Service
{

	SensorManager sm;
	Sensor accelerometer;
	Sensor orientation;
	SensorEventListener sensorEventlistener;

	private long curTime = 0;	
	final static long firstTime = System.currentTimeMillis();

	private String accelerometerStr;
	private String orientationStr;
	private String oldAccelerometerStr;

	float[] gravity = { 0, 0, SensorManager.STANDARD_GRAVITY };
	float[] linear_acceleration = new float[3];
	float[] temp = {0, 0, 0, 0};

	List<float[]> oldAccelerometerList = new ArrayList<float[]>();
	List<float[]> accelerometerList = new ArrayList<float[]>();
	List<float[]> orientationList = new ArrayList<float[]>();

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		Log.i("PDRService", "PDRService onBind()......");
		return null;
	}

	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		Toast.makeText(getBaseContext(), "服务已启动", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			FileIO.WriteToFile(accelerometerStr, accelerometerList);
			FileIO.WriteToFile(orientationStr, orientationList);
			FileIO.WriteToFile(oldAccelerometerStr, oldAccelerometerList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sm.unregisterListener(sensorEventlistener);
		Toast.makeText(getBaseContext(), "服务已关闭", Toast.LENGTH_SHORT).show();
	}

	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO Auto-generated method stub
		// 通过Bundle获取数据
		Bundle bundle = intent.getExtras();
		accelerometerStr = "/sdcard/adata" + bundle.getString("accelerometer") + ".txt";
		orientationStr = "/sdcard/odata" + bundle.getString("orientation") + ".txt";
		oldAccelerometerStr = "/sdcard/padata" + bundle.getString("primitiveAccelerometer") + ".txt";
		SensorEvent();
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void SensorEvent()
	{
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
				
				float[] accelerometerValues = new float[4];
				float[] orientationValues = new float[4];
				curTime = System.currentTimeMillis();
				switch(event.sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
					
					accelerometerValues[0] = event.values[0];
					accelerometerValues[1] = event.values[1];
					accelerometerValues[2] = event.values[2];
					curTime = System.currentTimeMillis();
					accelerometerValues[3] = Float.parseFloat(String.valueOf((double)(curTime - firstTime) / 1000));
					temp = accelerometerValues;
					oldAccelerometerList.add(accelerometerValues);
					break;
				case Sensor.TYPE_ORIENTATION:
					
					orientationValues[0] = event.values[0];
					orientationValues[1] = event.values[1];
					orientationValues[2] = event.values[2];
					curTime = System.currentTimeMillis();
					orientationValues[3] = Float.parseFloat(String.valueOf((double)(curTime - firstTime) / 1000));
					accelerometerList.add(temp);
					orientationList.add(orientationValues);
					break;
				default:
					break;
				}
			}
		};

		sm.registerListener(sensorEventlistener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		sm.registerListener(sensorEventlistener, orientation, SensorManager.SENSOR_DELAY_FASTEST);
	}
}
