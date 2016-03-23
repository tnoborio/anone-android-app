package com.halake.anone.models;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message {
    public enum Type {
        Audio("audio"), Stamp("stamp");

        private final String jsonValue;

        Type(String jsonValue) {
            this.jsonValue = jsonValue;
        }

        public static Type getEnum(String value) {
            for(Type v : values())
                if(v.jsonValue.equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException("unknown type: " + value);
        }
    }

    public int id;
    public String from;
    public String to;
    public Type type;
    public String message;
    public String url;
    public Date createdAt;

    public static List<Message> json2List(JSONArray jsonArray) {
        List<Message> messages = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Message message = consMessage(jsonArray.getJSONObject(i));
                if (message != null)
                    messages.add(message);

            } catch (JSONException e) {
                // do nothing
            }
        }

        return messages;
    }

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private static Message consMessage(JSONObject jsonObject) {
        // {"id":1,"from":"child","to":"papa","type":"audio","message":"政府","url":"http://13.71.156.156:4567/api/messages/1.wav","created_at":"2016-03-21T03:37:21+00:00"}
        Message message = new Message();
        try {
            message.id = jsonObject.getInt("id");
            message.from = jsonObject.getString("from");
            message.to = jsonObject.getString("to");
            message.type =  Type.getEnum(jsonObject.getString("type"));
            message.message = jsonObject.getString("message");
            message.url = jsonObject.getString("url");
            message.createdAt = dateFormatter.parse(jsonObject.getString("created_at"));

            return message;

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }
}
