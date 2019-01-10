package felix_loc_herman.drone_delivery;

import java.io.Serializable;

public class Peer implements Serializable {

    String username;
    String photoPath;
    Integer timestamp; // TODO: Decide unit
    boolean isReceiver;
    GPS gps;

    Peer(String username, String photoPath) {
        this.username    = username;
        this.photoPath   = photoPath;
        this.isReceiver  = true;
        this.timestamp   = (int) System.currentTimeMillis();
        this.gps = new GPS(this.timestamp);
    }


    public class GPS {
        Double north;
        Double east;
        Integer time_last_update;

        GPS(Integer initTime){
            north = 0.0;
            east  = 0.0;
            time_last_update = initTime;
        }
    }

}
