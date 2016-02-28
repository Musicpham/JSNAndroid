package com.github.paaddyy.jsnandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate {

	static {
		if (!OpenCVLoader.initDebug()) {
			Log.i("opencv", "opencv initialization failed!");
		} else {
			Log.i("opencv", "opencv initialization successul!");
		}
	}

	private TextView status;
	private Button connect;
	private ProgressBar loadingSpinner;

	private ARDiscoveryService mArdiscoveryService;
	private ServiceConnection mArdiscoveryServiceConnection;
	private List<ARDiscoveryDeviceService> deviceList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//Set Layout
		setContentView(R.layout.activity_main);
		//Load ARSDK-Libs
		ARSDK.loadSDKLibs();

		//GET GUI-ELEMENTS
		status = (TextView)findViewById(R.id.status);
		connect = (Button)findViewById(R.id.connect);
		loadingSpinner = (ProgressBar) findViewById(R.id.loadingSpinner);

		//INITIALIZE GUI
		connect.setEnabled(false);
		status.setText("Warte auf Verbindung...");
		loadingSpinner.setVisibility(View.VISIBLE);

		//INIT SUMO DISCOVERY SERVICE AND REGISTER RECEIVERS
		initDiscoveryService();
		registerReceivers();

		connect.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				ARDiscoveryDeviceService service = deviceList.get(0);

				Intent intent = new Intent(MainActivity.this, Options.class);

				/*
				Intent intent = new Intent(MainActivity.this, SumoParrot.class);
				intent.putExtra(SumoParrot.EXTRA_DEVICE_SERVICE, service); //über Session regeln*/

				startActivity(intent);
			}
		});
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initDiscoveryService()
	{
		// create the service connection
		if (mArdiscoveryServiceConnection == null)
		{
			mArdiscoveryServiceConnection = new ServiceConnection()
			{
				@Override
				public void onServiceConnected(ComponentName name, IBinder service)
				{
					mArdiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();

					startDiscovery();
				}

				@Override
				public void onServiceDisconnected(ComponentName name)
				{
					mArdiscoveryService = null;
				}
			};
		}

		if (mArdiscoveryService == null)
		{
			// if the discovery service doesn't exists, bind to it
			Intent i = new Intent(getApplicationContext(), ARDiscoveryService.class);
			getApplicationContext().bindService(i, mArdiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
		}
		else
		{
			// if the discovery service already exists, start discovery
			startDiscovery();
		}
	}

	private void startDiscovery()
	{
		if (mArdiscoveryService != null)
		{
			mArdiscoveryService.start();
		}
	}

	private void registerReceivers()
	{
		ARDiscoveryServicesDevicesListUpdatedReceiver mArdiscoveryServicesDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(this);
		LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
		localBroadcastMgr.registerReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
	}

	@Override
	public void onServicesDevicesListUpdated() {

		if (mArdiscoveryService != null)
		{
			deviceList = mArdiscoveryService.getDeviceServicesArray();

			if(deviceList != null)
			{
				status.setText("Verbindung bereit!");
				loadingSpinner.setVisibility(View.GONE);
				connect.setEnabled(true);
			}

		}
	}
}