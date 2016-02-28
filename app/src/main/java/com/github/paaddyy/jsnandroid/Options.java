package com.github.paaddyy.jsnandroid;

import android.app.Activity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;

import org.opencv.android.OpenCVLoader;

import java.util.List;

/**
 * Created by paddy on 28.02.2016.
 */
public class Options extends Activity{

    private Button automatic;
    private Button manual;
    private DatabaseHandler mDBHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Set Layout
        setContentView(R.layout.activity_options);

        mDBHandler = new DatabaseHandler(getApplicationContext());
        automatic = (Button) findViewById(R.id.automatic);
        manual = (Button) findViewById(R.id.manual);

        automatic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Options.this, Summary.class);
                intent.putExtra(Summary.EXTRA_SPACE, searchSpace());

                startActivity(intent);
            }
        });

        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private Integer searchSpace() {
        int space = -1;
        space = mDBHandler.getFreeSpace();

        return space;
    }

}
