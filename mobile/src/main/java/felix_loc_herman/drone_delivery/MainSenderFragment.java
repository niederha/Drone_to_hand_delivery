package felix_loc_herman.drone_delivery;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainSenderFragment extends Fragment {

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
    private static final String DB_GPS = "GPS";
    private static final String DB_NORTH = "north";
    private static final String DB_EAST = "east";
    private static final String DB_GPSTIME = "gpstime";
    private static final String DB_SENDERNAME = "sendername";
    //endregion

    private FirebaseRecordingListener firebaseRecordingListener;
    private DatabaseReference databaseReference;

    private final String TAG = this.getClass().getSimpleName();

    private OnFragmentInteractionListener mListener;
    private View fragmentView;


    private ListView listView;
    private ReceiverAdapter receiverAdapter;
    private double sender_long=0;
    private double sender_lat=0;

    public MainSenderFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        sender_long=MainActivity.user_longitude;
        sender_lat=MainActivity.user_latitude;
        fragmentView = inflater.inflate(R.layout.fragment_main_sender, container, false);

        // Setting the adapter
        listView = fragmentView.findViewById(R.id.onlinePeerList);
        receiverAdapter = new ReceiverAdapter(getActivity(), R.layout.row_main_sender_layout);
        listView.setAdapter(receiverAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Get the status of the receiver, connect if available
                TextView textView = view.findViewById(R.id.statusTextChangeMe);
                String status = textView.getText().toString();

                if ( status.equals(getString(R.string.rec_busy)) ){
                    Toast.makeText(getContext(),R.string.user_busy, Toast.LENGTH_SHORT).show();
                } else {
                    String receiverName = ((TextView) view.findViewById(R.id.username)).getText().toString();
                    if (receiverName.equals(MainActivity.userProfile.username)) // Do not connect to yourself
                        Toast.makeText(getContext(), R.string.that_is_you, Toast.LENGTH_SHORT).show();
                    else
                        alertReceiverAndStartSending(receiverName);
                }
            }
        });


        return fragmentView;
    }

   /* private void inititializeGPSreception()
    {

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
        //MainActivity.receiver = new Receiver(MainActivity.userProfile.username, MainActivity.userProfile.photoPath);

      /*  LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
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
                Log.i("MainSenderFragment","GPS location received");
                Log.i("MainSenderFragment", "lat="+location.getLatitude()+" long="+location.getLongitude());
                sender_long=location.getLongitude();
                sender_lat=location.getLatitude();
            }
        };
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);    //limit update to once every 5 seconds
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);  //TODO : send updates of GPS position (but then need to solve the bug of re-downloading pictures)
*
    }*/

    private void alertReceiverAndStartSending(final String receivername) {

        setLED(MainActivity.LED_COLOR.YELLOW);

        final Receiver receiver = new Receiver();

        final FirebaseDatabase firebaseDatabaseUp = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReferenceUp = firebaseDatabaseUp.getReference(DB_RECEIVER);
        databaseReferenceUp.child(receivername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                receiver.username = dataSnapshot.child(DB_USERNAME).getValue(String.class);
                receiver.photoPath = dataSnapshot.child(DB_PHOTOPATH).getValue(String.class);
                receiver.senderName = MainActivity.userProfile.username;
                receiver.timestamp = dataSnapshot.child(DB_TIMESTAMP).getValue(Integer.class);
                receiver.gps.north = dataSnapshot.child(DB_GPS).child(DB_NORTH).getValue(Double.class);
                receiver.gps.east = dataSnapshot.child(DB_GPS).child(DB_EAST).getValue(Double.class);
                receiver.gps.time_last_update = dataSnapshot.child(DB_GPS).child(DB_GPSTIME).getValue(Integer.class);

                databaseReferenceUp.child(receivername).runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        mutableData.child(DB_USERNAME).setValue(receiver.username);
                        mutableData.child(DB_PHOTOPATH).setValue(receiver.photoPath);
                        mutableData.child(DB_SENDERNAME).setValue(receiver.senderName);
                        mutableData.child(DB_TIMESTAMP).setValue(receiver.timestamp);
                        mutableData.child(DB_GPS).child(DB_NORTH).setValue(receiver.gps.north);
                        mutableData.child(DB_GPS).child(DB_EAST).setValue(receiver.gps.east);
                        mutableData.child(DB_GPS).child(DB_GPSTIME).setValue(receiver.gps.time_last_update);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        setLED(MainActivity.LED_COLOR.GREEN);
                        Intent intent = new Intent(MainSenderFragment.this.getContext(), CreateFormActivity.class);
                        intent.putExtra("username",MainActivity.userProfile.username);
                        intent.putExtra("receiver_username",receivername);
                        intent.putExtra("distance",DroneHandler.distance(sender_lat,sender_long,receiver.gps.north,receiver.gps.east));
                        intent.putExtra("ETA",DroneHandler.computeETAmin(sender_lat,sender_long,receiver.gps.north,receiver.gps.east));
                        intent.putExtra("sender_latitude",sender_lat);
                        intent.putExtra("sender_longitude",sender_long);
                        startActivity(intent);
                        //TODO: launch new activity for the sender. receiver is taken care of
                        //receivername
                        //receiver.username is the sender's username
                        //MainActivity.userProfile.username could also be used for this
                        //Toast.makeText(getContext(), "Connected to " + receivername, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to contact " + receivername, Toast.LENGTH_SHORT).show();
                setLED(MainActivity.LED_COLOR.RED);
            }
        });
    }

    void setLED(MainActivity.LED_COLOR color) {
        ImageView statusLED = fragmentView.findViewById(R.id.senderOnlineStatusIndicator);
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

    //region Fragment and listView stuff
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

    @Override
    public void onResume() {
        super.onResume();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseRecordingListener = new FirebaseRecordingListener();
        databaseReference.child(DB_RECEIVER).addValueEventListener(firebaseRecordingListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        setLED(MainActivity.LED_COLOR.YELLOW);
        databaseReference.child(DB_RECEIVER).removeEventListener(firebaseRecordingListener);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private class ReceiverAdapter extends ArrayAdapter<Receiver> {
        private int row_layout;

        ReceiverAdapter(FragmentActivity activity, int row_layout) {
            super(activity, row_layout);
            this.row_layout = row_layout;
        }

        private double calculateETA(Receiver.GPS gps){
            return DroneHandler.computeETAmin(sender_lat,sender_long,gps.north,gps.east);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            final View row = LayoutInflater.from(getContext()).inflate(row_layout, parent, false);

            String username = getItem(position).username;
            ((TextView) row.findViewById(R.id.username)).setText(username);

            if (username.equals(MainActivity.userProfile.username)){
                (row.findViewById(R.id.meTextView)).setVisibility(View.VISIBLE);
                (row.findViewById(R.id.etaText)).setVisibility(View.INVISIBLE);
                (row.findViewById(R.id.etaValue)).setVisibility(View.INVISIBLE);
                (row.findViewById(R.id.statusText)).setVisibility(View.INVISIBLE);
                (row.findViewById(R.id.statusTextChangeMe)).setVisibility(View.INVISIBLE);
            } else {
                String sendername = getItem(position).senderName;
                if (sendername.equals(MainActivity.receiver.SENDERDUMMYNAME)) {
                    ((TextView) row.findViewById(R.id.statusTextChangeMe)).setText(R.string.rec_ready);
                } else {
                    ((TextView) row.findViewById(R.id.statusTextChangeMe)).setText(R.string.rec_busy);
                }

                String eta = calculateETA(getItem(position).gps)+" minutes";
                ((TextView) row.findViewById(R.id.etaValue)).setText(eta);
            }


            //region Set profile picture
            String photoPath = getItem(position).photoPath;

            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(photoPath);
            storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    if (isAdded()) {
                        final Bitmap downloadedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ((ImageView) row.findViewById(R.id.userImage)).setImageBitmap(downloadedImage);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),"Profile picture download failed!", Toast.LENGTH_SHORT).show();
                    setLED(MainActivity.LED_COLOR.RED);
                }
            });
            //endregion
            return row;
        }
    }

    private class FirebaseRecordingListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            setLED(MainActivity.LED_COLOR.YELLOW);
            receiverAdapter.clear();
            for (final DataSnapshot rec : dataSnapshot.getChildren()) {
                final Receiver receiver = new Receiver();
                receiver.username = rec.child(DB_USERNAME).getValue().toString();
                receiver.photoPath = rec.child(DB_PHOTOPATH).getValue().toString();
                receiver.timestamp = rec.child(DB_TIMESTAMP).getValue(Integer.class);
                receiver.senderName = rec.child(DB_SENDERNAME).getValue().toString();
                receiver.gps.north = rec.child(DB_GPS).child(DB_NORTH).getValue(Double.class);
                receiver.gps.east = rec.child(DB_GPS).child(DB_EAST).getValue(Double.class);
                receiver.gps.time_last_update = rec.child(DB_GPS).child(DB_GPSTIME).getValue(Integer.class);

                receiverAdapter.add(receiver);
            }
            setLED(MainActivity.LED_COLOR.GREEN);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            setLED(MainActivity.LED_COLOR.RED);
        }
    }
    //endregion
}
