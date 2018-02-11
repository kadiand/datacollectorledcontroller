package accelerometer.kadian.com.accelerometerclientsecond;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements OnClickListener, AdapterView.OnItemSelectedListener {
    LinearLayout layout;
    Button start_button;
    Button stop_button;
    TextView status;

    Spinner sp_exp_type;
    EditText et_username, et_connect_to;
//    EditText et_server_ip1, et_server_ip2, et_server_ip3, et_server_ip4;
    AcceGyroManagerLong accgyro = null;

    private String username = "";
    private String connect_to = "";

    private String exp_type_value = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        layout = (LinearLayout) findViewById(R.id.linear);
        start_button = (Button) findViewById(R.id.start_button);
        stop_button = (Button) findViewById(R.id.stop_button);
        status = (TextView) findViewById(R.id.status);
        start_button.setOnClickListener(this);
        stop_button.setOnClickListener(this);
        stop_button.setEnabled(false);

        sp_exp_type = (Spinner) findViewById(R.id.exp_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.exp_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_exp_type.setAdapter(adapter);

        et_username = (EditText) findViewById(R.id.username);
        et_connect_to = (EditText) findViewById(R.id.connect_to);
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


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
		/*LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mMessageReceiver);*/
        super.onPause();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (accgyro != null) {
                            accgyro.stopSensors();
                        }
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();


    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == start_button.getId()){
            String error_log = "";

            exp_type_value = String.valueOf(sp_exp_type.getSelectedItem());

            username = et_username.getText().toString().replaceAll("\\s","");
            connect_to = et_connect_to.getText().toString().replaceAll("\\s","");

            if (username.length() < 3 || username.contains(" ")){
                error_log = error_log.concat("Username should be at least 3 characters with no spaces\n");
            }
            else if (connect_to.length() < 3 || connect_to.contains(" ")){
                error_log = error_log.concat("Partner name should be at least 3 characters with no spaces\n");
            }

            if (error_log.compareTo("") != 0){
                status.setText(error_log);

            }
            else {
                status.setText("");

                start_button.setEnabled(false);
                et_username.setEnabled(false);
                et_connect_to.setEnabled(false);

                stop_button.setEnabled(true);
                accgyro = new AcceGyroManagerLong(this, exp_type_value, username, connect_to);
                accgyro.startSensors();
            }
        }
        else if (v.getId() == stop_button.getId()){
            start_button.setEnabled(true);
            et_username.setEnabled(true);
            et_connect_to.setEnabled(true);

            stop_button.setEnabled(false);
            accgyro.stopSensors();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}