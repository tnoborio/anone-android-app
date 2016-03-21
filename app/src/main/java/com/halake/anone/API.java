package com.halake.anone;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class API {
    public static void postToken(String token) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://13.71.156.156:4567/api/papa/token/android").openConnection();

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

            StringBuffer response = new StringBuffer();

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
