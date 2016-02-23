package com.github.paaddyy.jsnandroid;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parrot.arsdk.ARSDK;


public class MainActivity extends Activity {

	//ARSDK.loadSDKLibs();

	private DatabaseHandler mDBHandler;
	private Button _search	,_reserve,_unreserve;
	private EditText _field;
	private TextView _status;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
				if (_field.getText().toString().isEmpty()) {
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
}
