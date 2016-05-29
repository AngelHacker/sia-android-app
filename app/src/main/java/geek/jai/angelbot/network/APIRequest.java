package geek.jai.angelbot.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import geek.jai.angelbot.app.ChatBotApp;

/**
 * Created by JAID on 28-05-2016.
 * For making api request
 */
public class APIRequest {

    public final String URL = ChatBotApp.API_URL;
    //volley request queue
    private RequestQueue requestQueue;
    private static APIRequest instance;

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";

    private APIRequest(Context context) {
        this.requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    public static APIRequest getInstance(Context context) {
        if (instance == null) {
            instance = new APIRequest(context);
        }
        return instance;
    }

    private void stringRequest(final HashMap<String, String> dataMap, String url, final APICallback callback) {
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        stringResponse(response, callback);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyError(error, callback);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = dataMap;
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setShouldCache(false);
        this.requestQueue.add(request);
    }

    private void stringGetRequest(String url, final APICallback callback) {

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        stringResponse(response, callback);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyError(error, callback);

            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setShouldCache(false);
        this.requestQueue.add(request);
    }

    private void stringRequest(final HashMap<String, String> dataMap, String url, String tag, final APICallback callback) {
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        stringResponse(response, callback);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyError(error, callback);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = dataMap;
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setShouldCache(false);
        request.setTag(tag);//This tag will be used to cancel a request which is associated with this tag name.
        this.requestQueue.add(request);
    }

    /**
     * Cancel All request which is associated with this tag name.
     *
     * @param tag : tag attached to a request
     */
    public void cancelAllRequest(String tag) {
        if (this.requestQueue != null) {
            this.requestQueue.cancelAll(tag);
        }
    }

    private void stringResponse(String response, APICallback callback) {
        if (callback instanceof ChatCallback) {
            chatCallback(response, (ChatCallback) callback, false, null);
        } else if (callback instanceof EndSIACallback) {
            endSIACallback(response, (EndSIACallback) callback, false, null);
        }
    }

    private void volleyError(VolleyError error, APICallback callback) {
        String volleyError = "Volley Error";
        if (callback instanceof ChatCallback) {
            chatCallback(null, (ChatCallback) callback, true, volleyError);
        } else if (callback instanceof EndSIACallback) {
            endSIACallback(null, (EndSIACallback) callback, true, volleyError);
        }
    }

    public interface ChatCallback extends APICallback {
        void reply(String reply);

        void error(int errorType, String message);
    }

    public void sendChat(boolean isFirst, String chat, ChatCallback callback) {
        //isChatStart=true&input=This
        String url = URL + "siaresponse";

        /*HashMap<String, String> map = new HashMap<>(2);
        if (isFirst) {
            map.put("isChatStart", "true");
        } else {
            map.put("isChatStart", "false");
        }
        map.put("input", chat);*/
        chat = Uri.encode(chat);

        String uri = String.format(url + "?isChatStart=%1$s&input=%2$s",
                isFirst,
                chat);

        stringGetRequest(uri, callback);
    }

    private void chatCallback(String response, ChatCallback callback,
                              boolean isVolleyError, String volleyErrorMessage) {
        if (isVolleyError) {
            callback.error(SystemLevelError.VOLLEY_ERROR, volleyErrorMessage);
            return;
        }
        callback.reply(response);

    }

    public interface EndSIACallback extends APICallback {
        void reply(String reply);

        void error(int errorType, String message);
    }

    public void endSIA(String location, EndSIACallback callback) {
        //isChatStart=true&input=This
        String url = URL + "endsiacall";

        location = Uri.encode(location);

        String uri = String.format(url + "?CustomerLocation=%1$s",
                location);

        stringGetRequest(uri, callback);
    }

    private void endSIACallback(String response, EndSIACallback callback,
                                boolean isVolleyError, String volleyErrorMessage) {
        if (isVolleyError) {
            callback.error(SystemLevelError.VOLLEY_ERROR, volleyErrorMessage);
            return;
        }
        Log.d(APIRequest.class.getSimpleName(), response);
        callback.reply(response);

    }
}
