package felix_loc_herman.drone_delivery;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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
    DatabaseReference deliveryRef;

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

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int TAG_CODE_PERMISSION_LOCATION=42;
            ActivityCompat.requestPermissions(this.getActivity(), new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    TAG_CODE_PERMISSION_LOCATION);
            // missing permissions to gps
            //Toast.makeText(getContext(),"Failed to enable GPS!", Toast.LENGTH_SHORT).show();
            //connectedSwitch.setChecked(false);

        }
        MainActivity.receiver = new Receiver(MainActivity.userProfile.username,
                                             MainActivity.userProfile.photoPath);

        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener()
        {
            //region unused function overrides
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
            //endregion

            @Override
            public void onLocationChanged(Location location) {
                Log.e("MainReceiverFragment","GPS location received");
                Log.e("MainReceiverFragment", "lat="+location.getLatitude()+" long="+location.getLongitude());
                // TODO: decide where to get the time from
                //MainActivity.receiver.gps.north = location.getLatitude();
                //MainActivity.receiver.gps.east = location.getLongitude();
                //MainActivity.receiver.gps.time_last_update = location.getTime();
                MainActivity.receiver.gps = new Receiver.GPS(location.getLatitude(), location.getLongitude());
                uploadPeerToPeerList(MainActivity.receiver);
            }
        };
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);    //limit update to once every 5 seconds
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);  //TODO : send updates of GPS position (but then need to solve the bug of re-downloading pictures)

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
                    final String sendername = dataSnapshot.getValue(String.class);
                    if (sendername != null && !sendername.equals(MainActivity.receiver.SENDERDUMMYNAME)) {
                        //TODO: launch activity for the receiver here. everything else taken care of.

                        //DatabaseReference deliveryGetRef = database.getReference("deliveries");
                        //deliveryRef = deliveryGetRef.child(sendername);
                        //deliveryRef.child("status").setValue(2);    //2 : REQUEST_RECEIVED_BY_RECEIVER

                        //read delivery information and create dialog to accept or decline delivery
                        ValueEventListener deliveryListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String item=dataSnapshot.child("item").getValue(String.class);
                                double quantity=dataSnapshot.child("quantity").getValue(Double.class).doubleValue();
                                String description=dataSnapshot.child("description").getValue(String.class);

                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainReceiverFragment.this.getContext());
                                alertDialog.setTitle("There is a delivery for you");
                                alertDialog.setMessage(sendername+" has a delivery for you:\nItem: "+item+"\nQuantity:"+quantity+"\nDescription:\n"+description);
                                alertDialog.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.i("MainReceiverFragment","delivery accepted");
                                        DatabaseReference deliveryGetRef = database.getReference("deliveries");
                                        deliveryRef = deliveryGetRef.child(sendername);
                                        deliveryRef.child("status").setValue(3);    //set status to 3=DELIVERY ACCEPTED
                                        Intent intent = new Intent(MainReceiverFragment.this.getContext(), ReceivingActivity.class);
                                        intent.putExtra("receiver_name",MainActivity.receiver);
                                        intent.putExtra("sender_name",sendername);
                                        startActivity(intent);
                                    }
                                });
                                alertDialog.setNegativeButton("DENY", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.i("MainReceiverFragment","delivery denied");
                                        DatabaseReference deliveryGetRef = database.getReference("deliveries");
                                        deliveryRef = deliveryGetRef.child(sendername);
                                        deliveryRef.child("cancelled").setValue(true);
                                    }
                                });
                                alertDialog.show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Getting Post failed, log a message
                                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                // ...
                            }
                        };
                        deliveryRef.addListenerForSingleValueEvent(deliveryListener);




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
        if (isOnline) {
            peerRef.removeValue();  //TODO : this line makes the app crash : should be replaced by something working
            isOnline = false;
        }
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