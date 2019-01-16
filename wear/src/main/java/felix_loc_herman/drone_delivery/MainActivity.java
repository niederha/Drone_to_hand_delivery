package felix_loc_herman.drone_delivery;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends WearableActivity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private final String TAG = this.getClass().getSimpleName();

    private static final String WEAR_MESSAGE_PATH = "/message";

    private GoogleApiClient googleApiClient;

    private ConstraintLayout constraintLayout;

    private final static int INTERVAL = 1000 * 60; // 1 min
    Handler timeHandler;

    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        constraintLayout = findViewById(R.id.container);

        setAmbientEnabled();

        initGoogleApiClient();

        timeHandler = new Handler();
        timeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                timeHandler.postDelayed(this, INTERVAL);
                printTime();
            }
        }, 1);
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)) {
                    if (!running) {
                          ((TextView) findViewById(R.id.textETA)).setVisibility(View.VISIBLE);
                          ((TextView) findViewById(R.id.textPutETAhere)).setVisibility(View.VISIBLE);
                          running = true;
                    }
                    String etaValue = new String(messageEvent.getData());
                    if (!etaValue.equals("STOP")) {
                        TextView etaTextView = findViewById(R.id.textPutETAhere);
                        etaTextView.setText(etaValue);
                    } else {
                        ((TextView) findViewById(R.id.textETA)).setVisibility(View.INVISIBLE);
                        ((TextView) findViewById(R.id.textPutETAhere)).setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    private void initGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi( Wearable.API )
                .addConnectionCallbacks( this )
                .build();

        if (googleApiClient != null && !(googleApiClient.isConnected() || googleApiClient.isConnecting() ))
            googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.MessageApi.addListener(googleApiClient, this);
    }

    private void printTime ( ){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        ((TextView) findViewById(R.id.textPutTimeHere)).setText(sdf.format(c.getTime()));
    }

    //region Required stuff


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient != null && !( googleApiClient.isConnected() || googleApiClient.isConnecting() ))
            googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null){
            Wearable.MessageApi.removeListener( googleApiClient, this );
            if ( googleApiClient.isConnected() ) {
                googleApiClient.disconnect();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (googleApiClient != null)
            googleApiClient.unregisterConnectionCallbacks(this);
        ((TextView) findViewById(R.id.textETA)).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.textPutETAhere)).setVisibility(View.INVISIBLE);
        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        updateDisplay();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            constraintLayout.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
            ((TextView) findViewById(R.id.textTime)).setTextColor(Color.WHITE);
            ((TextView) findViewById(R.id.textPutTimeHere)).setTextColor(Color.WHITE);
            ((TextView) findViewById(R.id.textETA)).setTextColor(Color.WHITE);
            ((TextView) findViewById(R.id.textPutETAhere)).setTextColor(Color.WHITE);
        } else {
            constraintLayout.setBackgroundColor(getResources().getColor(android.R.color.white, getTheme()));
            ((TextView) findViewById(R.id.textTime)).setTextColor(Color.BLACK);
            ((TextView) findViewById(R.id.textPutTimeHere)).setTextColor(Color.BLACK);
            ((TextView) findViewById(R.id.textETA)).setTextColor(Color.BLACK);
            ((TextView) findViewById(R.id.textPutETAhere)).setTextColor(Color.BLACK);
        }
    }
    //endregion
}
