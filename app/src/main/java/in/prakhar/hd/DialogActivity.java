package in.prakhar.hd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DialogActivity extends Activity {

    private static final String LOG_TAG = DialogActivity.class.getSimpleName();
    private TextToSpeech tts;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;

    private ImageView mic;
    private TextView text;
    private Button cancelButton;
    private ProgressBar progressBar;

    ArrayList<Integer> alarmDays;
    int hour = 0, min = 0;
    String timeFromLatNLng;

    private Voice voice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        initializeTextToSpeech();

        setTitle("Jarvis");

        mic = (ImageView) findViewById(R.id.iv_dialog);
        text = (TextView) findViewById(R.id.tv_dialog);
        cancelButton = (Button) findViewById(R.id.b_cancel);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        text.setText(R.string.i_am_listening);
        mic.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.GONE);

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecognizer();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Nothing happens on clicking outside of the dialog
        setFinishOnTouchOutside(false);

        startRecognizer();
    }

    // *** Function to start speech recognizer
    private void startRecognizer() {
        text.setText(R.string.i_am_listening);
        mic.setVisibility(View.INVISIBLE);

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }
            @Override
            public void onBeginningOfSpeech() {

            }
            @Override
            public void onRmsChanged(float rmsdB) {
            }
            @Override
            public void onBufferReceived(byte[] buffer) {

            }
            @Override
            public void onEndOfSpeech() {
                text.setText("Please speak again.");
                mic.setVisibility(View.VISIBLE);
            }
            @Override
            public void onError(int error) {
                Log.e("11111","errorrrrrrrrrrrrrrrrr");
            }
            @Override
            public void onPartialResults(Bundle partialResults) {

            }
            @Override
            public void onEvent(int eventType, Bundle params) {

            }
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(matches!=null){
                    String s = matches.get(0);
                    text.setText(s);
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();

                    if(s.contains("time ") || ((s.contains("tell") || s.contains("what")) && s.contains("time")) || s.contains("current time")){
                        Log.d(LOG_TAG, "searching for time");

                        // eg. what is the time in *city name* , what is the time of *city name*, current time in *city name*.....
                        if(s.contains("time in ") || s.contains("time of ")){
                            String[] strings = s.split(" ");
                            int t=0;
                            for(t=0; t<strings.length; t++){
                                if(strings[t].equals("time")){
                                    break;
                                }
                            }
                            ArrayList<String> stringList = new ArrayList<>();
                            for(int i=t+2; i<strings.length; i++){
                                stringList.add(strings[i]);
                            }
                            for(int i=0; i<stringList.size(); i++){
                                Log.d(LOG_TAG, "bbbb "+stringList.get(i));
                            }
                            if(t+2<strings.length){
                                if (haveNetworkConnection()){
                                    mic.setVisibility(View.INVISIBLE);
                                    progressBar.setVisibility(View.VISIBLE);
                                    // first api is called to get location details and then in it we call api to get time of that location
                                    getLocation(stringList);
                                }
                                else {
                                    speak("Sorry! No internet connection.");
                                    finish();
                                }
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                        else{
                            Date now = new Date();
                            String time  = DateUtils.formatDateTime(DialogActivity.this, now.getTime(), DateUtils.FORMAT_SHOW_TIME);
                            speak("The time is " + time);
                            finish();
                        }
                    }

                    // eg. how is the weather in *city name*, weather in *city name*, weather of *city name*
                    else if(s.contains("weather in") || s.contains("weather of")){
                        String[] strings = s.split(" ");
                        int t=0;
                        for(t=0; t<strings.length; t++){
                            if(strings[t].equals("weather")){
                                break;
                            }
                        }
                        ArrayList<String> stringList = new ArrayList<>();

                        for(int i=t+2; i<strings.length; i++){
                            stringList.add(strings[i]);
                        }
                        for(int i=0; i<stringList.size(); i++){
                            Log.d(LOG_TAG, "bbbb "+stringList.get(i));
                        }
                        if(t+2<strings.length){
                            if (haveNetworkConnection()){
                                mic.setVisibility(View.INVISIBLE);
                                progressBar.setVisibility(View.VISIBLE);
                                getWeather(stringList);
                            }
                            else {
                                speak("Sorry! No internet connection.");
                                finish();
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    // eg. what day is it, what is the date today, tomorrow's date.....
                    else if(s.contains(" day ") || s.contains("date")){
                        Log.d(LOG_TAG, "searching for date");
                        Date now = new Date();
                        if(s.contains("tomorrow")){
                            Date tomorrow = new Date(now.getTime() + (1000 * 60 * 60 * 24));
                            speak("Tomorrow's date is " + tomorrow);
                        }
                        else if(s.contains("yesterday")){
                            Date yesterday = new Date(now.getTime() - (1000 * 60 * 60 * 24));
                            speak("Yesterday's date was " + yesterday);
                        }
                        else{
                            speak("Today's date is " + now);
                        }
                        finish();
                    }

                    // eg. set an alarm for 5:30 a.m., wake me up at 5:30 a.m., set an alarm for 6 p.m. of mondays......
                    else if(s.contains("alarm") || s.contains("wake me up")){
                        if(s.contains("a.m.") || s.contains("p.m.")){
                            if (s.contains(":")) {
                                int x = s.indexOf(':');
                                if(s.charAt(x-2) == ' '){
                                    hour = s.charAt(x-1) - '0';
                                    min = Integer.parseInt(s.substring(x+1,x+3));
                                }
                                else{
                                    hour = Integer.parseInt(s.substring(x-2,x));
                                    min = Integer.parseInt(s.substring(x+1,x+3));
                                }
                            }
                            else{
                                String intValue = s.replaceAll("[^0-9]", "");
                                hour = Integer.parseInt(intValue);
                                min = 0;
                            }
                        }
                        else {
                            if (s.contains(":")) {
                                int x = s.indexOf(':');
                                if(s.charAt(x-2) == ' '){
                                    hour = s.charAt(x-1) - '0';
                                    min = Integer.parseInt(s.substring(x+1,x+3));
                                }
                                else{
                                    hour = Integer.parseInt(s.substring(x-2,x));
                                    min = Integer.parseInt(s.substring(x+1,x+3));
                                }
                            }
                            else{
                                String intValue = s.replaceAll("[^0-9]", "");
                                hour = Integer.parseInt(intValue);
                                min = 0;
                            }
                        }
                        alarmDays = new ArrayList<>();
                        if(s.contains("monday") || s.contains("tuesday") || s.contains("wednesday") || s.contains("thursday") || s.contains("friday") || s.contains("saturday") || s.contains("sunday")
                                || s.contains("Monday") || s.contains("Tuesday") || s.contains("Wednesday") || s.contains("Thursday") || s.contains("Friday") || s.contains("Saturday") || s.contains("Sunday")){

                            if(s.contains("sunday") || s.contains("Sunday"))
                                alarmDays.add(Calendar.SUNDAY);
                            if(s.contains("monday") || s.contains("Monday"))
                                alarmDays.add(Calendar.MONDAY);
                            if(s.contains("tuesday") || s.contains("Tuesday"))
                                alarmDays.add(Calendar.TUESDAY);
                            if(s.contains("wednesday") || s.contains("Wednesday"))
                                alarmDays.add(Calendar.WEDNESDAY);
                            if(s.contains("thursday") || s.contains("Thursday"))
                                alarmDays.add(Calendar.THURSDAY);
                            if(s.contains("friday") || s.contains("Friday"))
                                alarmDays.add(Calendar.FRIDAY);
                            if(s.contains("saturday") || s.contains("Saturday"))
                                alarmDays.add(Calendar.SATURDAY);
                        }

                        if(alarmDays.size()==0){
                            if(s.contains("p.m")){
                                createAlarm("Alarm set", hour+12, min);
                                if(min==0){
                                    speak("Alarm has be'en set for "+hour+" P.M");
                                }else
                                    speak("Alarm has been set for "+hour+" "+min+" P.M");
                            }
                            else {
                                createAlarm("Alarm set", hour, min);
                                if(min==0){
                                    speak("Alarm has been set for "+hour+" A.M");
                                }else
                                    speak("Alarm has been set for "+hour+" "+min+" A.M");
                            }
                        }
                        else {
                            if(s.contains("p.m")){
                                createAlarm("Alarm set", hour+12, min, alarmDays);
                                if(min==0){
                                    speak("Alarm has been set for "+hour+" P.M");
                                }else
                                    speak("Alarm has been set for "+hour+" "+min+" P.M");
                            }
                            else {
                                createAlarm("Alarm set", hour, min, alarmDays);
                                if(min==0){
                                    speak("Alarm has been set for "+hour+" A.M");
                                }else
                                    speak("Alarm has been set for "+hour+" "+min+" A.M");
                            }
                        }
                        finish();
                    }

                    // eg. start a timer for 5 minutes
                    else if(s.contains(" timer ")){
                        if(s.contains("minutes")){
                            String intValue = s.replaceAll("[^0-9]", "");
                            min = Integer.parseInt(intValue);
                            startTimer("Timer started", min*60);
                            speak("Timer has been started for "+min+" minutes");
                        }
                        finish();
                    }

                    else if(s.contains("open")){
                        String app = s.substring(s.indexOf(" ")+1, s.length());
                        Log.d("asdfgh",app);
                        if(app.equals("Twitter")){
                            speak("opening Twitter");
                            Intent i = getPackageManager().getLaunchIntentForPackage("com.twitter.android");
                            Log.d(LOG_TAG,app);
                            startActivity(i);
                        }
                        else if(app.equals("Facebook")){
                            speak("opening Facebook");
                            Intent i = getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
                            startActivity(i);
                        }
                        finish();
                    }
                    // if no other if statement satisfies, result is called from wolfram alpha API
                    else{
                        String[] strings = s.split(" ");
                        String input = "";
                        for(int i=0; i<strings.length; i++){
                            input = input.concat(strings[i]+"+");
                        }
                        Log.e(LOG_TAG, input);

                        if (haveNetworkConnection()){
                            mic.setVisibility(View.INVISIBLE);
                            progressBar.setVisibility(View.VISIBLE);
                            getResult(input);
                        }
                        else {
                            speak("Sorry! No internet connection.");
                            finish();
                        }
                    }
                }
            }
        });
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }

    // To get results from wolfram alpha API
    private void getResult(String input) {
        String url = "http://api.wolframalpha.com/v1/spoken?appid=49GUL8-5L583HAW99&i=";
        url = url.concat(input+"%3f");

        Log.d(LOG_TAG, "getResult: "+url);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        if(!response.contains("Wolfram Alpha")){
                            speak(response);
                            finish();
                        }
                        else {
                            speak("Sorry! Could not understand input.");
                            finish();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(stringRequest);
    }

    // To get location details(latitude and longitude) of any area from google maps api
    private void getLocation(final ArrayList<String> strings) {
        String url = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        final Double[] lat_lng = new Double[2];

        for(int i=0; i<strings.size(); i++){
            url = url.concat(strings.get(i));
        }
        Log.d("aaaaaaaaaaaa", url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    for(int i=0; i<results.length(); i++){
                        JSONObject object = results.getJSONObject(i);
                        JSONObject geometry = object.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        lat_lng[0] = location.getDouble("lat");
                        lat_lng[1] = location.getDouble("lng");
                        Log.d(LOG_TAG, ""+lat_lng[0]+" "+lat_lng[1]);

                        //  To get time after we get the location details
                        getTime(lat_lng, strings);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mic.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Log.d(LOG_TAG, "onErrorResponse: ");
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    // To get time from location details
    private void getTime(Double[] lat_lng, final ArrayList<String> st) {
        String url = "https://api.timezonedb.com/v2/get-time-zone?";
        String key = "CNUHMGOE5KLX";

        Uri uri = Uri.parse(url);
        Uri api_uri =  uri.buildUpon()
                .appendQueryParameter("key", key)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("by", "position")
                .appendQueryParameter("lat", lat_lng[0].toString())
                .appendQueryParameter("lng", lat_lng[1].toString())
                .build();

        String URL = api_uri.toString();
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        final String[] formatted = {""};
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            formatted[0] = response.getString("formatted");
                            timeFromLatNLng = formatted[0].split(" ")[1];
                            String mTime = timeFromLatNLng;
                            String hours = mTime.split(":")[0];
                            String minutes = mTime.split(":")[1];

                            mic.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            if (st.size() > 0) {
                                speak("The time in "+st+" is "+hours+" hours and "+minutes+" minutes");
                            }
                            else {
                                speak("Error collecting data");
                            }
                            Toast.makeText(getApplicationContext(), "The time in "+st+" is "+hours+" hours and "+minutes+" minutes", Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mic.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Log.d(LOG_TAG, "onErrorResponse: ");
                speak("Error collecting data");
                finish();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    // to get weather from openweathermap api
    private void getWeather(final ArrayList<String> strings) {
        String url = "https://api.openweathermap.org/data/2.5/weather?appid=c38e047f05537a8118ff15571728277d&q=";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        for(int i=0; i<strings.size(); i++){
            url = url.concat(strings.get(i));
        }
        Log.d("weatherUrl", url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray weather = response.getJSONArray("weather");
                            JSONObject object = weather.getJSONObject(0);
                            String description = object.getString("description");

                            JSONObject main = response.getJSONObject("main");
                            Double t = main.getDouble("temp") - 273.15;
                            int humidity = main.getInt("humidity");

                            int temp = t.intValue();

                            mic.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            if (weather!=null) {
                                speak("Currently in "+strings+" temperature is "+temp+" degrees celsius with "+description+" and "+humidity+" percent humidity.");
                            }
                            else {
                                speak("Error collecting data");
                            }
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mic.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Log.d(LOG_TAG, "onErrorResponse: ");
                speak("Error collecting data");
                finish();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    // To set alarm once
    public void createAlarm(String message, int hour, int minutes) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    // To set repetitive alarm on particular days
    public void createAlarm(String message, int hour, int minutes,ArrayList<Integer> alarmDays) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                .putExtra(AlarmClock.EXTRA_DAYS, alarmDays);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    public void startTimer(String message, int seconds) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                .putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    private void initializeTextToSpeech() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(tts.getEngines().size() == 0){
                    Toast.makeText(DialogActivity.this, "No TTS Engine found", Toast.LENGTH_SHORT).show();
                }
                else {
                    tts.setLanguage(Locale.US);

                    // To get selected voice from MainActivity
                    voice = MainActivity.getInstance().getVoice();

                    if(voice!=null){
                        tts.setVoice(voice);
                    }
                }
            }
        });
    }
    private void speak(String message) {
        if(Build.VERSION.SDK_INT >= 22){
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }else {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        tts.shutdown();
        mSpeechRecognizer.destroy();
    }

    public boolean haveNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo!=null && networkInfo.isConnected()){
            return true;
        }
        return false;
    }
}
