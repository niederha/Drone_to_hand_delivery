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
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.StrictMath.abs;

public class DroneHandler implements ARDeviceControllerListener {

    public static final int IDLE=0;
    public static final int FLYING=1;
    public static final int WAITING_TO_LAND=2;
    public static final int LANDING=3;
    public static final int LANDED=4;
    public static final int NOT_CONNECTED=5;
    private static double droneSpeed = 0.5;
    private DatabaseReference deliveryRef;
    private final int critBatteryLevel = 5;
    private final float distTolerance = 2;
    private final float angleTolerance = 10;
    private ARDeviceController mDroneController;
    private ARDiscoveryDeviceService mService;
    static private int state = NOT_CONNECTED;
    private double latitude = 500;
    private double longitude = 500;
    private double altitude;
    private double goalLatitude;
    private double goalLongitude;
    private float yaw;
    private float pitch;
    private float roll;
    private int forwardAngle = 20;
    private boolean gotoPt = false;

    public DroneHandler(ARDiscoveryDeviceService service, String sender_username) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference deliveryGetRef = database.getReference("deliveries");
        deliveryRef = deliveryGetRef.child(sender_username);
        state = NOT_CONNECTED;
        updateFirebaseDroneStatus(state);
        mService = service;
        ARDiscoveryDevice device = createDiscoveryDevice();
        mDroneController = createController(device);
        if (mDroneController != null) {
            mDroneController.addListener(this);
            ARCONTROLLER_ERROR_ENUM error = mDroneController.start();
            mDroneController.getFeatureCommon().sendWifiSettingsOutdoorSetting((byte)1);
            state = IDLE;
            updateFirebaseDroneStatus(state);
        }
        else{
            Log.e(TAG, "NO CONTROLLER");
        }
    }

    private void updateFirebaseETA(double ETA)
    {
        deliveryRef.child("ETA").setValue(ETA);
    }
    private void updateFirebaseDistance(double distance)
    {
        deliveryRef.child("distance").setValue(distance);
    }
    private void updateFirebaseDroneStatus(int droneStatus)
    {
        deliveryRef.child("droneStatus").setValue(droneStatus);
    }
    private void updateFirebaseDroneGPS(double latitude, double longitude)
    {
        deliveryRef.child("drone_GPS").child("north").setValue(latitude);
        deliveryRef.child("drone_GPS").child("east").setValue(longitude);
    }


    //region PublicFunctions

    public void takeOff()
    {
        /*if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED.equals(getPilotingState()))
        {
            ARCONTROLLER_ERROR_ENUM error = mDroneController.getFeatureARDrone3().sendPilotingTakeOff();

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK))
            {
                ARSALPrint.e(TAG, "Error while sending take off: " + error);
            }
            else{
                state = FLYING;
                updateFirebaseDroneStatus(state);
            }
        }*/

    }

    public void land()
    {
        // Reset drone Attitude
        /*gotoPt = false;
        mDroneController.getFeatureARDrone3().setPilotingPCMDPitch((byte) 0);
        mDroneController.getFeatureARDrone3().setPilotingPCMDYaw((byte) 0);
        mDroneController.getFeatureARDrone3().setPilotingPCMDFlag((byte)0);

        // Perform Landing
        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = getPilotingState();
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState) ||
                ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState))
        {
            ARCONTROLLER_ERROR_ENUM error = mDroneController.getFeatureARDrone3().sendPilotingLanding();
            state = LANDED;
            updateFirebaseDroneStatus(state);
            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK))
            {
                ARSALPrint.e(TAG, "Error while sending take off: " + error);
            }
            else{
                state = FLYING;
                updateFirebaseDroneStatus(state);
            }

        }*/
    }


    public void goTo(double goalLatitude, double  goalLongitude)
    {
        gotoPt=false; //true;
        this.goalLatitude=goalLatitude;
        this.goalLongitude=goalLongitude;
    }

    @Override
    public void onExtensionStateChanged (ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error)
    {
        return;
    }

    @Override
    public void onCommandReceived (ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {

        // On GPS position change
        if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED && (elementDictionary != null)) {
            ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
            if (args != null) {
                double newLatitude = (double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LATITUDE);
                double newLongitude = (double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LONGITUDE);
                double newAltitude = (double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_ALTITUDE);
                if (abs(newLatitude) < 90 && abs(newLongitude) < 90) {
                    latitude = newLatitude;
                    longitude = newLongitude;
                    altitude = newAltitude;
                    updateFirebaseDroneGPS(latitude,longitude);

                }
                if (state == FLYING && gotoPt) {
                    updateFirebaseDistance(getDistance());

                    if (getDistance() < distTolerance) {
                        mDroneController.getFeatureARDrone3().setPilotingPCMDPitch((byte) 0);
                        mDroneController.getFeatureARDrone3().setPilotingPCMDYaw((byte) 0);
                        mDroneController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 0);
                        state = WAITING_TO_LAND;
                        updateFirebaseDroneStatus(state);
                    } else {
                        mDroneController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 1);
                        if (abs(toDegrees(angleToPoint()) - toDegrees(yaw)) < angleTolerance) {
                            mDroneController.getFeatureARDrone3().setPilotingPCMDPitch((byte) ((int)(round(forwardAngle/180.0*100.0))));
                            mDroneController.getFeatureARDrone3().setPilotingPCMDYaw((byte) 0);
                            mDroneController.getFeatureARDrone3().setPilotingPCMDGaz((byte) 0);
                        } else {
                            int yawToReach =  (int) (round(toDegrees(angleToPoint()))/180.0*100.0);
                            mDroneController.getFeatureARDrone3().setPilotingPCMDPitch((byte) 0);
                            mDroneController.getFeatureARDrone3().setPilotingPCMDYaw((byte) 50);
                        }
                    }
                }
            }
        }

        // On battery level change
        if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED) && (elementDictionary != null)){
            ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
            if (args != null) {
                byte percent = (byte)((Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT)).intValue();
                int batLevel = percent;
                if (batLevel<critBatteryLevel)
                {
                    land();
                }
            }
        }

        // On attitude change
        if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED) && (elementDictionary != null)){
            ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
            if (args != null) {
                roll = (float)((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_ROLL)).doubleValue();
                pitch = (float)((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_PITCH)).doubleValue();
                yaw = (float)((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_YAW)).doubleValue();
                if (abs(toDegrees(angleToPoint())-toDegrees(yaw))<angleTolerance && state == FLYING && gotoPt){
                    mDroneController.getFeatureARDrone3().setPilotingPCMDYaw((byte) 0);
                }
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
        return computeETAmin(latitude, longitude, goalLatitude, goalLongitude);
    }

    // Returns the ETA between two points
    static public double computeETAmin(double pointALat, double pointALong, double pointBLat, double pointBLong){
        double dist = distance(pointALat, pointBLong, pointBLat, pointBLong);
        double ETA = dist/droneSpeed/60;
        return ETA;
    }


    //return current distance
    public double getDistance() {
        return distance(latitude,longitude,goalLatitude,goalLongitude);
    }

    //return distance between 2 points
    static public double distance(double pointALat, double pointALong,double pointBLat, double pointBLong){
        if (abs(pointALat)>90 || abs(pointALong)>90 || abs(pointBLat)>90 || abs(pointBLong)>90){
            return 0;
        }
        float earthRadiusKm = 6371.0f;
        float kmToM = 1000;
        double dLat = abs(pointALat-pointBLat);
        double dLon = abs(pointALong-pointBLong);

        double a = sin(dLat/2) * sin(dLat/2) +
                sin(dLon/2) * sin(dLon/2) * cos(pointALat) * cos(pointBLat);
        double c = 2 * atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = c *earthRadiusKm;
        return (float) distance*kmToM;
    }


    //endregion


    private float angleToPoint(){
        double dLat = abs(goalLatitude-latitude);
        double dLon = abs(goalLongitude-longitude);
        float angle = (float) atan2(dLon, dLat);
        return angle;
    }

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
