package in.prakhar.hd;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static in.prakhar.hd.NotificationActivity.CHANNEL_ID;

public class VoiceService extends Service implements RecognitionListener {

    private static final String LOG_TAG = VoiceService.class.getSimpleName();

    private static final String KEYPHRASE = "jarvis";

    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private Voice voice;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "onCreate");
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Started onStartCommand");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Recognition Service")
                .setContentText("Say 'Jarvis' to start recognition")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notification);

        runRecognizerSetup();

        return Service.START_NOT_STICKY;
    }

    private void runRecognizerSetup() {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(VoiceService.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Log.i(LOG_TAG, "Failed to init recognizer ");
                } else {
                    switchSearch(KEYPHRASE);
                }
            }
        }.execute();
    }

    private void setupRecognizer(File assetsDir) throws IOException {

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setKeywordThreshold(1e-45f) // Threshold to tune for keyphrase to balance between false alarms and misses
                .getRecognizer();
        recognizer.addListener(this);

        recognizer.addKeyphraseSearch(KEYPHRASE, KEYPHRASE);
    }

    // Important library method
    private void switchSearch(String searchName) {
        Log.i(LOG_TAG, "switchSearch searchName = " + searchName);
        recognizer.stop();

        if (searchName.equals(KEYPHRASE))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().contains(KEYPHRASE))
            switchSearch(KEYPHRASE);
        Log.i(LOG_TAG, "onEndOfSpeech");
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        Log.i(LOG_TAG, "onPartialResult text=" +text);
        if (text.contains(KEYPHRASE)) {
            switchSearch(KEYPHRASE);
        }
    }

    // To start DialogActivity on getting results
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            if(text.contains(KEYPHRASE)){
                Toast.makeText(this, "I'm listening...", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, DialogActivity.class);
                intent.putExtra("Voice", voice);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            Log.i(LOG_TAG, "onResult text = " +text);
        }
    }

    @Override
    public void onError(Exception e) {
        Log.i(LOG_TAG, "onError " + e.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KEYPHRASE);
        Log.i(LOG_TAG, "onTimeout");
    }
}
