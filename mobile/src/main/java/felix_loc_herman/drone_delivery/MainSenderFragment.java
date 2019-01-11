package felix_loc_herman.drone_delivery;

import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

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
    //endregion

    private final String TAG = this.getClass().getSimpleName();

    private OnFragmentInteractionListener mListener;
    private Profile userProfile;
    private View fragmentView;
    private String userID;

    private ListView listView;
    private PeerAdapter peerAdapter;

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

        inflater.inflate(R.layout.fragment_main_sender, container, false);
//
//        // Setting the adapter
//        listView = fragmentView.findViewById(R.id.onlinePeerList);
//        peerAdapter = new PeerAdapter(getActivity(), R.layout.row_main_sender_layout);
//        listView.setAdapter(peerAdapter);
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getContext(), "Sending to user: "
//                        + ((TextView) view.findViewById(R.id.username)).getText().toString(),
//                        Toast.LENGTH_SHORT).show();
//                //TODO: Start activity GenerateFormActivity through intent
//            }
//        });
//
//        userID = getActivity().getIntent().getExtras().getString(USER_ID);

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

    @Override
    public void onResume() {
        super.onResume();
//        databaseRef = FirebaseDatabase.getInstance().getReference();
//        mFirebaseRecordingListener = new MyFirebaseRecordingListener();
//        databaseRef.child("profiles").child(idUser).child("recordings").addValueEventListener(mFirebaseRecordingListener);
    }

    @Override
    public void onPause() {
        super.onPause();

//        databaseRef.child("profiles").child(idUser).child("recordings").removeEventListener(mFirebaseRecordingListener);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);


    }



    private class PeerAdapter extends ArrayAdapter<Profile> {
        private int row_layout;

        PeerAdapter(FragmentActivity activity, int row_layout) {
            super(activity, row_layout);
            this.row_layout = row_layout;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Reference to the row View
            View row = convertView;

            if (row == null) {
                // Inflate it from layout
                row = LayoutInflater.from(getContext()).inflate(row_layout, parent, false);
            }

            //TODO: populate fields from DB
//            ((TextView) row.findViewById(R.id.exerciseDevice2)).setText(getItem(position).exerciseHRbelt ? "yes" : "no");
//            ((TextView) row.findViewById(R.id.username)).setText(getItem(position));

            return row;
        }
    }
}
