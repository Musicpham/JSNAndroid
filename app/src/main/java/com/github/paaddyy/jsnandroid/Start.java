package com.github.paaddyy.jsnandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parrot.arsdk.ARSDK;
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
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Created by paddy on 28.02.2016.
 */
public class Start extends Activity{

    public Session session;
    public DatabaseHandler mDBHandler;

    public ARDiscoveryDeviceService service;
    public ARDiscoveryDevice device;
    public ARDeviceController deviceController;
    public String product;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("opencv", "opencv initialization failed!");
        } else {
            Log.i("opencv", "opencv initialization successul!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);

        session = new Session(getApplicationContext());
        mDBHandler = new DatabaseHandler(getApplicationContext());

        if (savedInstanceState == null) {
            //CREATE FIRST FRAGMENT VIEW
            getFragmentManager().beginTransaction().add(R.id.container, new ConnectFragment()).commit();
        }

    }

    public class ConnectFragment extends Fragment implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate {
        private TextView status;
        private Button connect;
        private ProgressBar loadingSpinner;

        private ARDiscoveryService mArdiscoveryService;
        private ServiceConnection mArdiscoveryServiceConnection;
        private List<ARDiscoveryDeviceService> deviceList;

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if(isVisibleToUser) {
                Activity a = getActivity();
                if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        //CONSTRUCTOR
        public ConnectFragment() {
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.activity_connect, container, false);

            //Load ARSDK-Libs
            ARSDK.loadSDKLibs();

            //GET GUI-ELEMENTS
            status = (TextView)rootView.findViewById(R.id.status);
            connect = (Button)rootView.findViewById(R.id.connect);
            loadingSpinner = (ProgressBar) rootView.findViewById(R.id.loadingSpinner);

            //INITIALIZE GUI
            connect.setEnabled(false);
            status.setText("Warten...");

            //INIT SUMO DISCOVERY SERVICE AND REGISTER RECEIVERS
            initDiscoveryService();
            registerReceivers();

            connect.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    connect.setBackgroundResource(R.drawable.ghost_button_filled);
                    service = deviceList.get(0);
                    product = service.getName();

                    MenuFragment newFragment = new MenuFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();

                    transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack if needed
                    transaction.replace(R.id.container, newFragment);
                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();
                }
            });

            return rootView;
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
                Intent i = new Intent(getActivity(), ARDiscoveryService.class);
                getActivity().bindService(i, mArdiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
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
            LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getActivity());
            localBroadcastMgr.registerReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
        }

        @Override
        public void onServicesDevicesListUpdated() {

            if (mArdiscoveryService != null)
            {
                deviceList = mArdiscoveryService.getDeviceServicesArray();

                if(deviceList != null)
                {
                    status.setText("Fertig!");
                    connect.setEnabled(true);
                    connect.setVisibility(View.VISIBLE);
                }

            }
        }
    }

    public class MenuFragment extends Fragment {

        private TextView name;

        private Button reserve;
        private Button unreserve;
        private Button status;
        private Button close;

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if(isVisibleToUser) {
                Activity a = getActivity();
                if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_menu, container, false);

            name = (TextView) rootView.findViewById(R.id.name);
            name.setText(product);

            close = (Button) rootView.findViewById(R.id.close);
            status = (Button) rootView.findViewById(R.id.status);
            reserve = (Button) rootView.findViewById(R.id.reserve);
            unreserve = (Button) rootView.findViewById(R.id.unreserve);

            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

            status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //
                }
            });

            reserve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OptionFragment newFragment = new OptionFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();

                    transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack if needed
                    transaction.replace(R.id.container, newFragment);
                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();
                }
            });

            unreserve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //
                }
            });

            return rootView;
        }
    }

    public class OptionFragment extends Fragment {
        private TextView name;

        private Button automatic;
        private Button manual;

        DatabaseHandler mDBHandler;

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if(isVisibleToUser) {
                Activity a = getActivity();
                if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_options, container, false);

            name = (TextView) rootView.findViewById(R.id.name);
            name.setText(product);

            mDBHandler = new DatabaseHandler(getActivity());

            automatic = (Button) rootView.findViewById(R.id.automatic);
            manual = (Button) rootView.findViewById(R.id.manual);

            manual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ManualChoiceFragment newFragment = new ManualChoiceFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();

                    transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack if needed
                    transaction.replace(R.id.container, newFragment);
                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();
                }
            });

            automatic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int space = -1;
                    space = mDBHandler.getFreeSpace();

                    session.setSpace(String.valueOf(space));

                    SummaryFragment newFragment = new SummaryFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();

                    transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack if needed
                    transaction.replace(R.id.container, newFragment);
                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();

                }
            });
            return rootView;
        }
    }

    public class SummaryFragment extends Fragment {

        private TextView name;

        private TextView txtSpace;
        private Button submit;
        private Button new_space;

        private String space;

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if(isVisibleToUser) {
                Activity a = getActivity();
                if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_summary, container, false);

            name = (TextView) rootView.findViewById(R.id.name);
            name.setText(product);

            space = session.getSpace();

            txtSpace = (TextView) rootView.findViewById(R.id.spaceNumber);
            txtSpace.setText(space);

            submit = (Button) rootView.findViewById(R.id.submit);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SumoParrot.class);
                    intent.putExtra(SumoParrot.EXTRA_DEVICE_SERVICE, service);


                    startActivity(intent);
                }
            });

            new_space = (Button) rootView.findViewById(R.id.new_space);
            new_space.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer Intspace = mDBHandler.getFreeSpace();
                    txtSpace.setText(String.valueOf(Intspace));
                    session.setSpace(String.valueOf(Intspace));
                }
            });

            return rootView;
        }
    }

    public class ManualChoiceFragment extends Fragment{
        private TextView name;

        private EditText spaceNumberManual;
        private Button submitManual;
        private TextView statusManual;

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if(isVisibleToUser) {
                Activity a = getActivity();
                if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_manualchoice, container, false);

            name = (TextView) rootView.findViewById(R.id.name);
            name.setText(product);

            spaceNumberManual = (EditText) rootView.findViewById(R.id.spaceNumberManual);
            submitManual = (Button) rootView.findViewById(R.id.submitManual);
            statusManual = (TextView) rootView.findViewById(R.id.statusManual);

            submitManual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SummaryFragment newFragment = new SummaryFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();

                    transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack if needed
                    transaction.replace(R.id.container, newFragment);
                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();
                }
            });

            return rootView;
        }
    }


}

