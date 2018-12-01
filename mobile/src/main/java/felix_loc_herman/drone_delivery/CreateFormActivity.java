/**
TODO : retrieve sender name (from intent extra or from local storage)   (for now, hard coded in OnCreate)
TODO : retrieve receiver name (probably from intend extra)   (for now, hard coded in OnCreate)
TODO : improve aesthetic (layout, ...)
 TODO : finish implementation of sending form to database
 TODO : launch new activity when clicking on "send" button
 TODO : stop the background process that checks for receiving
 TODO : send user location as "drone_GPS" to database?
 TODO: set properly ETA
 TODO : set properly distance
**/
package felix_loc_herman.drone_delivery;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class CreateFormActivity extends AppCompatActivity {

    private static final int REQUEST_SENT_TO_DATABASE = 1;      //status code for the delivery status (firebase)

    private String sender_username;
    private String receiver_username;
    private String itemName;    //name of the item to be sent
    private Float itemQuantity; //number of items to be sent (NaN if not specified)
    private String message;     //mesage sent by the sender to the receiver

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO : retrieve user and sender name form local storage or intent
        sender_username="fake_sender_username";
        receiver_username="fake_receiver_username";

        setContentView(R.layout.activity_create_form);  //display the layout
    }

    public void formCancelButtonClicked(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void formSendButtonClicked(View view) {
        TextView itemNameText = findViewById(R.id.itemNameText);    //get TextView
        itemName= itemNameText.getText().toString();         //extract name of item from TextView

        try {
            TextView quantityText = findViewById(R.id.quantityText);    //get TextView
            itemQuantity= Float.valueOf(quantityText.getText().toString());         //extract name of item from TextView
        } catch (NumberFormatException e) {
            itemQuantity=Float.NaN;           //using NaN (not a number) if the quantaity is invalid
        }


        TextView messageText = findViewById(R.id.messageText);    //get TextView
        message= messageText.getText().toString();         //extract name of item from TextView

        Log.e("myApp","ready to connect to firebase");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference deliveryGetRef = database.getReference("deliveries");
        DatabaseReference deliveryRef = database.getReference(sender_username);
        //DatabaseReference profileRef = profileGetRef.push();

        Log.e("myApp","ready to write to firebase");


        deliveryRef.runTransaction(new UploadFormContentHandler());

        Log.e("myApp","finished to write to firebase");
        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)),
                PICK_IMAGE);*/
    }

    private class UploadFormContentHandler implements Transaction.Handler { //This class does the actual upload and handles the launching of the next activity once finished
        @NonNull
        @Override
        public Transaction.Result doTransaction(@NonNull MutableData
                                                        mutableData) {
            Log.e("myApp","preparing data");
            //mutableData.child("sender").setValue(sender_username);    //not needed : it is already the key
            mutableData.child("reciever").setValue(receiver_username);
            mutableData.child("cancelled").setValue(false);
            mutableData.child("cancel_ack").setValue(false);
            mutableData.child("status").setValue(REQUEST_SENT_TO_DATABASE);
            //mutableData.child("drone_GPS").setValue();    //TODO: add user's GPS cordinate as drone_GPS
            mutableData.child("landing_allowed").setValue(false);
            mutableData.child("ETA").setValue(0);   //TODO: set properly ETA
            mutableData.child("distance").setValue(0.0);  //TODO : set properly distance
            mutableData.child("item").setValue(itemName);
            mutableData.child("quantity").setValue(itemQuantity);
            mutableData.child("message_to_receiver").setValue(message);

            Log.e("myApp","trying write");
            return Transaction.success(mutableData);
        }

        @Override
        public void onComplete(@Nullable DatabaseError databaseError,
                               boolean b, @Nullable DataSnapshot
                                       dataSnapshot) {
            Log.e("myApp","write sucessfull");
        }
    }

}
