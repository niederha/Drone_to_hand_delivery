package felix_loc_herman.drone_delivery;

import java.io.Serializable;

public class Receiver implements Serializable {

    String username;
    String photoPath;
    Integer timestamp; // TODO: Decide unit
    String senderName;
    GPS gps;

    public static final String SENDERDUMMYNAME = "A_SENDER_HAS_NO_NAME";

    Receiver(){
        this.senderName = SENDERDUMMYNAME;
        this.gps = new GPS(this.timestamp);
    }

    Receiver(String username, String photoPath) {
        this.username    = username;
        this.photoPath   = photoPath;
        this.timestamp   = (int) System.currentTimeMillis();
        this.senderName  = SENDERDUMMYNAME;
        this.gps = new GPS(this.timestamp);
    }


    public static class GPS {
        Double north;
        Double east;
        Integer time_last_update;

        GPS(Integer initTime){
            north = 0.0;
            east  = 0.0;
            time_last_update = initTime;
        }

        GPS(double north, double east) {
            this.north = north;
            this.east = east;
            this.time_last_update = (int) System.currentTimeMillis();
        }
    }

}
