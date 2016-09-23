package de.unistuttgart.robotic.android;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.projectoxford.speechrecognition.*;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ISpeechRecognitionServerEvents {

    private Button btn_startRecord;
    private Button btn_getMoney;
    private Button btn_getBeer;
    private Button btn_init;
    private Button btn_punsh;

    private TextView tv_message;

    private boolean pressed = false;
    private boolean recording = false;

    private MicrophoneRecognitionClient microphoneRecognitionClient = null;
    private MobileServiceClient mobileServiceClient;

    private String TAG_Partial = "PartialResponse";
    private String TAG_Intent = "IntentRepsonse";
    private String TAG_Error = "Error";
    private String TAG_Record = "Recording";
    private String TAG_FinalResponse = "FinalResponse";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        btn_startRecord = (Button) findViewById(R.id.btn_record);

        btn_startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pressed) {
                    pressed = true;
                    btn_startRecord.setBackgroundColor(Color.RED);
                    btn_startRecord.setText(R.string.btn_stop);
                    createDataClientWithIntent();
                } else {
                    pressed = false;
                    btn_startRecord.setBackgroundColor(Color.BLUE);
                    btn_startRecord.setText(R.string.btn_start);
                    microphoneRecognitionClient.endMicAndRecognition();
                    recording = false;
                    try {
                        microphoneRecognitionClient.finalize();
                    } catch (Throwable throwable) {
                        Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    microphoneRecognitionClient = null;
                }
            }
        });
        setSupportActionBar(toolbar);

        btn_getMoney = (Button) findViewById(R.id.btn_getmoney);
        btn_getMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendCommand("get_money");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btn_getBeer = (Button) findViewById(R.id.btn_getbeer);
        btn_getBeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendCommand("get_beer");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btn_init = (Button) findViewById(R.id.btn_init);
        btn_init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendCommand("init");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btn_punsh = (Button) findViewById(R.id.btn_punsh);
        btn_punsh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendCommand("get_punsh");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /*
        try {
            mobileServiceClient = new MobileServiceClient("https://makeathon.azurewebsites.net", this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createDataClientWithIntent() {
        recording = true;
        microphoneRecognitionClient = SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(MainActivity.this, "en-GB", this ,getString(R.string.primaryKey), getString(R.string.secondaryKey), getString(R.string.luisAppID), getString(R.string.luisSubscriptionID));
        microphoneRecognitionClient.startMicAndRecognition();

    }

    @Override
    public void onPartialResponseReceived(String s) {
        Log.d(TAG_Partial, s);
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult response) {
        for (int i = 0; i < response.Results.length; i++) {
            RecognizedPhrase phrase = response.Results[i];
            Log.d(TAG_FinalResponse, phrase.DisplayText);
        }
    }

    @Override
    public void onIntentReceived(String s) {
        String intentName = "";
        double score = 0;
        Log.d(TAG_Intent, s);
        try {
            JSONObject json = new JSONObject(s);
            Log.d(TAG_Intent, json.toString());
            JSONArray intentArray = json.getJSONArray("intents");

            for (int i=0;i<intentArray.length();i++) {
                JSONObject temp = intentArray.getJSONObject(i);
                String tempIntentName= temp.getString("intent");
                double tempScore = temp.getDouble("score");
                if(tempScore > score) {
                    intentName = tempIntentName;
                    score = tempScore;
                }
            }

            sendCommand(intentName);
            Log.d(TAG_Intent, intentName);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        /*
        IntentModel intentModel = new IntentModel(intentName, score);
        mobileServiceClient.getTable(IntentModel.class).insert(intentModel);
        */
    }

    @Override
    public void onError(int i, String s) {
        Log.d(TAG_Error, s);
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAudioEvent(boolean b) {
        if(recording) {
            Log.d(TAG_Record, " true");
        } else {
            Log.d(TAG_Record, " false");
        }
    }

    /**
     * for RaspberryPi to choose the right operation for robot
     * @param keyword: operation keyword,
     * @throws Exception
     */
    private void sendCommand(String keyword) throws Exception {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.12.96.141/?action="+ keyword;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "Response: " + response, Toast.LENGTH_SHORT).show();;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.getMessage());
            }
        });
        queue.add(request);
    }
}
