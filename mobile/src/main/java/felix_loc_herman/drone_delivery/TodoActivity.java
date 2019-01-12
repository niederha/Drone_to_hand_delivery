package felix_loc_herman.drone_delivery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class TodoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        Intent received_intent=getIntent();
        Bundle b=received_intent.getExtras();

        TextView tv=(TextView) findViewById(R.id.todoTextView);
        if(received_intent.hasCategory("message"))
        {
            String str=b.getString("message");
            if(str==null)
                tv.setText("null string");
            tv.setText(b.getString("message"));
        }
        else
        {
            tv.setText("Todo activity (no specific message passed as an extra)");
        }

    }
}
