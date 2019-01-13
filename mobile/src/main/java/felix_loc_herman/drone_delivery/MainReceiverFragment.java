package felix_loc_herman.drone_delivery;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class MainReceiverFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String USER_ID = "USER_ID";

    //region Strings for database lookup - should be moved
    private static final String DB_USERNAME = "username";
    private static final String DB_PASSWORD = "password";
    private static final String DB_PHOTOPATH = "photopath";
    private static final String DB_IMAGEURI = "ImageUri";
    private static final String DB_PHOTOS = "photos";
    private static final String DB_PROFILES = "profiles";
    private static final String DB_RECEIVER = "receiver";
    private static final String DB_TIMESTAMP = "timestamp";
    private static final String DB_GPS = "gps";
    private static final String DB_NORTH = "north";
    private static final String DB_EAST = "east";
    private static final String DB_GPSTIME = "gpstime";
    private static final String DB_SENDERNAME = "sendername";
    //endregion

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference peerGetRef = database.getReference(DB_RECEIVER);
    DatabaseReference peerRef;

    private final String TAG = this.getClass().getSimpleName();

    private OnFragmentInteractionListener mListener;
    //private Profile userProfile;
    private View fragmentView;
    private boolean isInitialized = false;
    private boolean isOnline = false;
    public Switch connectedSwitch = null;

    public MainReceiverFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.fragment_main_receiver,
                container, false);

        Intent intent = getActivity().getIntent();
        if (intent.hasExtra(MainActivity.USERNAME)){
            MainActivity.userProfile = new Profile(intent.getExtras().getString(MainActivity.USERNAME));
            getUserProfileFromDB();

        } else {
            MainActivity.userProfile = (Profile) intent.getSerializableExtra(USER_PROFILE);

            isInitialized = true;
        }

        connectedSwitch = fragmentView.findViewById(R.id.connectSwitch);
        connectedSwitch.setOnCheckedChangeListener(this);

        return fragmentView;
    }

    void setLED(MainActivity.LED_COLOR color) {
        ImageView statusLED = fragmentView.findViewById(R.id.onlineStatusIndicator);
        Drawable d;
        switch (color) {
            case OFF:
                d = getResources().getDrawable(android.R.drawable.presence_invisible);
                break;
            case YELLOW:
                d = getResources().getDrawable(android.R.drawable.presence_away);
                break;
            case GREEN:
                d = getResources().getDrawable(android.R.drawable.presence_online);
                break;
            case RED:
                d = getResources().getDrawable(android.R.drawable.presence_busy);
                break;
            default:
                return;
        }
        statusLED.setImageDrawable(d);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isInitialized){
            if (isChecked) {
                initPeerToPeerList();
            } else {
                disconnectPeer();
                setLED(MainActivity.LED_COLOR.OFF);
            }
        } else {
            Toast.makeText(getContext(),"Not ready", Toast.LENGTH_SHORT).show();
            connectedSwitch.setChecked(false);
        }
    }

    private void getUserProfileFromDB() {
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference(DB_PROFILES);
        databaseReference.child(MainActivity.userProfile.username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String db_username = dataSnapshot.child(DB_USERNAME).getValue(String.class);
                String db_password = dataSnapshot.child(DB_PASSWORD).getValue(String.class);
                String db_photopath = dataSnapshot.child(DB_PHOTOPATH).getValue(String.class);

                MainActivity.userProfile = new Profile(db_username, db_password, db_photopath);

                isInitialized = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setLED(MainActivity.LED_COLOR.RED);
            }
        });
    }

    private void initPeerToPeerList(){
        MainActivity.receiver = new Receiver(MainActivity.userProfile.username,
                                             MainActivity.userProfile.photoPath);
        //TODO: get GPS and update it
        //MainActivity.receiver.gps = new Receiver.GPS()
        uploadPeerToPeerList(MainActivity.receiver);

    }

    private void uploadPeerToPeerList(final Receiver receiver){

        // Set online LED to busy
        setLED(MainActivity.LED_COLOR.YELLOW);
        peerRef = peerGetRef.child(MainActivity.receiver.username);
        peerRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                mutableData.child(DB_USERNAME).setValue(receiver.username);
                mutableData.child(DB_PHOTOPATH).setValue(receiver.photoPath);
                mutableData.child(DB_TIMESTAMP).setValue(receiver.timestamp);
                mutableData.child(DB_GPS).child(DB_NORTH).setValue(receiver.gps.north);
                mutableData.child(DB_GPS).child(DB_EAST).setValue(receiver.gps.east);
                mutableData.child(DB_GPS).child(DB_GPSTIME).setValue(receiver.gps.time_last_update);
                mutableData.child(DB_SENDERNAME).setValue(receiver.senderName);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                setLED(MainActivity.LED_COLOR.GREEN);
                startListener();
            }
        });
    }

    private void startListener(){
        peerRef.child(DB_SENDERNAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isOnline) {
                    isOnline = true;
                } else {
                    String sendername = dataSnapshot.getValue(String.class);
                    if (sendername != null && !sendername.equals(MainActivity.receiver.SENDERDUMMYNAME)) {
                        //TODO: launch activity for the receiver here. everything else taken care of.
                        //sendername is the name of the sendername
                        //MainActivity.receiver  is the name of the receiver
                        //MainActivity.userProfile.username could also be used
                        Toast.makeText(getContext(), sendername + " connected!", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setLED(MainActivity.LED_COLOR.RED);
            }
        });
    }

    public void disconnectPeer(){
        setLED(MainActivity.LED_COLOR.YELLOW);
        peerRef.removeValue();
    }

    //region Fragment stuff
    @Override
    public void onResume() {
        super.onResume();

        if (isOnline) {
            uploadPeerToPeerList(MainActivity.receiver);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        disconnectPeer();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement " +
                    "OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
//endregion
}