package incognitects.incognitooth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class inboxActivity extends Activity {

    private ArrayAdapter<String> messageAdapter;
    private ListView inboxList;
    private PacketStore pstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        SharedPreferences keyStore = getSharedPreferences("KEYSTORE", 0);
        EncryptUtil encryptUtil = new EncryptUtil(keyStore);

        pstore = new PacketStore(getSharedPreferences("PACKETS", 0), encryptUtil);

        messageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        inboxList = (ListView) findViewById(R.id.inboxList);
        inboxList.setAdapter(messageAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        pstore.load();

        Log.d("Inbox", "Packet count: "+pstore.packets.size());

        messageAdapter.clear();
        for (Packet p : pstore.packets) {
            messageAdapter.add(p.getPayload());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inbox, menu);
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
}
