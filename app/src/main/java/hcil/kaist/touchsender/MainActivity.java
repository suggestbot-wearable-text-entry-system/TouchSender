package hcil.kaist.touchsender;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import bluetooth.BluetoothChatService;
import bluetooth.DeviceListActivity;
import graphic.GraphicView;


public class MainActivity extends Activity {
    private boolean isMOVEenable;

    private final int COMM_METHOD_BLUETOOTH = 1;
    private final int COMM_METHOD_SERIAL = 2;

    private int commMethod = COMM_METHOD_BLUETOOTH;
    private TextView mTextSerial = null;

    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_WRITE_DOWN = 30;
    public static final int MESSAGE_WRITE_MOVE = 31;
    public static final int MESSAGE_WRITE_UP = 32;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECT = 6;
    public static final int MESSAGE_VIBRATE = 7;
    public static final int MESSAGE_END = 8;
    public static final int MESSAGE_TOAST2 = 9;
    public static final int MESSAGE_PHASE = 10;
    public static final int MESSAGE_PHASE_SET_2 = 20;
    public static final int MESSAGE_TASK = 11;
    public static final int MESSAGE_EYE_CONDITION= 12;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private GraphicView gv;
    private CountDownTimer cdt;
    private boolean end = false;

    private long current, prev;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    private Vibrator vib;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);


        vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        gv = (GraphicView)findViewById(R.id.graphicView);
        gv.initialize(this, mHandler);
        isMOVEenable = false;

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }

    }


    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
        ensureDiscoverable();
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the compose field with a listener for the return key


        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }


    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }


    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_CONNECT:
                    switch(commMethod){
                        case COMM_METHOD_SERIAL:
                            break;
                        case COMM_METHOD_BLUETOOTH:
                            Intent serverIntent = null;
                            // Launch the DeviceListActivity to see devices and do scan
                            serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                            break;

                    }


                    break;
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    mChatService.write(writeBuf);
                    break;
                case MESSAGE_WRITE_MOVE:
                    /*if(isMOVEenable) {
                        byte[] writeBuf1 = (byte[]) msg.obj;
                        // construct a string from the buffer
                        // Log.d("WRITE", new String(writeBuf));
                        mChatService.write((byte[]) msg.obj);
                        writeBuf1 = null;
                    }*/
                    mChatService.write((byte[]) msg.obj);
                    break;
                case MESSAGE_WRITE_DOWN:
                    /*
                    if(hasMessages(MESSAGE_WRITE_MOVE)){
                        removeMessages(MESSAGE_WRITE_MOVE);
                    }*/
                    //byte[] writeBuf2 = (byte[]) msg.obj;
                    // construct a string from the buffer
                    // Log.d("WRITE", new String(writeBuf));
                    //Log.d("WRITE", new String(writeBuf2));
                    mChatService.write((byte[]) msg.obj);
                    //writeBuf2 = null;
                    //isMOVEenable = true;
                    break;
                case MESSAGE_WRITE_UP:
                    /*
                    isMOVEenable = false;
                    if(hasMessages(MESSAGE_WRITE_MOVE)){
                        removeMessages(MESSAGE_WRITE_MOVE);
                    }*/
                    //byte[] writeBuf3 = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //String aaa  = new String(writeBuf3);
                    //mChatService.flushOutput();
                    mChatService.write((byte[]) msg.obj);
                    //writeBuf3 = null;
                    break;
                case MESSAGE_READ:

                    vib.vibrate(30);
                    //gv.getInputHandler().obtainMessage(InputManager.FROM_BASE,msg.arg1,msg.arg2,msg.obj).sendToTarget();
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), (String)msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST2:
                    /*
                    if(msg.arg1 > -1){
                        switch (msg.arg2){
                            case SegmentAnalyser.STAR:
                                Toast.makeText(getApplicationContext(), "STAR!",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case SegmentAnalyser.V:
                                Toast.makeText(getApplicationContext(), "V !",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case SegmentAnalyser.DELETE:
                                Toast.makeText(getApplicationContext(), "DELETE!",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case SegmentAnalyser.PIG_TAIL:
                                Toast.makeText(getApplicationContext(), "PIG TAIL!",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case SegmentAnalyser.LINE:
                                switch (msg.arg1){
                                    case SegmentAnalyser.up:
                                        Toast.makeText(getApplicationContext(), "up!",
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    case SegmentAnalyser.down:
                                        Toast.makeText(getApplicationContext(), "down!",
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    case SegmentAnalyser.right:
                                        Toast.makeText(getApplicationContext(), "right!",
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    case SegmentAnalyser.left:
                                        Toast.makeText(getApplicationContext(), "left!",
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    case SegmentAnalyser.up_left:
                                        Toast.makeText(getApplicationContext(), "up_left!",
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    case SegmentAnalyser.up_right:
                                        Toast.makeText(getApplicationContext(), "up_right!",
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    case SegmentAnalyser.down_right:
                                        Toast.makeText(getApplicationContext(), "down_right!",
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    case SegmentAnalyser.down_left:
                                        Toast.makeText(getApplicationContext(), "down_left!",
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                }

                                break;

                        }
                    }
                    */
                    String whatis = "s " + msg.arg1 +" "+msg.arg2 + "\r\n";
                    byte[] writeBuf4 = whatis.getBytes();
                    // construct a string from the buffer
                    mChatService.write(writeBuf4);
                    whatis = null;
                    writeBuf4 = null;
                    break;
                case MESSAGE_VIBRATE:
                    vib.vibrate(msg.arg2);
                    break;
                case MESSAGE_END:
                    end = true;
                    vib.vibrate(100);
                    Toast.makeText(getApplicationContext(), "End!",
                            Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                    break;
                case MESSAGE_PHASE:
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }


}
