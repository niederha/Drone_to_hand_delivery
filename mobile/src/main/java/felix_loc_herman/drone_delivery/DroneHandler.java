package felix_loc_herman.drone_delivery;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.Serializable;
import java.util.List;

import static android.content.ContentValues.TAG;

public class DroneHandler implements Serializable {

    private ARDiscoveryServicesDevicesListUpdatedReceiver receiver;

    /*public enum droneState{
        IDLE, GOING_TOR_ECIVER, WATINTG_TO_LAND, LANDED_AT_RECIEVER, GOING_BACK
    }*/
    public static final int IDLE=0;
    public static final int FLYING=1;
    public static final int WAITING_TO_LAND=2;
    public static final int LANDING=3;
    public static final int LANDED=4;


    public DroneHandler(Context context){
        ARSDK.loadSDKLibs();
    }

    //region PublicFunctions

    public int getDroneState(){
        return 0;
    }

    // Get the device list to use
    public int getDeviceList(){
        int mock = 0;
        return mock;
    }

    // Connects the object to the drone
    public boolean connectToDrone(int device){
        return true;
    }

    public void takeOff (){

    }

    public boolean isConnectedToDrone()
    {
        return true;
    }

    public void goTo( double north, double east){

    }

    public void land(){

    }

    public void getBack(){

    }

    public int getFlightStatus(){
        int flightStatus = 0;
        return flightStatus;
    }

    // Return the current ETA
    public static double getETAmin(){
        return 1;
    }

    // Returns the ETA between two points
    static public double computeETAmin(int startPosition, int endPosition){
        return 1;
    }
    //endregion
    /*
    private ARDiscoveryService mArdiscoveryService = null;
    private ServiceConnection mArdiscoveryServiceConnection = null; // Connection to the drone

    private final ARDiscoveryServicesDevicesListUpdatedReceiverDelegate mDiscoveryDelegate =
            new ARDiscoveryServicesDevicesListUpdatedReceiverDelegate() {

                @Override
                public void onServicesDevicesListUpdated() {
                    if (mArdiscoveryService != null) {
                        // List containing all drones
                        List<ARDiscoveryDeviceService> deviceList = mArdiscoveryService.getDeviceServicesArray();
                        // TODO: something with the list
                    }
                }
            };

    // Starting the drone discovery
    private void initDiscoveryService()
    {
        // create the service connection
        if (mArdiscoveryServiceConnection == null)
        {
            mArdiscoveryServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mArdiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();
                    startDiscovery();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mArdiscoveryService = null;
                }
            };

        }

        if (mArdiscoveryService == null)
        {
            // if the discovery service doesn't exists, bind to it
            Intent i = new Intent(context, ARDiscoveryService.class);
            context.bindService(i, mArdiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        }
        else
        {
            // if the discovery service already exists, start discovery
            startDiscovery();
        }
    }

    private void startDiscovery()
    {
        if (mArdiscoveryService != null)
        {
            mArdiscoveryService.start();
        }
    }

    private void registerReceivers()
    {
         receiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(mDiscoveryDelegate);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(context);
        localBroadcastMgr.registerReceiver(receiver,
                new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }


    private ARDiscoveryDevice createDiscoveryDevice(@NonNull ARDiscoveryDeviceService service) {
        ARDiscoveryDevice device = null;
        try {
            device = new ARDiscoveryDevice(context, service);
        } catch (ARDiscoveryException e) {
            Log.e(TAG, "Exception", e);
        }

        return device;
    }

    private void unregisterReceivers()
    {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(context);
        localBroadcastMgr.unregisterReceiver(receiver);
    }

    private void closeServices()
    {
        Log.d(TAG, "closeServices ...");

        if (mArdiscoveryService != null)
        {
            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    mArdiscoveryService.stop();

                    context.unbindService(mArdiscoveryServiceConnection);
                    mArdiscoveryService = null;
                }
            }).start();
        }
    }
    */



}
