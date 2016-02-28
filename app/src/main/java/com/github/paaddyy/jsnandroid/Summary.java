package com.github.paaddyy.jsnandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by paddy on 28.02.2016.
 */
public class Summary extends Activity{

    public static String EXTRA_SPACE = "sumoParrot.extra.space";

    private Button submit;
    private TextView spaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Set Layout
        setContentView(R.layout.activity_summary);

        Intent intent = getIntent();
        Integer space = intent.getParcelableExtra(EXTRA_SPACE);

        spaceView = (TextView) findViewById(R.id.spaceNumber);
        spaceView.setText(space);
        submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
