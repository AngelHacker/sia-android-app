package geek.jai.angelbot.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import geek.jai.angelbot.R;
import geek.jai.angelbot.adapter.ChatDisplayAdapter;
import geek.jai.angelbot.modal.Chat;
import geek.jai.angelbot.network.APIRequest;
import geek.jai.angelbot.service.camera.Camera;
import geek.jai.angelbot.service.heavenondemand.BarcodeRecognition;
import geek.jai.angelbot.service.heavenondemand.OCRDocument;
import geek.jai.angelbot.util.ImageScaling;

/**
 * Created by JAID on 28-05-2016.
 */
public class MainActivity extends Activity implements TextWatcher,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        TextToSpeech.OnInitListener {

    //private TextView txtSpeechInput;
    private RecyclerView myChatRecyclerView;
    private EditText chatBox;
    private ImageButton btnSpeak;
    private ImageButton btnCamera;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    private ChatDisplayAdapter myChatDisplayAdapter;
    private List<Chat> myChatList;

    private boolean isSpeakEnabled;
    private Camera camera;

    private ScrollView optionScroll;

    private GoogleApiClient client;

    private boolean isFirstChat = true;

    private ImageButton refreshChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myChatRecyclerView = (RecyclerView) findViewById(R.id.chatRecycler);
        //txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        myChatList = new ArrayList<>();

        /*Chat chat = new Chat("Chat Received", 1);
        myChatList.add(chat);*/

        myChatDisplayAdapter = new ChatDisplayAdapter(this, myChatList);
        myChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myChatRecyclerView.setAdapter(myChatDisplayAdapter);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        refreshChat = (ImageButton) findViewById(R.id.refreshChat);

        chatBox = (EditText) findViewById(R.id.chatBox);
        chatBox.addTextChangedListener(this);

