package ouc.pdr.activity;

import will.stepcounter.R;
import ouc.pdr.service.PDRService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PDRActivity extends Activity implements OnClickListener
{

	private Button startBtn = null;
	private Button stopBtn = null;
	private TextView time = null;
	private Button quitBtn = null;
	private Button handledataBtn = null;

	private EditText accelerometerText = null;
	private EditText orientationText = null;
	private EditText primitiveAccelerometerText = null;

	long curTime = 0;
	long lastTime = 0;
	private long count = 0;

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
				count++;
			}
			time.setText("" + count);
		}

	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.i("PDRActivity", "PDRActivity onCreate()......");
		
		startBtn = (Button) findViewById(R.id.start);
		stopBtn = (Button) findViewById(R.id.stop);
		time = (TextView) findViewById(R.id.time);
		quitBtn = (Button) findViewById(R.id.quit);
		handledataBtn = (Button)findViewById(R.id.handledata);

		accelerometerText = (EditText) findViewById(R.id.accelerometer);
		orientationText = (EditText) findViewById(R.id.orientation);
		primitiveAccelerometerText = (EditText) findViewById(R.id.primitiveaccelerometer);
		
		startBtn.setOnClickListener(this);
		stopBtn.setOnClickListener(this);
		quitBtn.setOnClickListener(this);
		handledataBtn.setOnClickListener(this);

	}

	public void onClick(View view)
	{
		
		Log.i("PDRActivity", "PDRActivity onClick()......");
		// TODO Auto-generated method stub
		switch (view.getId())
		{
		//PDR开始工作
		case R.id.start:
			Toast.makeText(getBaseContext(), "开始计数", Toast.LENGTH_SHORT).show();
			Intent startIntent = new Intent();
			
			// 将数据添加到Bundle中
			Bundle bundle = new Bundle();
			bundle.putString("accelerometer", accelerometerText.getText().toString());
			bundle.putString("orientation", orientationText.getText().toString());
			bundle.putString("primitiveAccelerometer", primitiveAccelerometerText.getText().toString());
			// 将Bundle中的数据绑在Intent上
			startIntent.putExtras(bundle);

			startIntent.setClass(getBaseContext(), PDRService.class);
			startService(startIntent);
			//开始采集数据时，计时器同时也开始工作
			run = true;
			handler.postDelayed(task, 1000);
			Log.i("PDRActivity", "PDRActivity onStartCount()......");
			break;
			
		case R.id.stop:
			Toast.makeText(getBaseContext(), "停止计数", Toast.LENGTH_SHORT).show();
			Intent stopIntent = new Intent();
			stopIntent.setClass(getBaseContext(), PDRService.class);
			stopService(stopIntent);

			run = false;
			handler.post(task);
			Log.i("PDRActivity", "PDRActivity onStopCount()......");
			break;
			
		case R.id.quit:
			Toast.makeText(getBaseContext(), "退出程序", Toast.LENGTH_SHORT).show();
			Log.i("PDRActivity", "PDRActivity onQuitCount()......");
			finish();
			break;
			
		case R.id.handledata:
			Toast.makeText(getBaseContext(), "处理数据", Toast.LENGTH_SHORT).show();
			Intent handledata = new Intent();
			handledata.setClass(getBaseContext(), HandleDataActivity.class);
			startActivity(handledata);
			break;
		}
	}
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		Log.i("PDRActivity", "PDRActivity onDestroy()......");
		super.onDestroy();
	}

}