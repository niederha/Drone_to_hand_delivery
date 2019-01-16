package felix_loc_herman.drone_delivery;
/**
 * TODO : add notifications (enterring status 2 and 3 and cancelled by receiver)?
 * TODO : denied : delete from database
 * TODO : denied : propose to retry? (in POPUP)
 * TODO : redirect REQUEST_ACCEPTED_BY_RECEIVER to the right activity
 * TODO : ask for confirmation to cancel?
 * TODO : public void CancelButtonClicked(View view) : check if it is correct and thread safe (I don't use transactions nor OnSuccess
 * TODO : replace toasts by notifications?
 **/

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class WaitingForAcceptationByReceiverActivity extends AppCompatActivity {

    private static final int REQUEST_SENT_TO_DATABASE = 1;
    private static final int REQUEST_RECEIVED_BY_RECEIVER = 2;
    private static final int REQUEST_ACCEPTED_BY_RECEIVER = 3;

    private String sender_username;
    private String receiver_username;
    private int status;

    private DatabaseReference deliveryRef;
    private ValueEventListener valueEventListenerDelivery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b=getIntent().getExtras();
        sender_username=b.getString("username");
        receiver_username=b.getString("receiver_username");

        status=REQUEST_SENT_TO_DATABASE;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference deliveryGetRef = database.getReference("deliveries");
        deliveryRef = deliveryGetRef.child(sender_username);

        valueEventListenerDelivery = deliveryRef.addValueEventListener(new StatusUpdateEventListener(this));

        setContentView(R.layout.activity_waiting_for_acceptation_by_receiver);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void CancelButtonClicked(View view) {
        if(status==REQUEST_SENT_TO_DATABASE) {  //the receiver hasn't received the request yet : it is enough to delete it from the database
            deliveryRef.removeValue();  //TODO : check if it threadsafe and correct
            Toast.makeText(this,"The delivery request has been cancelled successfully",Toast.LENGTH_SHORT);
        }
        else
        {
            deliveryRef.child("cancelled").setValue(true);  //inform the receiver that we cancelled the delivery  //TODO : check if it threadsafe and correct
            Toast.makeText(this,"The delivery request has been cancelled successfully",Toast.LENGTH_SHORT);
        }
        Intent intent = new Intent(this,GPSActivity.class);
        intent.putExtra(MainActivity.USERNAME, sender_username);
        startActivity(intent);
    }


    private class StatusUpdateEventListener implements ValueEventListener {         //listener to listen on status change
        private Context context;

        StatusUpdateEventListener(Context context) {
            this.context=context;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            status=dataSnapshot.child("status").getValue(Integer.class).intValue();     //retrieve status
            boolean refused_by_receiver=dataSnapshot.child("cancelled").getValue(Boolean.class).booleanValue();     //retrieve cancelling status
            if(refused_by_receiver) {    //the receiver declined the delivery
                Toast.makeText(context,"Sending cancelled:\nthe receiver don't want to receive the package",Toast.LENGTH_LONG);
                Intent intent = new Intent(context,MainActivity.class);
                startActivity(intent);
            }
            if(status==REQUEST_RECEIVED_BY_RECEIVER) {   //the request was received by the receivers phone
                ((TextView)findViewById(R.id.StatusTextView)).setText("The delivery request was received by the receivers phone : you have to wait for him to accept or deny it.");
            }
            else if(status==REQUEST_ACCEPTED_BY_RECEIVER) { //the receiver accepted the delivery
                Toast.makeText(context,"The receiver accepted the delivery",Toast.LENGTH_LONG);
                Intent intent = new Intent(context,TakeOffActivity.class);
                //TODO : add drone handler as extra
                intent.putExtra("username",sender_username);
                intent.putExtra("receiver_username",receiver_username);
                startActivity(intent);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    }

    public void onDestroy()
    {
        Log.i("WaitingForAcceptationByReceiverActivity","OnDestroy : stopping listeners");
        if(deliveryRef!=null && valueEventListenerDelivery!=null)
            deliveryRef.removeEventListener(valueEventListenerDelivery);
        super.onDestroy();
    }
}
