package felix_loc_herman.drone_delivery;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String USER_ID = "USER_ID";

    //TODO: Fragment declarations will be set here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: Create section state pager adapter

        //TODO: instantiate fragments

        //TODO: Create viewPager and set default tab


    }

    private void setUpViewPager(ViewPager mViewPager){
        //TODO: Add fragments to sectionStatePagerAdapter
    }

    //TODO: Is this override really necessary?
    /*@Override
    public void onFragmentInteraction(Uri uri){
        // No operation
    }*/ // Commented, override does not exist in inherited superclass yet

}

