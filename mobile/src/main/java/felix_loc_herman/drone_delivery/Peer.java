package felix_loc_herman.drone_delivery;

import java.io.Serializable;

public class Peer implements Serializable {

    String username;
    Integer timestamp; // TODO: Decide unit
    boolean isReceiver;

    public class GPS {
        Double north;
        Double east;
        Integer time_last_update;
    }

    Peer(String username) {
        this.username = username;
        this.isReceiver = true;
        this.timestamp = (int) System.currentTimeMillis();
    }

}
