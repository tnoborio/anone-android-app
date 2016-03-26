package com.halake.anone;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;


public class SelectStampPopupWindow extends PopupWindow implements AdapterView.OnItemClickListener {
    private final Listener listener;
    private Activity activity;

    public SelectStampPopupWindow(Activity activity, ArrayList<Bitmap> bitmaps, Listener listener) {
        super(activity);

        this.activity = activity;
        this.listener = listener;

        View popupView = activity.getLayoutInflater().inflate(R.layout.popup_select_stamp, null);
        setContentView(popupView);

        ListView listView = (ListView) popupView.findViewById(R.id.list);
        listView.setAdapter(new ListAdapter(bitmaps));
        listView.setOnItemClickListener(this);

        setOutsideTouchable(true);
        setFocusable(true);

        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        setWidth((int) (size.x * 0.8));
        setHeight((int) (size.y * 0.8));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        dismiss();

        this.listener.onSelectStamp(bitmap);
    }

    interface Listener {
        void onSelectStamp(Bitmap bitmap);
    }

    private class ListAdapter extends BaseAdapter {
        private List<Bitmap> bitmaps;

        public ListAdapter(ArrayList<Bitmap> bitmaps) {
            super();
            this.bitmaps = bitmaps;
        }

        @Override
        public int getCount() {
            return bitmaps.size();
        }

        @Override
        public Object getItem(int position) {
            return bitmaps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Bitmap bitmap = (Bitmap)getItem(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) SelectStampPopupWindow.this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.row_select_stamp, parent, false);
            }

            final ImageView view = (ImageView) row.findViewById(R.id.image);
            view.setImageBitmap(bitmap);

            return row;
        }
    }
}
