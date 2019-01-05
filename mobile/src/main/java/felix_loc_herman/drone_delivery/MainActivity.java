package felix_loc_herman.drone_delivery;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements
        MainReceiverFragment.OnFragmentInteractionListener,
        MainSenderFragment.OnFragmentInteractionListener {

    private final String TAG = this.getClass().getSimpleName();

    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String USER_ID = "USER_ID";

    private SectionsStatePagerAdapter sectionsStatePagerAdapter;
    private MainReceiverFragment mainReceiverFragment;
    private MainSenderFragment mainSenderFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());

        mainReceiverFragment = new MainReceiverFragment();
        mainSenderFragment = new MainSenderFragment();

        ViewPager viewPager = findViewById(R.id.mainViewPager);
        setUpViewPager(viewPager);

        // set default tab
        viewPager.setCurrentItem(sectionsStatePagerAdapter.getPositionByTitle(getString(R.string.tab_title_receiver)));
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

