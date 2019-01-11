package felix_loc_herman.drone_delivery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private static final String DB_GPS = "gps";
    private static final String DB_NORTH = "north";
    private static final String DB_EAST = "east";
    private static final String DB_GPSTIME = "gpstime";
    private static final String DB_SENDERNAME = "sendername";
    //endregion

    private FirebaseRecordingListener firebaseRecordingListener;
    private DatabaseReference databaseReference;

    private final String TAG = this.getClass().getSimpleName();

    private OnFragmentInteractionListener mListener;
    private Profile userProfile;
    private View fragmentView;
    //private String userID;

    private ListView listView;
    private ReceiverAdapter receiverAdapter;

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

        fragmentView = inflater.inflate(R.layout.fragment_main_sender, container, false);

        // Setting the adapter
        listView = fragmentView.findViewById(R.id.onlinePeerList);
        receiverAdapter = new ReceiverAdapter(getActivity(), R.layout.row_main_sender_layout);
        listView.setAdapter(receiverAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getContext(), "Sending to user: "
                        + ((TextView) view.findViewById(R.id.username)).getText().toString(),
                        Toast.LENGTH_SHORT).show();

                //TODO: Start activity GenerateFormActivity through intent
            }
        });
//
//        userID = getActivity().getIntent().getExtras().getString(USER_ID);

        return fragmentView;

    }

    private void onRefreshButtonClicked(View view) {
        //onDestroy();
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
        ImageView statusLED = fragmentView.findViewById(R.id.senderOnlineStatusIndicator);
        Drawable d = getResources().getDrawable(android.R.drawable.presence_away);
        statusLED.setImageDrawable(d);
        databaseReference.child(DB_RECEIVER).removeEventListener(firebaseRecordingListener);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private class ReceiverAdapter extends ArrayAdapter<Receiver> {
        private int row_layout;

        ReceiverAdapter(FragmentActivity activity, int row_layout) {
            super(activity, row_layout);
            this.row_layout = row_layout;
        }

        private String calculateETA(Receiver.GPS gps){
            //TODO: calculate ETA!
            return "12:34";
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            final View row = LayoutInflater.from(getContext()).inflate(row_layout, parent, false);

            ((TextView) row.findViewById(R.id.username)).setText(getItem(position).username);

            String eta = calculateETA(getItem(position).gps );
            ((TextView) row.findViewById(R.id.etaValue)).setText(eta);

            String sendername = getItem(position).senderName;
            if (sendername.equals(MainActivity.receiver.SENDERDUMMYNAME)) {
                ((TextView) row.findViewById(R.id.statusTextChangeMe)).setText(R.string.rec_ready);
            } else {
                ((TextView) row.findViewById(R.id.statusTextChangeMe)).setText(R.string.rec_busy);
            }

            //region Set profile picture
            String photoPath = getItem(position).photoPath;

            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(
                photoPath
            );
            storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    if (isAdded()) {
                        final Bitmap downloadedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ((ImageView) row.findViewById(R.id.userImage)).setImageBitmap(downloadedImage);
                        //ImageView imageView = fragmentView.findViewById(R.id.userImage);
                        //imageView.setImageBitmap(downloadedImage);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),"Profile picture download failed!", Toast.LENGTH_SHORT).show();
                    ImageView statusLED = fragmentView.findViewById(R.id.senderOnlineStatusIndicator);
                    Drawable d = getResources().getDrawable(android.R.drawable.presence_busy);
                    statusLED.setImageDrawable(d);
                }
            });
            //endregion
            return row;
        }
    }

    private class FirebaseRecordingListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ImageView statusLED = fragmentView.findViewById(R.id.senderOnlineStatusIndicator);
            Drawable d = getResources().getDrawable(android.R.drawable.presence_away);
            statusLED.setImageDrawable(d);
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
            statusLED = fragmentView.findViewById(R.id.senderOnlineStatusIndicator);
            d = getResources().getDrawable(android.R.drawable.presence_online);
            statusLED.setImageDrawable(d);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            ImageView statusLED = fragmentView.findViewById(R.id.senderOnlineStatusIndicator);
            Drawable d = getResources().getDrawable(android.R.drawable.presence_busy);
            statusLED.setImageDrawable(d);
        }
    }
}
