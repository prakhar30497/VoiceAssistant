package in.prakhar.hd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
    private static final String LOG_TAG = Receiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "onReceive");
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Log.i(LOG_TAG, "onReceive onBoot");
            context.startService(new Intent(context, VoiceService.class));
        }
    }
}
