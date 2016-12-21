package com.bazaar.mizaaz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.bazaar.mizaaz.message.NetworkChangeMessage;

import org.greenrobot.eventbus.EventBus;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private EventBus bus = EventBus.getDefault();
    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getExtras()!=null) {
            NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
                NetworkChangeMessage networkChangeMessage = new NetworkChangeMessage();

                networkChangeMessage.connected  = true;

                bus.post(networkChangeMessage);
            } else if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {

                NetworkChangeMessage networkChangeMessage = new NetworkChangeMessage();

                networkChangeMessage.connected  = false;

                networkChangeMessage.message = context.getString(R.string.error_no_network);

                bus.post(networkChangeMessage);
            }
        }


    }
}