        isSpeakEnabled = true;
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isSpeakEnabled) {
                    promptSpeechInput();
                } else {
                    if (chatBox.getText().length() >= 0) {
                        sendMessage(chatBox.getText().toString());
                        chatBox.setText("");
                    }

                }

            }
        });
        btnCamera = (ImageButton) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera = new Camera(MainActivity.this);
                camera.startCamera();
            }
        });

        refreshChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLastLatitude != 0 && mLastLongitude != 0) {
                    APIRequest.getInstance(MainActivity.this)
                            .endSIA(mLastLongitude + ";" + mLastLatitude,
                                    new APIRequest.EndSIACallback() {
                                        @Override
                                        public void reply(String reply) {

                                        }

                                        @Override
                                        public void error(int errorType, String message) {

                                        }
                                    });
                }
                addITPOptions();
                firstAPICall();
            }
        });

        optionScroll = (ScrollView) findViewById(R.id.optionScroll);
        optionScroll.setVisibility(View.GONE);
        addITPOptions();
        //addConfirmation();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        // Create an instance of GoogleAPIClient.
        if (client == null) {
            client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        firstAPICall();
    }

    private void firstAPICall() {
        APIRequest.getInstance(this)
                .sendChat(true,
                        "start",
                        new APIRequest.ChatCallback() {
                            @Override
                            public void reply(String reply) {
                                writeReceivedChats(reply);
                            }

                            @Override
                            public void error(int errorType, String message) {
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void writeSentMessage(String chatMessage) {
        myChatList.add(new Chat(chatMessage, 0));
        int pos = myChatList.size() - 1;
        myChatDisplayAdapter.notifyItemInserted(pos);
        myChatRecyclerView.smoothScrollToPosition(pos + 1);
    }

    private void sendMessage(String chatMessage) {
        myChatList.add(new Chat(chatMessage, 0));
        int pos = myChatList.size() - 1;
        myChatDisplayAdapter.notifyItemInserted(pos);
        myChatRecyclerView.smoothScrollToPosition(pos + 1);
        //call volley service
        APIRequest.getInstance(this)
                .sendChat(false,
                        chatMessage,
                        new APIRequest.ChatCallback() {
                            @Override
                            public void reply(String reply) {
                                writeReceivedChats(reply);
                            }

                            @Override
                            public void error(int errorType, String message) {
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //txtSpeechInput.setText(result.get(0));
                    //add to list and refresh that position.
                    chatBox.setText(result.get(0));
                }
                break;

            case Camera.CAPTURE_IMAGE_ACTIVITY_REQ:
                Uri uri = camera.handleAndGetImageUri(requestCode, resultCode, data, null);
                //file:///storage/emulated/0/Pictures/IMG_20160528_184344.jpg
                if (uri == null) return;
                //Calling OCR

                String actualPath = uri.getEncodedPath();
                //Log.d(MainActivity.class.getSimpleName(), "Actual Path : " + actualPath);

                //Scaling image
                ImageScaling scaling = new ImageScaling();
                String path = scaling.decodeFile(actualPath, 150, 150);
                //Log.d(MainActivity.class.getSimpleName(), "Scaled Image Path : " + path);

                //Adding to list
                Chat chat = new Chat();
                chat.setFileURI(path);
                chat.setType(2);
                myChatList.add(chat);
                //notify recycler
                int pos = myChatList.size() - 1;
                myChatDisplayAdapter.notifyItemInserted(pos);
                myChatRecyclerView.smoothScrollToPosition(pos + 1);

                addCameraOptions(actualPath);
                break;


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        client.connect();
        super.onStart();

        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.ENGLISH);
        textToSpeech.setPitch(0.6f);
        textToSpeech.setSpeechRate(2);
    }

    @Override
    protected void onStop() {
        client.disconnect();
        super.onStop();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() == 0) {
            isSpeakEnabled = true;
            btnSpeak.setImageResource(R.drawable.ic_mic_white_48dp);
        } else {
            isSpeakEnabled = false;
            //R.drawable.ic_send_white_48dp
            btnSpeak.setImageResource(R.drawable.ic_send_white_48dp);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    //Inflate Camera options
    public void addCameraOptions(final String actualPath) {
        //Toast.makeText(this, "addCameraOptions", Toast.LENGTH_SHORT).show();
        optionScroll.setVisibility(View.VISIBLE);
        optionScroll.removeAllViews();
        View view = View.inflate(this, R.layout.camera_options, optionScroll);
        Button barcode = (Button) view.findViewById(R.id.barcodeBtn);
        Button screen = (Button) view.findViewById(R.id.screenBtn);

        barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Barcode portion
                registerBroadcast(BarcodeRecognition.BARCODE_ACTION); //register broadcast
                BarcodeRecognition barcodeRecognition = new BarcodeRecognition(MainActivity.this);
                barcodeRecognition.makeRequest(actualPath);
                optionScroll.removeAllViews();
                optionScroll.setVisibility(View.GONE);
            }
        });

        screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //OCRDocument
                registerBroadcast(OCRDocument.OCR_ACTION); //register broadcast
                OCRDocument ocrDocument = new OCRDocument(MainActivity.this);
                ocrDocument.makeRequest(actualPath);
                optionScroll.removeAllViews();
                optionScroll.setVisibility(View.GONE);
            }
        });
    }

    //Inflate Camera options
    public void addITPOptions() {
        //Toast.makeText(this, "addCameraOptions", Toast.LENGTH_SHORT).show();
        optionScroll.setVisibility(View.VISIBLE);
        optionScroll.removeAllViews();
        View view = View.inflate(this, R.layout.issue_ticket_purchase, optionScroll);

        ImageButton issue = (ImageButton) view.findViewById(R.id.btnIssues);
        issue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Issues
                //Toast.makeText(MainActivity.this, "Issues Clicked", Toast.LENGTH_SHORT).show();
                APIRequest.getInstance(MainActivity.this)
                        .sendChat(false,
                                "issue", new APIRequest.ChatCallback() {
                                    @Override
                                    public void reply(String reply) {
                                        writeSentMessage("issue");
                                        optionScroll.setVisibility(View.GONE);
                                        writeReceivedChats(reply);
                                    }

                                    @Override
                                    public void error(int errorType, String message) {

                                    }
                                });
            }
        });

        ImageButton ticket = (ImageButton) view.findViewById(R.id.btnTicket);
        ticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ticket
                //Toast.makeText(MainActivity.this, "Ticket Clicked", Toast.LENGTH_SHORT).show();
                APIRequest.getInstance(MainActivity.this)
                        .sendChat(false,
                                "ticket", new APIRequest.ChatCallback() {
                                    @Override
                                    public void reply(String reply) {
                                        writeSentMessage("ticket");
                                        optionScroll.setVisibility(View.GONE);
                                        writeReceivedChats(reply);
                                    }

                                    @Override
                                    public void error(int errorType, String message) {

                                    }
                                });

            }
        });
        /*
        ImageButton purchase = (ImageButton) view.findViewById(R.id.btnPurchase);
        purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //purchase
                //Toast.makeText(MainActivity.this, "Purchase Clicked", Toast.LENGTH_SHORT).show();
                APIRequest.getInstance(MainActivity.this)
                        .sendChat(false,
                                "purchase", new APIRequest.ChatCallback() {
                                    @Override
                                    public void reply(String reply) {
                                        writeSentMessage("purchase");
                                        optionScroll.setVisibility(View.GONE);
                                        writeReceivedChats(reply);
                                    }

                                    @Override
                                    public void error(int errorType, String message) {

                                    }
                                });
            }
        });*/
    }

    //Inflate Confirmation
    public void addConfirmation() {
        //Toast.makeText(this, "addCameraOptions", Toast.LENGTH_SHORT).show();
        optionScroll.setVisibility(View.VISIBLE);
        optionScroll.removeAllViews();
        View view = View.inflate(this, R.layout.confirmation, optionScroll);

        Button yes = (Button) view.findViewById(R.id.btnYes);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionScroll.setVisibility(View.GONE);
                writeSentMessage("yes");
                //Issues
                //Toast.makeText(MainActivity.this, "Yes Clicked", Toast.LENGTH_SHORT).show();
                APIRequest.getInstance(MainActivity.this)
                        .sendChat(false,
                                "yes",
                                new APIRequest.ChatCallback() {
                                    @Override
                                    public void reply(String reply) {
                                        writeReceivedChats(reply);
                                    }

                                    @Override
                                    public void error(int errorType, String message) {

                                    }
                                });
            }
        });

        Button no = (Button) view.findViewById(R.id.btnNo);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionScroll.setVisibility(View.GONE);
                writeSentMessage("no");
                //Issues
                //Toast.makeText(MainActivity.this, "No Clicked", Toast.LENGTH_SHORT).show();
                APIRequest.getInstance(MainActivity.this)
                        .sendChat(false,
                                "no",
                                new APIRequest.ChatCallback() {
                                    @Override
                                    public void reply(String reply) {
                                        writeReceivedChats(reply);
                                    }

                                    @Override
                                    public void error(int errorType, String message) {

                                    }
                                });
            }
        });
    }

    //Working on Barcode and OCR for broadcasting
    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(MainActivity.this, "BroadcastReceiver", Toast.LENGTH_SHORT).show();
            if (intent.getAction().equals(BarcodeRecognition.BARCODE_ACTION)) {
                String message = intent.getStringExtra("message");
                if (message.equals("error")) {
                    writeReceivedChats("Failed to Read Barcode");
                } else {
                    writeReceivedChats(message);
                }
                unregisterBarcode();
                //isFirstChat = false;

            } else if (intent.getAction().equals(OCRDocument.OCR_ACTION)) {
                String message = intent.getStringExtra("message");
                if (message.equals("error")) {
                    writeReceivedChats("Failed to Read OCR");
                } else {
                    writeReceivedChats(message);
                }

                unregisterOCR();
                //isFirstChat = false;
            }
        }
    };

    private void registerBroadcast(String action) {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        bManager.registerReceiver(bReceiver, intentFilter);
    }

    protected void unregisterBarcode() {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        bManager.unregisterReceiver(bReceiver);
    }

    protected void unregisterOCR() {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        bManager.unregisterReceiver(bReceiver);
    }

    private Location mLastLocation;
    private double mLastLatitude;
    private double mLastLongitude;

    //Location Integration
    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                client);
        if (mLastLocation != null) {
            mLastLatitude = mLastLocation.getLatitude();
            mLastLongitude = mLastLocation.getLongitude();
            //Toast.makeText(MainActivity.this, mLastLatitude + "\n" + mLastLongitude, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void writeReceivedChats(String message) {
        if (message.contains("Please select one of the below options to proceed further.")) {
            addITPOptions();
        } else if (message.contains("Can I do anything else for you?")) {
            addConfirmation();
        }
        myChatList.add(new Chat(message, 1));
        int pos = myChatList.size() - 1;
        myChatDisplayAdapter.notifyItemInserted(pos);
        myChatRecyclerView.smoothScrollToPosition(pos + 1);
        speakOut(message);
    }

    private TextToSpeech textToSpeech;

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = textToSpeech.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
                speakOut("This Language is not supported");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    private void speakOut(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
