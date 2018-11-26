package felix_loc_herman.drone_delivery;

import java.io.Serializable;

public class Profile implements Serializable {

    // TODO: some tricks to change to firebase authorization scheme?
    String username;
    String password;
    String photoPath; // TODO: want profile picture for users? nice to have?
    Boolean hasDrone;

    Profile(String username, String password) {
        this.username = username;
        this.password = password;
    }

    //TODO: Datamap declaraion, is it really needed?

}
