package com.halake.anone;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.halake.anone.models.Message;
import com.halake.anone.models.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class API {
    private static RequestQueue requestQueue;
    private static String BASE_URL = "http://13.71.156.156:4567/api";

    public static void setup(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public static class FetchMessages extends JsonArrayRequest {
        public FetchMessages(Response.Listener<JSONArray> listener,
                          Response.ErrorListener errorListener, int limit) {
            super(BASE_URL + "/papa/messages?limit=" + limit, listener, errorListener);
        }

        interface Listener {
            void onSuccess(List<Message> messages);
            void onError();
        }
    }

    public static void fetchMessages(final FetchMessages.Listener listener) {
        requestQueue.add(new FetchMessages(new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(final JSONArray result) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onSuccess(Message.json2List(result));
                        Log.d("", result.toString());
                    }
                }).start();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError();
            }
        }, 10));
    }

    public static class PostStamp extends JsonObjectRequest {
        private static JSONObject request(String to) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("to", to);
            } catch (JSONException e) {
                /* ignore */
            }
            return obj;
        }

        public PostStamp(final Listener listener, String to, final Bitmap stampBitmap) {
            super(Method.POST, BASE_URL + "/papa/stamps/to/" + to, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject obj) {
                    String path;
                    try {
                        path = obj.getString("post_path");
                    } catch (JSONException e) {
                        listener.onError();
                        return;
                    }
                    requestQueue.add(new FileRequest(path, listener, Utils.bitmapBytes(stampBitmap)));
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.onError();
                }
            });
        }

        private static class FileRequest extends Request<String> {
            private byte[] body;
            private Listener listener;

            public FileRequest(String url, final Listener listener, byte[] body) {
                super(Method.POST, url, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError();
                    }
                });
                this.listener = listener;
                this.body = body;
            }

            @Override
            public String getBodyContentType() {
                return "application/octet-stream";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return this.body;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                return Response.success("Uploaded", getCacheEntry());

            }

            @Override
            protected void deliverResponse(String response) {
                this.listener.onSuccess();
            }
        }

        interface Listener {
            void onSuccess();
            void onError();
        }
    }

    public static void postStamp(final Bitmap bitmap, String to, final PostStamp.Listener listener) {
        requestQueue.add(new PostStamp(listener, to, bitmap));
    }

    public static void postToken(String token) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(BASE_URL + "/papa/token/android").openConnection();

            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);

            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("token", token);
            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();

            StringBuilder response = new StringBuilder();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            Log.d("", response.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
