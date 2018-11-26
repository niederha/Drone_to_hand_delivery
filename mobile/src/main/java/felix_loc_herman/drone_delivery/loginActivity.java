package felix_loc_herman.drone_delivery;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class loginActivity extends AppCompatActivity {

    //region Variable declaraions

    private final String TAG = this.getClass().getSimpleName();

    private static final int REGISTER_PROFILE = 1;

    private Profile userProfile = null;
    private String userID;

    //endregion

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    //region Button XML Callbacks

    public void registerButtonClicked(View view){
        Intent intentEditProfile = new Intent(loginActivity.this, editProfileActivity.class);
        if (userProfile != null) {
            //TODO: finish when fragments are implemented
            //intentEditProfile.putExtra()
        }
        startActivityForResult(intentEditProfile, REGISTER_PROFILE);
    }

    public void loginButtonClicked(View view) {
        //TODO: access firebase and do some magic
    }

    //endregion
}
