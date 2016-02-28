package com.github.paaddyy.jsnandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;

import java.io.ByteArrayInputStream;

/**
 * Created by _paaddy_ on 24.02.2016.
 */
public class SumoParrot extends Activity implements ARDeviceControllerListener, ARDeviceControllerStreamListener {

    private static String TAG = SumoParrot.class.getSimpleName();
    public static String EXTRA_DEVICE_SERVICE = "sumoParrot.extra.device.service";

    public ARDeviceController deviceController;
    public ARDiscoveryDeviceService service;
    public ARDiscoveryDevice device;

    private Button jumHightBt;
    private Button jumLongBt;

    private Button turnLeftBt;
    private Button turnRightBt;

    private Button forwardBt;
    private Button backBt;

    private TextView batteryLabel;

    private AlertDialog alertDialog;

    private ImageView imgView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //SET LAYOUT
        setContentView(R.layout.activity_sumo);

        initIHM();
        initVideo();

        Intent intent = getIntent();
        service = intent.getParcelableExtra(EXTRA_DEVICE_SERVICE);

        //create the device
        try {
            device = new ARDiscoveryDevice();

            ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();

            device.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
        } catch (ARDiscoveryException e) {
            e.printStackTrace();
            Log.e(TAG, "Error: " + e.getError());
        }

        if (device != null) {
            try {
                //create the deviceController
                deviceController = new ARDeviceController(device);
                deviceController.addListener(this);
                deviceController.addStreamListener(this);
            } catch (ARControllerException e) {
                e.printStackTrace();
            }
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initIHM() {
        jumHightBt = (Button) findViewById(R.id.jumHightBt);
        jumHightBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().sendAnimationsJump(ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_HIGH);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        jumLongBt = (Button) findViewById(R.id.jumLongBt);
        jumLongBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().sendAnimationsJump(ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_LONG);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });


        turnRightBt = (Button) findViewById(R.id.turnRightBt);
        turnRightBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 50);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        turnLeftBt = (Button) findViewById(R.id.turnLeftBt);
        turnLeftBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) -50);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        forwardBt = (Button) findViewById(R.id.forwardBt);
        forwardBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 50);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 0);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        backBt = (Button) findViewById(R.id.backBt);
        backBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) -50);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 0);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        batteryLabel = (TextView) findViewById(R.id.batteryLabel);
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();

        //start the deviceController
        if (deviceController != null) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SumoParrot.this);

            // set title
            alertDialogBuilder.setTitle("Connecting ...");


            // create alert dialog
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            ARCONTROLLER_ERROR_ENUM error = deviceController.start();

            if (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
                finish();
            }
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "SumoParrot Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.github.paaddyy.jsnandroid/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    private void stopDeviceController() {
        if (deviceController != null) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SumoParrot.this);

            // set title
            alertDialogBuilder.setTitle("Disconnecting ...");

            // show it
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // create alert dialog
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    ARCONTROLLER_ERROR_ENUM error = deviceController.stop();

                    if (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
                        finish();
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        if (deviceController != null) {
            deviceController.stop();
        }

        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "SumoParrot Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.github.paaddyy.jsnandroid/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    public void onBackPressed() {
        stopDeviceController();
    }

    public void onUpdateBattery(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryLabel.setText(String.format("%d%%", percent));
            }
        });
    }


    @Override
    public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error) {
        Log.i(TAG, "onStateChanged ... newState:" + newState + " error: " + error);

        switch (newState) {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                //The deviceController is started
                Log.i(TAG, "ARCONTROLLER_DEVICE_STATE_RUNNING .....");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //alertDialog.hide();
                        alertDialog.dismiss();
                    }
                });
                deviceController.getFeatureJumpingSumo().sendMediaStreamingVideoEnable((byte) 1);
                break;

            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                //The deviceController is stoped
                Log.i(TAG, "ARCONTROLLER_DEVICE_STATE_STOPPED .....");

                deviceController.dispose();
                deviceController = null;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //alertDialog.hide();
                        alertDialog.dismiss();
                        finish();
                    }
                });
                break;

            default:
                break;
        }
    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error) {
        // Nothing to do
    }


    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {
        if (elementDictionary != null) {
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED) {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);

                if (args != null) {
                    Integer batValue = (Integer) args.get("arcontroller_dictionary_key_common_commonstate_batterystatechanged_percent");

                    onUpdateBattery(batValue);
                }
            }
        } else {
            Log.e(TAG, "elementDictionary is null");
        }
    }

    @Override
    public ARCONTROLLER_ERROR_ENUM configureDecoder(ARDeviceController deviceController, ARControllerCodec codec) {
        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }

    @Override
    public ARCONTROLLER_ERROR_ENUM onFrameReceived(ARDeviceController deviceController, ARFrame frame) {
        if (!frame.isIFrame())
            return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR_STREAM;

        byte[] data = frame.getByteData();
        ByteArrayInputStream ins = new ByteArrayInputStream(data);
        Bitmap bmp = BitmapFactory.decodeStream(ins);

        FrameDisplay fDisplay = new FrameDisplay(imgView, bmp);
        fDisplay.execute();

        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }


    @Override
    public void onFrameTimeout(ARDeviceController deviceController) {
        Log.i(TAG, "onFrameTimeout ..... ");
    }

    //region video
    public void initVideo() {
        imgView = (ImageView) findViewById(R.id.imageView);
    }

    //endregion video
}
