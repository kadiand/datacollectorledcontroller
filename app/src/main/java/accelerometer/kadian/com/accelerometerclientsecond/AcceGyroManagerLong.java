package accelerometer.kadian.com.accelerometerclientsecond;

/**
 * Created by owboateng on 18-1-2016.
 */
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class AcceGyroManagerLong implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private long lastUpdate_ace = 0;
    private long lastUpdate_gyro= 0;
    private long curTime_ace = 0;
    private long curTime_gyro = 0;
    String ace_report = "";
    String gyro_report = "";
    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;

    private boolean startRecord;
    Context context;
    private String activity = null;

    private boolean stop_bg_threads = false;
    private String username;
    private String partner_name;
    private String server_ip;

    JSONObject active = null;
    JSONObject passive = null;
    JSONObject resting = null;
    JSONObject neutral = null;

    String exp_type;
    DatagramSocket s = null;
    private int target_port;
    InetAddress local = null;

    public AcceGyroManagerLong(Context context, String exp_type_value, String username, String connect_to){
        this.context = context;
        this.exp_type = exp_type_value;
        this.username = username.toLowerCase();
        this.partner_name = connect_to.toLowerCase();

        this.server_ip = "131.155.175.79";
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        // get the accelerometer sensor
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager
                .getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        try {
            active = new JSONObject();
            passive = new JSONObject();
            resting = new JSONObject();
            neutral = new JSONObject();
            active.put("Red", 255);
            active.put("Green", 0);
            active.put("Blue", 0);
            active.put("Intensity", 100);
            active.put("Modus", 0);

            passive.put("Red", 0);
            passive.put("Green", 255);
            passive.put("Blue", 0);
            passive.put("Intensity", 100);
            passive.put("Modus", 0);

            resting.put("Red", 0);
            resting.put("Green", 0);
            resting.put("Blue", 255);
            resting.put("Intensity", 100);
            resting.put("Modus", 0);

            neutral.put("Red", 255);
            neutral.put("Green", 255);
            neutral.put("Blue", 255);
            neutral.put("Intensity", 100);
            neutral.put("Modus", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            local = InetAddress.getByName("192.168.43.255");//my broadcast ip
            target_port = 6100; //port that I’m using
            s = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        startRecord = false;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    private void setRunGBThreads(boolean bol){
        this.stop_bg_threads = bol;
    }

    public void socket_connect(){
        try {
            new Thread(new SocketThread()).start();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void socket_close(){
        try {
            if (socket != null) {
                if (out != null) {
                    out.println("exit");
                    out.flush();
                }
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startSensors(){
        curTime_ace = 0;
        curTime_gyro = 0;
        lastUpdate_ace = 0;
        lastUpdate_gyro = 0;
        ace_report = "";
        gyro_report = "";
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyroscope,
                SensorManager.SENSOR_DELAY_FASTEST);

        startRecord = true;
        socket_connect();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub

        if(startRecord == true){
            Sensor mySensor = event.sensor;
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                curTime_ace = System.currentTimeMillis();

                if ((curTime_ace - lastUpdate_ace) >= 20) {
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];
                    lastUpdate_ace = curTime_ace;
                    ace_report =  "acce " + x + " " + y + " " + z + " end";
                    if (out != null){
                        out.println(ace_report);
                        out.flush();
                    }
                }
            }

            else if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
                // TODO Auto-generated method stub
                // Many sensors return 3 values, one for each axis.

                curTime_gyro = System.currentTimeMillis();

                if ((curTime_gyro - lastUpdate_gyro) >= 20) {
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];
                    lastUpdate_gyro = curTime_gyro;

                    gyro_report = "gyro " + x + " " + y + " " + z + " end";
                    if (out != null){
                        out.println(gyro_report);
                        out.flush();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    public void stopSensors(){
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mGyroscope);
        startRecord = false;
        stop_bg_threads = true;
        socket_close();
    }

    class SocketThread implements Runnable {

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(server_ip);
                socket = new Socket(serverAddr, 9999); //Update
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                if (out != null){
                    out.println("led#username#" + username + "#partner#" + partner_name);
                    out.flush();
                }
                if (in == null) {
                    Log.d("check", "In is null");
                }
                else{
                    Log.d("check", "In is not null");
                }
                String classes = in.readLine();
                while (startRecord && classes != null && classes != ""){
                    new Thread(new LedController(classes)).start();
                    classes = in.readLine();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    class LedController implements Runnable {

        private String classes;
        public LedController(String classes){
            this.classes = classes;
        }

        @Override
        public void run() {
            if (exp_type.equalsIgnoreCase("Lights")) {
                this.changeLedProperty(classes);
            }
            else{
                this.changeLightState(neutral);
            }
        }

        public void changeLightState(JSONObject color_state) {
            try{
                byte[] msg = color_state.toString().getBytes();
                DatagramPacket p = new DatagramPacket(msg, msg.length, local, target_port);
                s.send(p);
                Log.d("check","message send");
            }catch(Exception e){
                Log.d("check","error  " + e.toString());
            }
        }

        public void changeLedProperty(String server_resp) {
            if (exp_type.equalsIgnoreCase("Lights")) {
                String[] acts = server_resp.split(" ");
                int len = acts.length;
                for (int i = 0; i < len; i++) {
                    String val = acts[i];
                    val = val.replaceAll("\\s", "");
                    if (val.equals("6.0")) {
                        changeLightState(resting);
                        Log.d("light_check", "Laying");
                    } else if (val.equals("5.0")) {
                        changeLightState(passive);
                        Log.d("light_check", "Standing");
                    } else if (val.equals("4.0")) {
                        changeLightState(passive);
                        Log.d("light_check", "Sitting");
                    } else if (val.equals("3.0")) {
                        changeLightState(active);
                        Log.d("light_check", "Walking downstairs");
                    } else if (val.equals("2.0")) {
                        changeLightState(active);
                        Log.d("light_check", "Walking upstairs");
                    } else if (val.equals("1.0")) {
                        changeLightState(active);
                        Log.d("light_check", "Walking");
                    }
                    try {
                        // Sleep for 2.5 seconds
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
