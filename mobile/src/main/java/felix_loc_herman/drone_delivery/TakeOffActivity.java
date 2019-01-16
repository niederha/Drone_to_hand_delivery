//TODO :proper cancelling management
//TODO : fixe refreshing of screen when ETA or distance changes

package felix_loc_herman.drone_delivery;

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

public class TakeOffActivity extends AppCompatActivity {

    private String sender_username;
    private String receiver_username;
    private DatabaseReference deliveryRef;
    private ValueEventListener valueEventListenerDelivery;
    private static final int DRONE_FLYING_TO_RECEIVER = 5;
    private double distance;
    private double ETA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle b=getIntent().getExtras();
        sender_username=b.getString("username");
        receiver_username=b.getString("receiver_username");
        distance=b.getDouble("distance");
        ETA=b.getDouble("ETA");


        //TODO : if the drone isn't set up yet, lauch the set up activity for return
       /* while(!droneHandler.isConnectedToDrone())   //the drone is not connected yet
        {

            Intent intent = new Intent(this,ConnectActivity.class);
            intent.putExtra("droneHandler",droneHandler);
            startActivity(intent);
            //TODO : planify return?
        }*/
        setContentView(R.layout.activity_take_off);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        TextView tv_dist=(TextView) findViewById(R.id.activityTakeOff_distance);
        tv_dist.setText(distance+" m");


        TextView tv_ETA=(TextView) findViewById(R.id.activityTakeOff_ETA);
        tv_ETA.setText(ETA+" minutes");

        //setting up the firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference deliveryGetRef = database.getReference("deliveries");
        deliveryRef = deliveryGetRef.child(sender_username);

        valueEventListenerDelivery = deliveryRef.addValueEventListener(new DeliveryUpdateEventListener(this));
    }

    public void activityTakeOff_CancelButton_Clicked(View view) {
        deliveryRef.child("cancelled").setValue(true);  //inform the receiver that we cancelled the delivery  //TODO : check if it threadsafe and correct
        Toast.makeText(this,"The delivery request has been cancelled successfully",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void activityTakeOff_TakeOffSWitch_switched(View view) {
        deliveryRef.child("status").setValue(DRONE_FLYING_TO_RECEIVER);  //inform the receiver that we cancelled the delivery  //TODO : check if it threadsafe and correct

        Intent intent = new Intent(this,MapActivity.class);
        intent.putExtra("username",sender_username);
        intent.putExtra("receiver_username",receiver_username);
        startActivity(intent);
    }

    private class DeliveryUpdateEventListener implements ValueEventListener {         //listener to listen on status change
        private Context context;

        DeliveryUpdateEventListener(Context context) {
            this.context=context;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            distance=dataSnapshot.child("distance").getValue(Double.class).doubleValue();     //retrieve distance
            ETA=dataSnapshot.child("ETA").getValue(Double.class).doubleValue();     //retrieve ETA
            boolean cancelled_by_receiver=dataSnapshot.child("cancelled").getValue(Boolean.class).booleanValue();     //retrieve cancelling status
            if(cancelled_by_receiver) {    //the receiver cancelled the delivery
                Toast.makeText(context,"Sending cancelled:\nthe receiver don't want to receive the package anyore",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context,MainActivity.class);
                startActivity(intent);
            }

            //update distance and ETA
            setContentView(R.layout.activity_take_off);
            TextView tv_dist=(TextView) findViewById(R.id.activityTakeOff_distance);
            tv_dist.setText(String.valueOf(distance));

            TextView tv_ETA=(TextView) findViewById(R.id.activityTakeOff_ETA);
            tv_ETA.setText(String.valueOf(ETA));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    }

    public void onDestroy()
    {
        Log.i("TakeOffActivity","OnDestroy : stopping listeners");
        if(deliveryRef!=null && valueEventListenerDelivery!=null)
            deliveryRef.removeEventListener(valueEventListenerDelivery);
        super.onDestroy();
    }
}
