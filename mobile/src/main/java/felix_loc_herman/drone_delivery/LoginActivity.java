package felix_loc_herman.drone_delivery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private static final int REGISTER_PROFILE = 1;
    private static final String TYPED_USERNAME = "TYPED_USERNAME";
    private static final String TYPED_PASSWORD = "TYPED_PASSWORD";

    //region Strings for database lookup - should be moved
    private static final String DB_USERNAME = "username";
    private static final String DB_PASSWORD = "password";
    private static final String DB_PROFILES = "profiles";
    //endregion

    private Profile userProfile = null;
    private String userID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.v(TAG, "Finished onCreate for " + TAG);
    }

//region Button XML Callbacks

    public void loginButtonClicked(View view) {
        // TODO: Smarter way than downloading the entire database for each login?
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference(DB_PROFILES);

        final TextView loginMessageTextView = findViewById(R.id.LoginMessage);
        final String usernameInput = ((EditText) findViewById(R.id.username)).getText().toString();
        final String passwordInput = ((EditText) findViewById(R.id.password)).getText().toString();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean notMember = true;
                for (final DataSnapshot user : dataSnapshot.getChildren()) {
                    String db_username = user.child(DB_USERNAME).getValue(String.class);
                    String db_password = user.child(DB_PASSWORD).getValue(String.class);
                    if (usernameInput.equals(db_username) && passwordInput.equals(db_password)) {
                        userID = user.getKey();
                        notMember = false;
                        break;
                    }
                }
                if (notMember) {
                    loginMessageTextView.setText(R.string.login_failed);
                    loginMessageTextView.setTextColor(Color.RED);
                } else {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.USER_ID, userID);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void registerButtonClicked(View view) {
        final String usernameInput = ((EditText) findViewById(R.id.username)).getText().toString();
        final String passwordInput = ((EditText) findViewById(R.id.password)).getText().toString();

        Intent intentEditProfile = new Intent(LoginActivity.this, EditProfileActivity.class);

        if (userProfile != null) {
            intentEditProfile.putExtra(MainActivity.USER_PROFILE, userProfile);
        } else {
            if (!usernameInput.isEmpty()) {
                intentEditProfile.putExtra(TYPED_USERNAME, usernameInput);
            }
            if (!passwordInput.isEmpty()) {
                intentEditProfile.putExtra(TYPED_PASSWORD, passwordInput);
            }
        }

        startActivityForResult(intentEditProfile, REGISTER_PROFILE);
    }

//endregion

    @Override // onActivityForResult is called when editProfileActivity finishes
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTER_PROFILE && resultCode == RESULT_OK && data != null) {
            userProfile = (Profile) data.getSerializableExtra(MainActivity.USER_PROFILE);
            if (userProfile != null) {
                TextView usernameTextView = findViewById(R.id.username);
                TextView passwordTextView = findViewById(R.id.password);
                usernameTextView.setText(userProfile.username);
                passwordTextView.setText(userProfile.password);
            }
        }
    }
}
