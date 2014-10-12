package incognitects.incognitooth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class AddKey extends Activity {

    private Button addButton;
    private EditText nickText;
    private EditText publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_key);
        final SharedPreferences keyStore = getSharedPreferences("NICK_KEYSTORE", 0);
        final SharedPreferences.Editor keyStoreEdit = keyStore.edit();
        nickText = (EditText) findViewById(R.id.editTextNick);
        publicKey = (EditText) findViewById(R.id.editTextPublicKey);
        addButton = (Button) findViewById(R.id.buttonAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                String nickname = nickText.getText().toString();
                String PublicKeyStr = publicKey.getText().toString();
                keyStoreEdit.putString(nickname, PublicKeyStr);
                keyStoreEdit.commit();
                finish();
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_key, menu);
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
