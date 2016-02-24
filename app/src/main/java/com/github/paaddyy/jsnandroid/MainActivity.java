package com.github.paaddyy.jsnandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate {

	private DatabaseHandler mDBHandler;
	private Button _search	,_reserve,_unreserve;
	private EditText _field;
	private TextView _status;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	private static final String TAG = MainActivity.class.getSimpleName();

	private ARDiscoveryService ardiscoveryService;
	private boolean ardiscoveryServiceBound = false;
	private ServiceConnection ardiscoveryServiceConnection;
	public IBinder discoveryServiceBinder;

	private ListView listView ;
	private List<ARDiscoveryDeviceService> deviceList;
	private String[] deviceNameList;

	private BroadcastReceiver ardiscoveryServicesDevicesListUpdatedReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ARSDK.loadSDKLibs();

		//JUMPING SUMO
		initBroadcastReceiver();
		initServiceConnection();

		listView = (ListView) findViewById(R.id.list);

		deviceList = new ArrayList<ARDiscoveryDeviceService>();
		deviceNameList = new String[]{};

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, deviceNameList);


		// Assign adapter to ListView
		listView.setAdapter(adapter);

		//ListView Item Click Listener
		/*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				ARDiscoveryDeviceService service = deviceList.get(position);

				Intent intent = new Intent(MainActivity.this, PilotingActivity.class);
				intent.putExtra(PilotingActivity.EXTRA_DEVICE_SERVICE, service);


				startActivity(intent);
			}

		});*/

		mDBHandler = new DatabaseHandler(this);
		_search = (Button) findViewById(R.id.button1);
		_reserve = (Button) findViewById(R.id.button2);
		_unreserve = (Button) findViewById(R.id.button3);

		_field = (EditText) findViewById(R.id.editText1);
		_status = (TextView) findViewById(R.id.textView1);

		_search.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				searchSpace();
			}
		});

		_reserve.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				_un_reserveSpace(Integer.parseInt(_field.getText().toString()));

			}
		});

		_unreserve.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				_un_reserveSpace(Integer.parseInt(_field.getText().toString()));
			}
		});

		_field.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (_field.getText().toString().isEmpty() || Integer.parseInt(_field.getText().toString()) > 9) {
					return;
				}
				getStatusOfSpace(Integer.parseInt(_field.getText().toString()));
			}
		});

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

	private void searchSpace() {
		boolean success = false;
		int space = 0;
		space = mDBHandler.getFreeSpace();

		if (space != 0) {
			success = true;
		}

		if (success) {
			Toast.makeText(getApplicationContext(), "FOUND AVAILABLE SPACE: " + space, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(), "NO AVAILABLE SPACE FOUND", Toast.LENGTH_LONG).show();
		}
	}

	private void changeStatusofSpace(String space) {
		int new_value = mDBHandler.changeStatusOfSpace(space);

		updateStatusGUI(new_value);
	}

	private void updateStatusGUI(int flag) {
		if (flag == 1) {
			_status.setText("FREI!");
		} else if (flag == 0) {
			_status.setText("BELEGT!");
		}
	}

	private void _un_reserveSpace(int space) {
		changeStatusofSpace(String.valueOf(space));
	}

	private void getStatusOfSpace(int space) {
		updateStatusGUI(mDBHandler.getStatusOfSpace(String.valueOf(space)));
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.github.paaddyy.jsnandroid/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.github.paaddyy.jsnandroid/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}

	private void initBroadcastReceiver()
	{
		ardiscoveryServicesDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(this);
	}

	@Override
	public void onServicesDevicesListUpdated() {
		Log.d(TAG, "onServicesDevicesListUpdated ...");

		List<ARDiscoveryDeviceService> list;

		if (ardiscoveryService != null)
		{
			list = ardiscoveryService.getDeviceServicesArray();

			deviceList = new ArrayList<ARDiscoveryDeviceService>();
			List<String> deviceNames = new ArrayList<String>();

			if(list != null)
			{
				for (ARDiscoveryDeviceService service : list)
				{
					Log.e(TAG, "service :  "+ service + " name = " + service.getName());
					ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());
					Log.e(TAG, "product :  "+ product);
					// only display Jumping Sumo
					if (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS.equals(product))
					{
						deviceList.add(service);
						deviceNames.add(service.getName());
					}
				}
			}

			deviceNameList = deviceNames.toArray(new String[deviceNames.size()]);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, deviceNameList);

			// Assign adapter to ListView
			listView.setAdapter(adapter);
		}
	}

	private void initServiceConnection()
	{
		ardiscoveryServiceConnection = new ServiceConnection()
		{
			@Override
			public void onServiceConnected(ComponentName name, IBinder service)
			{
				discoveryServiceBinder = service;
				ardiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();
				ardiscoveryServiceBound = true;

				ardiscoveryService.start();
			}

			@Override
			public void onServiceDisconnected(ComponentName name)
			{
				ardiscoveryService = null;
				ardiscoveryServiceBound = false;
			}
		};
	}
}
