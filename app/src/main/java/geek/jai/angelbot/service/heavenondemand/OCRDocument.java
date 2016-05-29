package geek.jai.angelbot.service.heavenondemand;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
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
public class OCRDocument implements IHODClientCallback {

    public static final String OCR_ACTION = "ocrdocument";
    private HODClient hodClient = new HODClient(ChatBotApp.HPE_API_KEY, this);
    private String jobID;
    private int MODE = 0;
    private int count = 0;

    private Context mContext;

    public OCRDocument(Context context) {
        mContext = context;
    }

    public void makeRequest(String filepath) {
        Log.d(OCRDocument.class.getSimpleName(), "makeRequest filepath : " + filepath);
        String hodApp = HODApps.OCR_DOCUMENT;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("file", filepath);
        params.put("mode", "document_photo");
        List<String> languages = new ArrayList<>();
        languages.add("en");
        params.put("languages", languages);
        MODE = 1;
        hodClient.PostRequest(params, hodApp, HODClient.REQ_MODE.ASYNC);
    }

    @Override
    public void requestCompletedWithContent(String response) {
        Log.d(OCRDocument.class.getSimpleName(), "requestCompletedWithContent : " + response);
        Gson gson = new Gson();
        /*Type mapType = new
                TypeToken<HashMap<String, String>>() {
                }.getType();*/
        if (MODE == 0) {

        } else if (MODE == 1) {
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
                List textBlocks = (List) result.get("text_block");
                if (textBlocks != null) {
                    //textBlocks done.
                    if (textBlocks.size() != 0) {
                        String text = (String) ((Map) textBlocks.get(0)).get("text");
                        //text fetched.. :)
                        APIRequest.getInstance(mContext)
                                .sendChat(false, text,
                                        new APIRequest.ChatCallback() {
                                            @Override
                                            public void reply(String reply) {
                                                Intent returnIntent = new Intent();
                                                returnIntent.setAction(OCR_ACTION);
                                                returnIntent.putExtra("message", reply);
                                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(returnIntent);
                                            }

                                            @Override
                                            public void error(int errorType, String message) {

                                            }
                                        });
                        //Log.d(OCRDocument.class.getSimpleName(), text);
                    } else {
                        Intent returnIntent = new Intent();
                        returnIntent.setAction(OCR_ACTION);
                        returnIntent.putExtra("message", "error");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(returnIntent);
                    }
                }
            }
        }

    }

    @Override
    public void requestCompletedWithJobID(String response) {
        Log.d(OCRDocument.class.getSimpleName(), response);
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

    /**
     * Intent returnIntent = new Intent();
     returnIntent.setAction(RECEIVE_PIC_URL);
     returnIntent.putExtra("pic_url", url);
     LocalBroadcastManager.getInstance(ImageUploadService.this).sendBroadcast(returnIntent);
     */
}
