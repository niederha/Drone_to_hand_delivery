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

    private Profile userProfile = null;
    private String userID;

    public static Receiver receiver;

    private SectionsStatePagerAdapter sectionsStatePagerAdapter;
    private MainReceiverFragment mainReceiverFragment;
    private MainSenderFragment mainSenderFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        //region Get user info from intent
//        Intent intent = getIntent();
//        if (intent.hasExtra(USER_PROFILE)) {
//            userProfile = (Profile) intent.getExtras().get(USER_PROFILE);
//        }
//        if (intent.hasExtra(USER_ID)) {
//            userID = intent.getExtras().getString(USER_ID);
//        }
//        //endregion

        //region Fragment initializations
        setContentView(R.layout.activity_main);

        sectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());

        mainReceiverFragment = new MainReceiverFragment();
        mainSenderFragment = new MainSenderFragment();

        ViewPager viewPager = findViewById(R.id.mainViewPager);
        setUpViewPager(viewPager);

        // set default tab
        viewPager.setCurrentItem(sectionsStatePagerAdapter.getPositionByTitle(getString(R.string.tab_title_receiver)));

        //setupOnPageChangeListener(viewPager);
        //endregion

    }

    private void setUpViewPager(ViewPager viewPager) {
        sectionsStatePagerAdapter.addFragment(mainReceiverFragment, getString(R.string.tab_title_receiver));
        sectionsStatePagerAdapter.addFragment(mainSenderFragment, getString(R.string.tab_title_sender));
        viewPager.setAdapter(sectionsStatePagerAdapter);
    }
//
//    private void setupOnPageChangeListener(ViewPager viewPager) {
//        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//
//            // This method will be invoked when a new page becomes selected.
//            @Override
//            public void onPageSelected(int position) {
//                switch (position){
//                    case 0:
//                        break;
//                    case 1:
//                        MainReceiverFragment.disconnectPeer();
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
//    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

}

