// Good resource: https://www.binpress.com/android-wear-message-api/

package felix_loc_herman.drone_delivery;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class WatchHandler implements GoogleApiClient.ConnectionCallbacks {

    private static final String START_ACTIVITY = "/start_activity";
    public static final String MESSAGE_PATH = "/message";

    private static GoogleApiClient googleApiClient;

    public static void init(Context context){
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();

        googleApiClient.connect();
    }

    public static void sendMessage(final String path, final String message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            googleApiClient, node.getId(), path, message.getBytes()
                    ).await();
                }
            }
        }).start();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        sendMessage (START_ACTIVITY, "" );
    }

    public static void disconnect(){
        googleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
}
