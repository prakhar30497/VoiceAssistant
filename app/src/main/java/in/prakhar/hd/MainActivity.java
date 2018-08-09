package in.prakhar.hd;

import android.Manifest;
import android.app.assist.AssistContent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Switch enable_jarvis_switch;
    private TextView jarvis_text;

    private boolean jarvis_enabled;
    Intent serviceIntent;
//    FloatingActionButton mic;
    private Spinner countrySpinner;
    private Spinner voiceSpinner;

    private TextToSpeech textToSpeech;

    List<String> countries;
    List<Voice> allVoices;
    List<String> voicesName;
    List<String> USVoicesName;
    List<String> INVoicesName;
    List<String> GBVoicesName;
    List<String> AUVoicesName;

    List<String> USVoicesNameShow;
    List<String> INVoicesNameShow;
    List<String> GBVoicesNameShow;
    List<String> AUVoicesNameShow;

    String selected_voice_name;
    String selected_country;
    private Voice voice;

    private static MainActivity instance;

    ArrayAdapter<String> auVoiceAdapter;
    ArrayAdapter<String> inVoiceAdapter;
    ArrayAdapter<String> usVoiceAdapter;
    ArrayAdapter<String> gbVoiceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        enable_jarvis_switch = findViewById(R.id.switch_jarvis);
        jarvis_text = findViewById(R.id.tv_jarvis);
//        mic = findViewById(R.id.fab_mic);
        countrySpinner = findViewById(R.id.spinner);
        voiceSpinner = findViewById(R.id.voice_spinner);

        voiceSpinner.setEnabled(false);

        countries = Arrays.asList("AU", "IN", "GB", "US");

        allVoices = new ArrayList<>(); // List of all voices with type Voice
        voicesName = new ArrayList<>(); // List of name of all voice

        // These are the List of name of particaular country
        INVoicesName = new ArrayList<>();
        AUVoicesName = new ArrayList<>();
        USVoicesName = new ArrayList<>();
        GBVoicesName = new ArrayList<>();

        // These are the list to be shown in UI i.e. Voice 1, Voice 2
        INVoicesNameShow = new ArrayList<>();
        AUVoicesNameShow = new ArrayList<>();
        USVoicesNameShow = new ArrayList<>();
        GBVoicesNameShow = new ArrayList<>();

        jarvis_enabled = false;

        // *** This is used this to get all voices and filling all Lists and setting up spinner adapters
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.US);
                Set<Voice> voices = textToSpeech.getVoices();
                List<Voice> voiceList = new ArrayList<>(voices);

                for(int i=0; i<voiceList.size(); i++){
                    Log.d("VoiceList", voiceList.get(i).toString());
                    if (voiceList.get(i).getName().contains("en-") && !voiceList.get(i).isNetworkConnectionRequired())
                        allVoices.add(voiceList.get(i));
                }

                for (int i=0; i<allVoices.size(); i++) {
                    Log.d("AllVoices", allVoices.get(i).getName());
                    voicesName.add(allVoices.get(i).getName());

                    if(allVoices.get(i).getName().contains("au") || allVoices.get(i).getName().contains("AU")) {
                        AUVoicesName.add(allVoices.get(i).getName());
                        AUVoicesNameShow.add("Voice "+AUVoicesName.size());
                    }else if(allVoices.get(i).getName().contains("us") || allVoices.get(i).getName().contains("US")) {
                        USVoicesName.add(allVoices.get(i).getName());
                        USVoicesNameShow.add("Voice "+USVoicesName.size());
                    }else if(allVoices.get(i).getName().contains("in") || allVoices.get(i).getName().contains("IN")) {
                        INVoicesName.add(allVoices.get(i).getName());
                        INVoicesNameShow.add("Voice "+INVoicesName.size());
                    }else if(allVoices.get(i).getName().contains("gb") || allVoices.get(i).getName().contains("GB")) {
                        GBVoicesName.add(allVoices.get(i).getName());
                        GBVoicesNameShow.add("Voice "+GBVoicesName.size());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, countries);

                auVoiceAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, AUVoicesNameShow);
                usVoiceAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, USVoicesNameShow);
                inVoiceAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, INVoicesNameShow);
                gbVoiceAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, GBVoicesNameShow);


                countrySpinner.setAdapter(adapter);
            }
        });

        // *** to get selected country and voiceSpinner adapter
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_country = parent.getItemAtPosition(position).toString();
                //voiceSpinner is only on selecting a country
                voiceSpinner.setEnabled(true);

                // adapter is set based on country came
                if(selected_country.equals("AU"))
                    voiceSpinner.setAdapter(auVoiceAdapter);
                else if(selected_country.equals("IN"))
                    voiceSpinner.setAdapter(inVoiceAdapter);
                else if(selected_country.equals("GB"))
                    voiceSpinner.setAdapter(gbVoiceAdapter);
                else if(selected_country.equals("US"))
                    voiceSpinner.setAdapter(usVoiceAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // *** To get selected voice name
        voiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch(selected_country){
                    case "AU":{
                        selected_voice_name = AUVoicesName.get(position);
                        break;
                    }
                    case "IN":{
                        selected_voice_name = INVoicesName.get(position);
                        break;
                    }
                    case "GB":{
                        selected_voice_name = GBVoicesName.get(position);
                        break;
                    }
                    case "US":{
                        selected_voice_name = USVoicesName.get(position);
                        break;
                    }
                }
                Log.d("position", " " + position);

                // To find the voice with selected_voice_name
                for (int i=0; i<allVoices.size(); i++) {
                    if(allVoices.get(i).getName().equals(selected_voice_name)){
                        // Selected voice
                        voice = allVoices.get(i);
                        break;
                    }
                }
                Log.d("selectedVoice", voice.getName());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // This called from DialogActivity to get instance
    public static MainActivity getInstance() {
        return instance;
    }

    // This is called when swich is switched
    public void onClick(View view) {
        Log.i(LOG_TAG, "onClick");

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        // if switch is switched on, service is started for hotword detection
        if (enable_jarvis_switch.isChecked()) {
            jarvis_enabled = true;
            jarvis_text.setText("You may speak now...");
            serviceIntent = new Intent(this, VoiceService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }
        // if sewitch is switched off, service is stopped
        else {
            jarvis_enabled = false;
            jarvis_text.setText("Jarvis Disabled");
            serviceIntent = new Intent(this, VoiceService.class);
            stopService(serviceIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //if switch is on, then only start service on resume activity
        if(jarvis_enabled){
            serviceIntent = new Intent(this, VoiceService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        //if switch is on, then only stop service on pause activity
        if(jarvis_enabled){
            serviceIntent = new Intent(this, VoiceService.class);
            stopService(serviceIntent);
        }
        textToSpeech.shutdown();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceIntent = new Intent(this, VoiceService.class);
        stopService(serviceIntent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                jarvis_text.setText("You may speak now...");
                serviceIntent = new Intent(this, VoiceService.class);
                ContextCompat.startForegroundService(this, serviceIntent);
            } else {
                finish();
            }
        }
    }

//    public void onClickMic(View view) {
//        Intent intent = new Intent(this, DialogActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }

    @Override
    public void onProvideAssistContent(AssistContent outContent) {
        super.onProvideAssistContent(outContent);
        Intent intent = new Intent(this, DialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        outContent.setIntent(intent);
    }
    @Override
    public void onProvideAssistData(Bundle data) {
        super.onProvideAssistData(data);
    }

    // Called to get the selected voice
    public Voice getVoice(){
        Log.d(LOG_TAG, "getVoice: "+ voice.getName());
        return voice;
    }
}
