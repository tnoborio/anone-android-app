package com.halake.anone;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.halake.anone.models.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        API.setup(this);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> bitmaps = new ArrayList<>();
                bitmaps.add("http://nyampass.com/stamp1.png");
                bitmaps.add("http://nyampass.com/stamp2.png");
                bitmaps.add("http://nyampass.com/stamp1.png");
                bitmaps.add("http://nyampass.com/stamp2.png");
                new SelectStampPopupWindow(MainActivity.this, bitmaps, new SelectStampPopupWindow.Listener() {
                    @Override
                    public void onSelectStamp(Bitmap bitmap) {
                        API.postStamp(bitmap, "child", new API.PostStamp.Listener() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "スタンプを送信しました！", Toast.LENGTH_LONG).show();
                                        reloadMessages();
                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                // do nothing
                            }
                        });
                    }
                }).showAsDropDown(findViewById(R.id.fab));
            }
        });

        registerGCMToken(GoogleCloudMessaging.getInstance(this));

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(new ListAdapter());

        reloadMessages();
    }

    private void reloadMessages() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("読込中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        API.fetchMessages(new API.FetchMessages.Listener() {
            @Override
            public void onSuccess(final List<Message> messages) {
                inMainThred(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        listAdapter().updateData(messages);
                    }
                });
            }

            @Override
            public void onError() {
                inMainThred(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    private Handler handler = new Handler();
    @SuppressWarnings("SpellCheckingInspection")
    private void inMainThred(Runnable r) {
        handler.post(r);
    }

    private ListAdapter listAdapter() {
        return (ListAdapter)this.listView.getAdapter();
    }

    private void registerGCMToken(final GoogleCloudMessaging gcm) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    return gcm.register(Config.SENDER_ID);
                } catch (IOException ex) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String id) {
                sendRegistrationIdToBackend(id);
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend(final String id) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                API.postToken(id);
                return null;
            }
        }.execute(null, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ListAdapter extends BaseAdapter {
        private List<Message> messages = new ArrayList<>();

        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public Object getItem(int position) {
            return messages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).type.ordinal();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Message message = (Message)getItem(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                int id;

                switch (message.type) {
                    case Audio:
                       id = R.layout.row_left_message;
                        break;

                    case Stamp:
                        id = R.layout.row_right_message;
                        break;

                    default:
                        throw new IllegalAccessError();
                }

                row = inflater.inflate(id, parent, false);
            }

            if (row.findViewById(R.id.face) != null)
                ViewUtils.roundedImage(MainActivity.this, (ImageView) row.findViewById(R.id.face), R.drawable.child);

            if (row.findViewById(R.id.message) != null) {
                final TextView textView = (TextView) row.findViewById(R.id.message);
                textView.setText(message.message);
            }

            if (row.findViewById(R.id.stamp) != null) {
                ImageView imageView = (ImageView) row.findViewById(R.id.stamp);
                new ViewUtils.ImageDownloader(imageView, message.url).execute();
            }

            return row;
        }

        public void updateData(List<Message> messages) {
            this.messages = messages;
            notifyDataSetChanged();
        }
    }
}
