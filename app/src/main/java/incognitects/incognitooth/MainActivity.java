package incognitects.incognitooth;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public class MainActivity extends Activity {
    // Debugging
    private static final String TAG = "incognitooth";
    private static final boolean D = true;

    private Queue<BluetoothDevice> peers = new LinkedList<BluetoothDevice>();

    // Name of the connected device
    private String mConnectedDeviceAddress;
    private String mConnectedDeviceName;

    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBTService = null;
    private PacketStore pstore;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String DEVICE_NAME = "device_name";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MAX_MESSAGE_SIZE = 117;         //Max number of bytes that can be entered as a message

    private ArrayAdapter<String> keysAdapter;

    public TextView tv;
    public TextView tvNumChar;
    public EditText editTextMsg;
    public Button buttonSend;
    private Button openInbox;
    private Button buttonAddKey;


    private BluetoothDevice activePeer;
    private String selectedRecipient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.recipientsListTextView);
        tv.setText("select recipient");

        //Indicator that shows how many characters were entered
        tvNumChar = (TextView) findViewById(R.id.textViewNumChar);

        //Text field to enter the message
        editTextMsg = (EditText) findViewById(R.id.editTextMsg);
        editTextMsg.addTextChangedListener(new android.text.TextWatcher() {
            public void afterTextChanged(android.text.Editable s) {
                int len = editTextMsg.getText().length();
                if (len > MAX_MESSAGE_SIZE) {
                    editTextMsg.setText(editTextMsg.getText().subSequence(0, MAX_MESSAGE_SIZE));
                    len = MAX_MESSAGE_SIZE;
                    editTextMsg.setSelection(MAX_MESSAGE_SIZE);
                }
                tvNumChar.setText(Integer.toString(len));

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        //List that shows the available recipients
        keysAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        ListView listV = (ListView) findViewById(R.id.listView);
        listV.setAdapter(keysAdapter);
        listV.setItemsCanFocus(true);

        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) listV.getLayoutParams();
        lp.height = 300;
        listV.setLayoutParams(lp);
        listV.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

        public void onItemClick(android.widget.AdapterView<?> parentAdapter, View view, int position,
                                    long id) {
                // We know the View is a TextView so we can cast it
                TextView clickedView = (TextView) view;
                selectedRecipient = clickedView.getText().toString();
                //...... if entry of list has been clicked do sth with clickedView here
            }
        });

        buttonSend = (Button) findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Packet newPacket = new Packet(selectedRecipient, editTextMsg.getText().toString());
                pstore.encrypt(newPacket);
                pstore.add(newPacket);
            }
        });

        openInbox = (Button) findViewById(R.id.openInbox);
        openInbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), inboxActivity.class);
                startActivity(i);
            }
        });

        buttonAddKey = (Button) findViewById(R.id.buttonAddKey);
        buttonAddKey.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), AddKey.class);
                startActivity(i);
            }
        });

        SharedPreferences keyStore = getSharedPreferences("KEYSTORE", 0);
        EncryptUtil encryptUtil = new EncryptUtil(keyStore);

        if(!keyStore.contains(EncryptUtil.PRIVATE_KEY) || !keyStore.contains(EncryptUtil.PUBLIC_KEY)) {
            encryptUtil.generateKey();
        }

        pstore = new PacketStore(getSharedPreferences("PACKETS", 0), encryptUtil);
        //pstore.add(new Packet("phipp", "This is fun."));
        //pstore.add(new Packet("etienned", "Hi!"));

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
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
        } else {
            setupRelay();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        pstore.load();

        keysAdapter.clear();
        SharedPreferences prefs = getSharedPreferences("NICK_KEYSTORE", 0);
        Map<String, ?> keys = prefs.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            keysAdapter.add(entry.getKey());
        }

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBTService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth services
                mBTService.start();
            }
        }
    }

    private void setupRelay() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        ensureDiscoverable();
        mBTService = new BluetoothService(this, mHandler);
        relay();
    }

    @Override
    public void onPause() {
        super.onPause();
        pstore.store();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(D) Log.e(TAG, "--- ON DESTROY ---");

        // Stop the Bluetooth chat services
        if (mBTService != null) mBTService.stop();
        unregisterReceiver(mReceiver);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    setupRelay();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "BT not enabled...", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0); // 0 = ALL the time
            startActivity(discoverableIntent);
        }
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

    private void sendMsg(String message) {
        // Check that we're actually connected before trying anything
        int tries = 0;
        boolean success = false;
        while(!success && tries < 10 ){
            success = (mBTService.getState() == BluetoothService.STATE_CONNECTED);
            tries++;
        }
        if (!success) {
            return;
        }

        Log.d(TAG, "Attempting to send message " + message);
        // Check that there's actually something to send
        if (message.length() > 0) {
            setStatus("Sending....");
            //Toast.makeText(getApplicationContext(), "Sending msg "+message+".", Toast.LENGTH_SHORT).show();
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBTService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    public void relay() {
        //if (peers.size() == 0) {
        //boolean success = this.mBluetoothAdapter.startDiscovery();
        int tries = 0;
        boolean success = false;

        if (mBTService.getState() > 1){
            return;
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        while (!success && tries < 5) {
            success = mBluetoothAdapter.startDiscovery();
            tries++;
        }

        if (!success) {
            setStatus("Could not start Peering!");
            Toast.makeText(getApplicationContext(), "Could not start Discovery.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Peering...", Toast.LENGTH_LONG).show();
        }

        //}
        //else {
        //    initConnection();
        //}
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("[PEERING]", "Checking peer "+device.getName());
                if (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART) {
                    Log.d("[PEERING]", "Found a smartphone. Updating peers.");
                    mBluetoothAdapter.cancelDiscovery();
                    activePeer = device;
                    mBTService.connect(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                relay();
            }
        }
    };

    private void initConnection() {
        mBTService.connect(peers.poll());
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    //if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus("Connected to: " + mConnectedDeviceName);

                            // Send everything we have
                            Log.d(TAG, "Sending " + pstore.packets.size() + " packets.");
                            for(Packet p : pstore.packets){
                                // Only send to this device if we haven't sent
                                // the same message before
                                //if (!p.deliveredTo.contains(mConnectedDeviceAddress)) {
                                sendMsg(p.serialize());
                                //    p.deliveredTo.add(mConnectedDeviceAddress);
                               // }
                            }
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus("Connecting...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                            setStatus("Listening");
                            break;
                        case BluetoothService.STATE_NONE:
                            setStatus("Not connected!");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    // Writes are finished
                    // Disconnect
                    //mBTService.stop();
                    //activePeer = null;
                    //relay();

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    Packet packetRecieved = Packet.deSerialize(readMessage);
                    if (packetRecieved != null) {
                        Log.d(TAG, "Recieved packet for " + packetRecieved.getRecipient());
                        if (packetRecieved.getRecipient().equals("phipp")) {
                            // This message is for us! Display it!
                            Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_LONG).show();
                        }
                        pstore.add(packetRecieved);
                    } else {
                        Log.e(TAG, "RECIEVED A NULL PACKET!!!!");
                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save information about the connected device
                    Bundle msgData = msg.getData();
                    mConnectedDeviceAddress = msgData.getString(DEVICE_ADDRESS);
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    break;
                /*case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;*/
            }
        }
    };
}
