package com.halake.anone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

public class ViewUtils {
    public static void roundedImage(Context context, ImageView view, int id) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
        bitmap = roundedBitmap(bitmap, 0.3);

        view.setImageBitmap(bitmap);
    }

    private static Bitmap roundedBitmap(Bitmap bitmap, double radius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, (float)(bitmap.getWidth() * radius), (float)(bitmap.getHeight() * radius), paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static class ImageDownloader extends AsyncTask<Void, Void, Bitmap> {
        ImageView imageView;
        String url;

        public ImageDownloader(ImageView imageView, String url) {
            if (imageView == null)
                throw new RuntimeException();
            this.imageView = imageView;
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                return BitmapFactory.decodeStream(new java.net.URL(this.url).openStream());

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}
