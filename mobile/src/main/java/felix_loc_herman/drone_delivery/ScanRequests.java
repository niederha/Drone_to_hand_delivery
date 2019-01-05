package felix_loc_herman.drone_delivery;

import java.io.Serializable;

public class ScanRequests implements Serializable {

    String sender_username;
    Integer timestamp; // TODO: Decide unit
    boolean active;

    ScanRequests(String sender_username) {
        this.sender_username = sender_username;
        this.active = true;
        this.timestamp = (int)System.currentTimeMillis();
    }

    private class GPS {
        Double north;
        Double east;
        String time_last_update;
    }

}
