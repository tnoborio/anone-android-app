package com.halake.anone.models;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class Utils {
    public static byte[] bitmapBytes(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();
    }
}
