package will.activity;

import java.io.IOException;

import org.apache.commons.math.MathException;

import will.file.FileIO;
import will.service.PedometerService;
import will.service.PedometerService.PedometerBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PedometerActivity extends Activity implements OnClickListener
{
	private static final String VELOCITYFILE = "/sdcard/velocity.txt";
	
	private static final String ACCELEROMETERFILE = "/sdcard/accelerometer.txt";
	
	private static final String LOCALMEANACCELEROMETER = "/sdcard/localmeanaccelerometer.txt";
	
	private static final String TAG = "PDRActivity";
	private Button bindServiceBtn = null;
	private Button unbindServiceBtn = null;
	private Button offlineBtn = null;
	private Button resetBtn = null;
	
	private TextView time = null;
	private TextView stepNumText = null;
	private TextView totalLengthText = null;
	private TextView strideLengthText = null;
	private TextView velocityText = null;
	
	private TextView accuracyStepNumText = null;
	private TextView accuracyTotalLengthText = null;
	private TextView accuracyStrideLengthText = null;
	private TextView accuracyVelocityText = null;
	
	PedometerService mService;
    boolean mBound = false;
    boolean sensorStoped = true;
	
	int stepNum = 0;
	float totalLength = 0;
	float strideLength = 0;
	float velocity = 0;
	
	int accuracyStepNum = 0;
	float accuracyTotalLength = 0;
	float accuracyStrideLength = 0;
	float accuracyVelocity = 0;
	
	int VISIBLE = 0;
	int INVISIBLE = 4;
	int GONE = 8;
	
	long curTime = 0;
	long lastTime = 0;
	private long second = 0;
	private int hour = 0;
	private int minute = 0;
	
	private boolean run = false;

	private Handler handler = new Handler();

	private Runnable task = new Runnable()
	{

		public void run()
		{
			// TODO Auto-generated method stub
			if (run)
			{ 
				handler.postDelayed(this, 1000);
				second++;
				if(second >= 60) {
					minute++;
					second = 0;
				}
				if(minute >= 60) {
					hour++;
					minute = 0;
				}
			}
			time.setText(hour + getResources().getString(R.string.hour) + minute + getResources().getString(R.string.minute) + second + getResources().getString(R.string.second));
			stepNum = mService.getStepNum();
			stepNumText.setText(getResources().getString(R.string.bigstep) +":"+ String.valueOf(stepNum/2)+ "\n" + 
					getResources().getString(R.string.smallstep) + ":" + String.valueOf(stepNum));
			totalLength = (float) mService.getTotalLength();
			totalLengthText.setText(String.valueOf(totalLength));
			strideLength = (float) mService.getStrideLength();
			strideLengthText.setText(String.valueOf(strideLength));
			velocity = (float) mService.getVelocity();
			velocityText.setText(String.valueOf(velocity));
			
		}

	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.i(TAG, "PDRActivity onCreate()......");
		
		bindServiceBtn = (Button) findViewById(R.id.bindService);
		unbindServiceBtn = (Button) findViewById(R.id.unbindService);
		unbindServiceBtn.setEnabled(false);
		offlineBtn = (Button) findViewById(R.id.offlineBtn);
		offlineBtn.setEnabled(false);
		resetBtn = (Button) findViewById(R.id.reset);
		resetBtn.setEnabled(false);
		
		time = (TextView) findViewById(R.id.time);
		stepNumText = (TextView) findViewById(R.id.step_value);
		totalLengthText = (TextView) findViewById(R.id.distance_value);
		strideLengthText = (TextView) findViewById(R.id.strideLength_value);
		velocityText = (TextView) findViewById(R.id.velocity_value);
		
		accuracyStepNumText = (TextView) findViewById(R.id.step_value2);
		accuracyTotalLengthText = (TextView) findViewById(R.id.distance_value2);
		accuracyStrideLengthText = (TextView) findViewById(R.id.strideLength_value2);
		accuracyVelocityText = (TextView) findViewById(R.id.velocity_value2);
		
		bindServiceBtn.setOnClickListener(this);
		unbindServiceBtn.setOnClickListener(this);
		offlineBtn.setOnClickListener(this);
		resetBtn.setOnClickListener(this);

	}

	public void onClick(View view)
	{
		
		Log.i(TAG, "PDRActivity onClick()......");
		// TODO Auto-generated method stub
		switch (view.getId())
		{
		case R.id.bindService:
			Log.i(TAG, "PDRActivity BindService......");
			Intent intent = new Intent(this, PedometerService.class);
	        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			mBound = true;
			Toast.makeText(getBaseContext(), getResources().getString(R.string.startcounting), Toast.LENGTH_SHORT).show();
			time.setVisibility(VISIBLE);
			bindServiceBtn.setText(getResources().getString(R.string.counting));
			bindServiceBtn.setEnabled(false);
			unbindServiceBtn.setEnabled(true);
			offlineBtn.setEnabled(false);
			resetBtn.setEnabled(false);
			run = true;
			handler.postDelayed(task, 1000);
			mService.acquireWakeLock();
			mService.SensorEvent();
			sensorStoped = false;
			break;
			
		case R.id.unbindService:
			Log.i(TAG, "PDRActivity unBindService......");
			Toast.makeText(getBaseContext(), getResources().getString(R.string.stopcounting), Toast.LENGTH_SHORT).show();
			offlineBtn.setEnabled(true);
			resetBtn.setEnabled(true);
			bindServiceBtn.setEnabled(true);
			bindServiceBtn.setText(getResources().getString(R.string.continuecounting));	
			run = false;
			handler.post(task);			
				if(sensorStoped == false) 
				{
					mService.StopSensor();
					sensorStoped = true;
					mService.wakeLock.release();
				}	            	    
			break;
		case R.id.offlineBtn:
			Log.i(TAG, "PDRActivity Offline Computing......");
			Toast.makeText(getBaseContext(), getResources().getString(R.string.offlinecalculating), Toast.LENGTH_SHORT).show();
			accuracyStepNum = (int) mService.getAccuracyStepNumAnsTotalLength()[0];
			accuracyStepNumText.setText(getResources().getString(R.string.bigstep) + String.valueOf(accuracyStepNum/2)+ "\n" + 
                    getResources().getString(R.string.smallstep) + String.valueOf(accuracyStepNum));
			accuracyTotalLength = (float) mService.getAccuracyStepNumAnsTotalLength()[1];
			accuracyTotalLengthText.setText(String.valueOf(accuracyTotalLength));
			accuracyStrideLength = (float) mService.getAccuracyStrideLength();
			accuracyStrideLengthText.setText(String.valueOf(accuracyStrideLength));
			accuracyVelocity = (float) mService.getAccuracyVelocity();
			accuracyVelocityText.setText(String.valueOf(accuracyVelocity));			
			try {
				//FileIO.WriteToFile2(LOCALMEANACCELEROMETER, mService.getLocalMeanAccelerometer());
				FileIO.WriteToFile(VELOCITYFILE, mService.getVelocity1(), 4);
				FileIO.WriteToFile(ACCELEROMETERFILE, mService.getRotatedAccelerometer(), 4);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.reset:
			Log.i(TAG, "PDRActivity reset......");
			Toast.makeText(getBaseContext(), getResources().getString(R.string.reset), Toast.LENGTH_SHORT).show();
			mService.Reset();
			stepNumText.setText("");
			totalLengthText.setText("");
			strideLengthText.setText("");
			velocityText.setText("");
			accuracyStepNumText.setText("");
			accuracyTotalLengthText.setText("");
			accuracyStrideLengthText.setText("");
			accuracyVelocityText.setText("");
			unbindServiceBtn.setEnabled(false);
			offlineBtn.setEnabled(false);
			resetBtn.setEnabled(false);
			second = 0;
			minute = 0;
			hour = 0;
			if(sensorStoped == false) 
			{
				mService.StopSensor();
				sensorStoped = true;
				mService.wakeLock.release();
			}	
			time.setVisibility(INVISIBLE);
			bindServiceBtn.setEnabled(true);
			bindServiceBtn.setText(getResources().getString(R.string.startcounting));
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
	    case R.id.setting:
	        return true;
	    case R.id.about:
	    	return true;
	    case R.id.quit:	    	
			Log.i(TAG, "PDRActivity onQuitCount()......");
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage(getResources().getString(R.string.suretoquit)).
	    	setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Toast.makeText(getBaseContext(), getResources().getString(R.string.quit), Toast.LENGTH_SHORT).show();
					finish();					
				}
			}).
			setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			}).show();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		 // Bind to LocalService
		Intent intent = new Intent(this, PedometerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        }

	 /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	PedometerBinder binder = (PedometerBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		Log.i(TAG, "PDRActivity onDestroy()......");
		if(sensorStoped == false) 
		{
			mService.StopSensor();
			sensorStoped = true;
			mService.wakeLock.release();
		}	
		if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
		super.onDestroy();
	}
}