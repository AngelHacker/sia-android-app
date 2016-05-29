package geek.jai.angelbot.service.heavenondemand;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import geek.jai.angelbot.app.ChatBotApp;
import geek.jai.angelbot.network.APIRequest;
import hod.api.hodclient.HODApps;
import hod.api.hodclient.HODClient;
import hod.api.hodclient.IHODClientCallback;

/**
 * Created by JAID on 28-05-2016.
 */
public class BarcodeRecognition implements IHODClientCallback {

    public static final String BARCODE_ACTION = "barcode";
    private HODClient hodClient = new HODClient(ChatBotApp.HPE_API_KEY, this);
    private String jobID;
    private int MODE = 0;
    private Context mContext;

    public BarcodeRecognition(Context context) {
        mContext = context;
    }

    public void makeRequest(String filepath) {
        Log.d(BarcodeRecognition.class.getSimpleName(), "makeRequest filepath : " + filepath);

        String hodApp = HODApps.RECOGNIZE_BARCODES;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("file", filepath);
        params.put("barcode_orientation", "upright");
        List<String> type = new ArrayList<>();
        type.add("all1d");
        type.add("qr");
        params.put("barcode_type", type);

        MODE = 1;
        hodClient.PostRequest(params, hodApp, HODClient.REQ_MODE.ASYNC);
    }

    @Override
    public void requestCompletedWithContent(String response) {
        Log.d(BarcodeRecognition.class.getSimpleName(), "requestCompletedWithContent : " + response);
        Gson gson = new Gson();
        if (MODE == 1) {
            Map jsonMap = gson.fromJson(response, Map.class);
            String status = (String) jsonMap.get("status");
            String jobId = (String) jsonMap.get("jobID");
            if (!status.equals("finished")) {
                hodClient.GetJobResult(jobId);
                return;
            }
            List<Map<String, Object>> actions = (List) jsonMap.get("actions");
            if (actions.size() != 0) {
                Map result = (Map) actions.get(0).get("result");
                List barcodes = (List) result.get("barcode");
                if (barcodes != null) {
                    //textBlocks done.
                    if (barcodes.size() != 0) {
                        String text = (String) ((Map) barcodes.get(0)).get("text");
                        //text fetched.. :)
                        APIRequest.getInstance(mContext)
                                .sendChat(false, text,
                                        new APIRequest.ChatCallback() {
                                            @Override
                                            public void reply(String reply) {
                                                Intent returnIntent = new Intent();
                                                returnIntent.setAction(BARCODE_ACTION);
                                                returnIntent.putExtra("message", reply);
                                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(returnIntent);
                                            }

                                            @Override
                                            public void error(int errorType, String message) {

                                            }
                                        });
                        //Log.d(OCRDocument.class.getSimpleName(), text);
                    } else {
                        //Log.d(OCRDocument.class.getSimpleName(), "Unable to read");
                        Intent returnIntent = new Intent();
                        returnIntent.setAction(BARCODE_ACTION);
                        returnIntent.putExtra("message", "error");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(returnIntent);
                    }
                }
            }
        }
    }

    @Override
    public void requestCompletedWithJobID(String response) {
        Log.d(BarcodeRecognition.class.getSimpleName(), response);
        try {
            JSONObject mainObject = new JSONObject(response);
            if (!mainObject.isNull("jobID")) {
                jobID = mainObject.getString("jobID");
                hodClient.GetJobResult(jobID);
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void onErrorOccurred(String errorMessage) {

    }
}
