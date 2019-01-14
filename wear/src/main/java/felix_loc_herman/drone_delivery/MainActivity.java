package felix_loc_herman.drone_delivery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends WearableActivity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private final String TAG = this.getClass().getSimpleName();

    private static final String WEAR_MESSAGE_PATH = "/message";

    private GoogleApiClient googleApiClient;

    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        constraintLayout = findViewById(R.id.container);

        setAmbientEnabled();

        initGoogleApiClient();
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)){

                    TextView etaTextView = findViewById(R.id.textPutETAhere);
                    String etaValue = new String(messageEvent.getData());
                    etaTextView.setText(etaValue);
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

    // TODO: update clock time

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
        } else {
            constraintLayout.setBackgroundColor(getResources().getColor(android.R.color.white, getTheme()));
        }
    }
    //endregion
}
