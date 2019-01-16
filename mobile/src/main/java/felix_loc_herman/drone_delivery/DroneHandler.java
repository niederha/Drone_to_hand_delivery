package felix_loc_herman.drone_delivery;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARFeatureARDrone3;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.arsal.ARSALPrint;

import android.content.Context;
import android.util.Log;


import static android.content.ContentValues.TAG;

public class DroneHandler implements ARDeviceControllerListener {
    /*public enum droneState{
            IDLE, GOING_TOR_ECIVER, WATINTG_TO_LAND, LANDED_AT_RECIEVER, GOING_BACK
        }*/
    public static final int IDLE=0;
    public static final int FLYING=1;
    public static final int WAITING_TO_LAND=2;
    public static final int LANDING=3;
    public static final int LANDED=4;
    public static final int NOT_CONNECTED=5;
    private final DatabaseReference deliveryRef;

    private ARDeviceController mDroneController;
    private ARDiscoveryDeviceService mService;
    private ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_STATE_ENUM autoFlightState;
    static private int state = NOT_CONNECTED;

    public DroneHandler(ARDiscoveryDeviceService service, String sender_username) {
        state = NOT_CONNECTED;
        mService = service;
        ARDiscoveryDevice device = createDiscoveryDevice();
        mDroneController = createController(device);
        if (mDroneController != null) {
            mDroneController.addListener(this);
            ARCONTROLLER_ERROR_ENUM error = mDroneController.start();
            mDroneController.getFeatureCommon().sendWifiSettingsOutdoorSetting((byte)1);
            state = IDLE;
        }
        else{
            Log.e(TAG, "NO CONTROLLER");
        }

        //connect to firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference deliveryGetRef = database.getReference("deliveries");
        deliveryRef = deliveryGetRef.child(sender_username);
    }

    private void updateFirebase_ETA(double ETA)
    {
        deliveryRef.child("ETA").setValue(ETA);
    }
    private void updateFirebase_distance(double distance)
    {
        deliveryRef.child("distance").setValue(distance);
    }
    private void updateFirebase_DroneStatus(double droneStatus)
    {
        deliveryRef.child("droneStatus").setValue(droneStatus);
    }
    private void updateFirebase_DroneGPS(double latitude, double longitude)
    {
        deliveryRef.child("drone_GPS").child("north").setValue(latitude);
        deliveryRef.child("drone_GPS").child("east").setValue(longitude);
    }


    //region PublicFunctions

    public boolean isConnectedToADrone(){
        if (mDroneController != null){
            return true;
        }
        else{
            return false;
        }
    }

    public void takeOff()
    {
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED.equals(getPilotingState()))
        {
            ARCONTROLLER_ERROR_ENUM error = mDroneController.getFeatureARDrone3().sendPilotingTakeOff();

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK))
            {
                ARSALPrint.e(TAG, "Error while sending take off: " + error);
            }
            else{
                state = FLYING;
            }
        }

    }

    public void land()
    {
        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = getPilotingState();
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState) ||
                ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState))
        {
            ARCONTROLLER_ERROR_ENUM error = mDroneController.getFeatureARDrone3().sendPilotingLanding();

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK))
            {
                ARSALPrint.e(TAG, "Error while sending take off: " + error);
            }
            else{
                state = FLYING;
            }

        }
    }

    static public int getDroneState(){
        if (state == NOT_CONNECTED){
            return NOT_CONNECTED;
        }
        else{
            return state;
        }
    }


    public void goTo(double latitude, double  longitude)
    {

    }

    @Override
    public void onExtensionStateChanged (ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error)
    {
        return;
    }

    @Override
    public void onCommandReceived (ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {
        if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED) && (elementDictionary != null)){
            ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
            if (args != null) {
                ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED_ERROR_ENUM error = ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED_ERROR_ENUM.getFromValue((Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED_ERROR));
                Log.e(TAG, error.toString());
            }
        }

        if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED && (elementDictionary != null)){
            ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
            if (args != null) {
                double latitude = (double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LATITUDE);
                double longitude = (double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LONGITUDE);
                double altitude = (double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_ALTITUDE);
            }
        }

        if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED) && (elementDictionary != null)){
            ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
            if (args != null) {
                byte percent = (byte)((Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT)).intValue();
            }
        }
    }

    @Override
    // called when the state of the device controller has changed
    public void onStateChanged (ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error)
    {
        switch (newState)
        {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                break;
            case ARCONTROLLER_DEVICE_STATE_STARTING:
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPING:
                break;
            default:
                break;
        }
    }

    // Return the current ETA
    public double getETAmin(){
        return 1;
    }

    // Returns the ETA between two points
    static public double computeETAmin(double point_A_lat, double point_A_long, double point_B_lat, double point_B_long){
        return 1;
    }


    //return current distance
    public double getDistance() {return  1;}

    //return distance between 2 points
    static public double distance(double point_A_lat, double point_A_long,double point_B_lat, double point_B_long){
        return 1;
    }


    //endregion

    private ARDeviceController createController(ARDiscoveryDevice device)
    {
        ARDeviceController deviceController = null;
        try {
            deviceController = new ARDeviceController(device);
        } catch (ARControllerException e) {
            e.printStackTrace();
        }
        return deviceController;
    }

    private ARDiscoveryDevice createDiscoveryDevice()
    {
        ARDiscoveryDevice device = null;
        if ((mService != null))
        {
            try
            {
                device = new ARDiscoveryDevice();

                ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) mService.getDevice();

                device.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
            }
            catch (ARDiscoveryException e)
            {
                e.printStackTrace();
                Log.e(TAG, "Error: " + e.getError());
            }
        }
        else{
            Log.e(TAG,"No Device created");
        }
        return device;
    }

    private ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM getPilotingState()
    {
        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.eARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_UNKNOWN_ENUM_VALUE;
        if (mDroneController != null)
        {
            try
            {
                ARControllerDictionary dict = mDroneController.getCommandElements(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED);
                if (dict != null)
                {
                    ARControllerArgumentDictionary<Object> args = dict.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                    if (args != null)
                    {
                        Integer flyingStateInt = (Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE);
                        flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(flyingStateInt);
                    }
                }
            }
            catch (ARControllerException e)
            {
                e.printStackTrace();
            }
        }
        return flyingState;
    }

}
