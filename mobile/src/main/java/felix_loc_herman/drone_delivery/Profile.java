package felix_loc_herman.drone_delivery;

import java.io.Serializable;

public class Profile implements Serializable {

    // TODO: some tricks to change to firebase authorization scheme?

    String username;
    String password; // hashed
    Integer time_last_seen;
    String photoPath;
    private class GPS {
        Double north;
        Double east;
        String time_last_update;
    }

    Profile(String username, String password) {
        this(username, password, null);
    }
    Profile(String username, String password, String photoPath) {
        this.username = username;
        this.password = password;
        this.photoPath = photoPath;
    }

}
