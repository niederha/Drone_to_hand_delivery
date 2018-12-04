package felix_loc_herman.drone_delivery;

import java.io.Serializable;

public class Delivery implements Serializable {

    String sender;
    String receiver;
    boolean isCancelled;
    boolean cancelAcked;
    DeliveryStates deliveryState;
    boolean landingAllowed;
    Integer ETA;
    Double distanceToRec;

    String itemDescription;
    Float quantity;
    String messageToRec;
    String messageToSndr;

    Drone_GPS drone_gps;

    Delivery(String sender, String receiver, Integer ETA, double distanceToRec) {
        this.sender = sender;
        this.receiver = receiver;
        this.ETA = ETA;
        this.distanceToRec = distanceToRec;
    }

    //region Super-constructor
    Delivery(String sender,
             String receiver,
             boolean isCancelled,
             boolean cancelAcked,
             DeliveryStates deliveryState,
             boolean landingAllowed,
             Integer ETA,
             double distanceToRec,
             String itemDescription,
             Float quantity,
             String messageToRec,
             String messageToSndr,
             Drone_GPS drone_gps
             ) {
        this.sender = sender;
        this.receiver = receiver;
        this.isCancelled = isCancelled;
        this.deliveryState = deliveryState;
        this.landingAllowed = landingAllowed;
        this.ETA = ETA;
        this.distanceToRec = distanceToRec;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.messageToRec = messageToRec;
        this.messageToSndr = messageToSndr;
        this.drone_gps = drone_gps;
    }

    //endregion

    //region Subclass and enum

    private class Drone_GPS {
        Double north;
        Double east;
        Integer time_last_updated;

        Drone_GPS(){
            north = null;
            east = null;
            time_last_updated = null;
        }
    }

    public enum DeliveryStates {
        IDLE,
        REQUEST_SENT_TO_DATABASE,
        REQUEST_RECEIVED_BY_RECEIVER,
        REQUEST_ACCEPTED_BY_RECEIVER,
        DRONE_CONNECTED_AND_ACCEPTED_BY_RECEIVER, // TODO: Bad name, the drone is accepted or the order?
        DRONE_FLYING_TO_RECEIVER,
        DRONE_ARRIVED_NEAR_RECEIVER,              // ie. receiver controls it
        DRONE_IS_LANDING_AT_RECEIVER,
        DRONE_LANDED_AT_RECEIVER,
        DRONE_FLYING_BACK_TO_SENDER,
        DRONE_READY_TO_LANG_AT_SENDER
    }

    //endregion
}
