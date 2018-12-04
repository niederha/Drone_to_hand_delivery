package felix_loc_herman.drone_delivery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EditProfileActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private static final String TYPED_USERNAME = "TYPED_USERNAME";
    private static final String TYPED_PASSWORD = "TYPED_PASSWORD";
    private static final int PICK_IMAGE = 1;

    //region Strings for database lookup - should be moved
    private static final String DB_USERNAME = "username";
    private static final String DB_PASSWORD = "password";
    private static final String DB_PHOTOPATH = "photopath";
    private static final String DB_IMAGEURI = "ImageUri";
    private static final String DB_PHOTOS = "photos";
    private static final String DB_PROFILES = "profiles";
    //endregion

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference profileGetRef = database.getReference(DB_PROFILES);
    private static DatabaseReference profileRef = profileGetRef.push();

    private Profile userProfile;
    private String userID;
    private String username;
    private String password;
    private Uri savedImageUri;
    private File imageFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //region Retrieve data sent from LoginActivity
        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.USER_PROFILE)) {
            userID = intent.getExtras().getString(MainActivity.USER_PROFILE);
            fetchDataFromDatabaseAndSetItemsToView();
        } else {
            if (intent.hasExtra(TYPED_USERNAME)) {
                username = intent.getExtras().getString(TYPED_USERNAME);
                //region Display username
                TextView usernameTextView = findViewById(R.id.editUsername);
                usernameTextView.setText(username);
                //endregion
            }
            if (intent.hasExtra(TYPED_PASSWORD)) {
                password = intent.getExtras().getString(TYPED_PASSWORD);
                //region "Display" password
                TextView passwordTextView = findViewById(R.id.editPassword);
                passwordTextView.setText(username);
                //endregion
            }
        }

        //endregion

        //region SavedInstance for resuming activity
        if (savedInstanceState != null) {
            savedImageUri = savedInstanceState.getParcelable("ImageUri");
            if (savedImageUri != null) {
                try {
                    InputStream imageStream = getContentResolver().openInputStream(savedImageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ImageView imageView = findViewById(R.id.userImage);
                    imageView.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        //endregion

        Log.v(TAG, "Finished onCreate for " + TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                clearUserForm();
                break;
            case R.id.action_validate:
                if (validateForm()) {
                    addProfileToFirebase();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(DB_IMAGEURI, savedImageUri);
    }

    //region User-profile functions

    //region Choose and save profile picture
    public void chooseProfilePicture(View view) { // Called through onClick
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "@string/selectImage"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            // Create temp file and try to copy image from intent data
            imageFile = new File(getExternalFilesDir(null), "profileImage"); // TODO: Does last argument matter?
            try {
                copyImageFromUriToFile(imageUri, imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Display image
            final InputStream imageStream;
            try {
                savedImageUri = Uri.fromFile(imageFile);
                imageStream = getContentResolver().openInputStream(savedImageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ImageView imageView = findViewById(R.id.userImage);
                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyImageFromUriToFile(Uri uriInput, File fileOutput) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = getContentResolver().openInputStream(uriInput);
            out = new FileOutputStream(fileOutput);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
            out.close();
        }
    }

    //endregion

    private boolean validateForm() {
        TextView usernameTextView = findViewById(R.id.editUsername);
        TextView passwordTextView = findViewById(R.id.editPassword);
        TextView passwordRepeatTextView = findViewById(R.id.editPasswordRepeat);

        if (usernameTextView.getText().toString().isEmpty()
                | passwordTextView.getText().toString().isEmpty()
                | (imageFile == null)) {
            Toast.makeText(this, R.string.form_incomplete, Toast.LENGTH_SHORT).show();
            return false;
        } else if (passwordTextView.getText().toString().equals(passwordRepeatTextView.getText().toString())) {
            userProfile = new Profile(
                    usernameTextView.getText().toString(),
                    passwordTextView.getText().toString(),
                    imageFile.getPath());
            return true;
        } else {
            Toast.makeText(this, R.string.password_mismatch, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void clearUserForm() {
        ImageView imageView = findViewById(R.id.userImage);
        TextView usernameTextView = findViewById(R.id.editUsername);
        TextView passwordTextView = findViewById(R.id.editPassword);
        TextView passwordRepeatTextView = findViewById(R.id.editPasswordRepeat);

        imageView.setImageDrawable(null);
        usernameTextView.setText("");
        passwordTextView.setText("");
        passwordRepeatTextView.setText("");
    }

    //endregion

    //region Firebase functions

    private void fetchDataFromDatabaseAndSetItemsToView() {
        //region Create views
        final TextView usernameTextView = findViewById(R.id.editUsername);
        final TextView passwordTextView = findViewById(R.id.editPassword);
        final ImageView imageVew = findViewById(R.id.userImage);
        //endregion

        profileGetRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String db_username = dataSnapshot.child(DB_USERNAME).getValue(String.class);
                String db_password = dataSnapshot.child(DB_PASSWORD).getValue(String.class);
                String db_photopath = dataSnapshot.child(DB_PHOTOPATH).getValue(String.class);

                //region Download and set the image
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(db_photopath);
                storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        final Bitmap downloadedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageVew.setImageBitmap(downloadedImage);
                    }
                });
                //endregion

                //region Put downloaded text data into the views
                usernameTextView.setText(db_username);
                passwordTextView.setText(db_password);
                //endregion
            }

            //region Junk
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
            //endregion
        });

        profileRef = profileGetRef.child(userID);
    }

    //region Upload functions
    private void addProfileToFirebase() {
        // Loading profile picture and compressing it
        BitmapDrawable bitmapDrawable = (BitmapDrawable) ((ImageView) findViewById(R.id.userImage)).getDrawable();
        if (bitmapDrawable == null) {
            Toast.makeText(EditProfileActivity.this, R.string.missing_picture, Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference = storageReference.child(DB_PHOTOS).child(profileRef.getKey() + ".jpg");

        UploadTask uploadTask = photoReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads
                Toast.makeText(EditProfileActivity.this, R.string.photo_upload_failed, Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new PhotoUploadSuccessListener());
    }

    private class PhotoUploadSuccessListener implements OnSuccessListener<UploadTask.TaskSnapshot> {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    userProfile.photoPath = uri.toString();
                    profileRef.runTransaction(new ProfileDataUploader());
                }
            });
        }
    }

    private class ProfileDataUploader implements Transaction.Handler {
        @NonNull
        @Override
        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
            mutableData.child(DB_USERNAME).setValue(userProfile.username);
            mutableData.child(DB_PASSWORD).setValue(userProfile.password);
            mutableData.child(DB_PHOTOPATH).setValue(userProfile.photoPath);
            return Transaction.success(mutableData);
        }

        @Override
        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
            if (b) {
                Toast.makeText(EditProfileActivity.this, R.string.registration_success, Toast.LENGTH_SHORT).show();

                // Finish account creation
                Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                intent.putExtra(MainActivity.USER_PROFILE, userProfile);
                setResult(AppCompatActivity.RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, R.string.registration_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //endregion
    //endregion
}
