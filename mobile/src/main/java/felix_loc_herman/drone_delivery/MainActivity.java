package felix_loc_herman.drone_delivery;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements
        MainReceiverFragment.OnFragmentInteractionListener,
        MainSenderFragment.OnFragmentInteractionListener {

    private final String TAG = this.getClass().getSimpleName();

    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String USERNAME = "USERNAME";

    public static Profile userProfile;
    public static Receiver receiver;

    private SectionsStatePagerAdapter sectionsStatePagerAdapter;
    private MainReceiverFragment mainReceiverFragment;
    private MainSenderFragment mainSenderFragment;

    public static double user_longitude;
    public static double user_latitude;

    public enum LED_COLOR {OFF, YELLOW, RED, GREEN}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b=getIntent().getExtras();
        user_longitude=b.getDouble("user_longitude");
        user_latitude=b.getDouble("user_latitude");


        receiver = new Receiver();

        //region Fragment initializations
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        sectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());

        mainReceiverFragment = new MainReceiverFragment();
        mainSenderFragment = new MainSenderFragment();

        ViewPager viewPager = findViewById(R.id.mainViewPager);
        setUpViewPager(viewPager);

        // set default tab
        viewPager.setCurrentItem(sectionsStatePagerAdapter.getPositionByTitle(getString(R.string.tab_title_receiver)));
        //endregion

    }

    private void setUpViewPager(ViewPager viewPager) {
        sectionsStatePagerAdapter.addFragment(mainReceiverFragment, getString(R.string.tab_title_receiver));
        sectionsStatePagerAdapter.addFragment(mainSenderFragment, getString(R.string.tab_title_sender));
        viewPager.setAdapter(sectionsStatePagerAdapter);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
    }

}

