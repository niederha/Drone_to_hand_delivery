package felix_loc_herman.drone_delivery;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainReceiverFragment extends Fragment {

    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String USER_ID = "USER_ID";

    //region Strings for database lookup - should be moved
    private static final String DB_USERNAME = "username";
    private static final String DB_PASSWORD = "password";
    private static final String DB_PHOTOPATH = "photopath";
    private static final String DB_IMAGEURI = "ImageUri";
    private static final String DB_PHOTOS = "photos";
    private static final String DB_PROFILES = "profiles";
    //endregion

    private final String TAG = this.getClass().getSimpleName();

    private OnFragmentInteractionListener mListener;
    private Profile userProfile;
    private View fragmentView;
    private String userID;

    public MainReceiverFragment() {}


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO:
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
        if (intent.hasExtra(MainActivity.USER_ID)){
            userID = intent.getExtras().getString(USER_ID);
            getUserProfileFromDB();

            // Set online LED to offline
            ImageView statusLED = fragmentView.findViewById(R.id.onlineStatusIndicator);
            Drawable d = getResources().getDrawable(android.R.drawable.presence_away);
            statusLED.setImageDrawable(d);
        } else {
            userProfile = (Profile) intent.getSerializableExtra(USER_PROFILE);
            TextView textView = fragmentView.findViewById(R.id.mainReceiverHeadline);
            textView.setText(userProfile.username);

            // Set online LED to online
            ImageView statusLED = fragmentView.findViewById(R.id.onlineStatusIndicator);
            Drawable d = getResources().getDrawable(android.R.drawable.presence_online);
            statusLED.setImageDrawable(d);
        }

        //while (userProfile == null); // wait for download



        //TODO: create and upload userprofile to peerlist
        //TODO: enable eventlistener and logic involved

        return fragmentView;
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

    private void getUserProfileFromDB() {
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference(DB_PROFILES);
        databaseReference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String db_username = dataSnapshot.child(DB_USERNAME).getValue(String.class);
                String db_password = dataSnapshot.child(DB_PASSWORD).getValue(String.class);
                String db_photopath = dataSnapshot.child(DB_PHOTOPATH).getValue(String.class);

                userProfile = new Profile(db_username, db_password, db_photopath);

                TextView textView = fragmentView.findViewById(R.id.mainReceiverHeadline);
                textView.setText(userProfile.username);

                // Set online LED to online
                ImageView statusLED = fragmentView.findViewById(R.id.onlineStatusIndicator);
                Drawable d = getResources().getDrawable(android.R.drawable.presence_online);
                statusLED.setImageDrawable(d);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Set online LED to offline
                ImageView statusLED = fragmentView.findViewById(R.id.onlineStatusIndicator);
                Drawable d = getResources().getDrawable(android.R.drawable.presence_busy);
                statusLED.setImageDrawable(d);
            }
        });
    }
}
