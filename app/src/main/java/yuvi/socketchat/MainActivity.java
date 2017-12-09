package yuvi.socketchat;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.PublicKey;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * Created by yubaraj on 12/9/17.
 */

public class MainActivity extends AppCompatActivity {
    Socket socket = null;
    RecyclerView rv_chat;
    EditText edt_message;
    OnChatEventChanges chatListener;
    ChatAdapter adapter;
    ImageView iv_send;
    boolean isTyping = false;
    Handler typingHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Chat application");
        Log.d("Main", "InMain");
        rv_chat = findViewById(R.id.rv_chat);
        edt_message = findViewById(R.id.edt_message);
        iv_send = findViewById(R.id.iv_send);
        adapter = new ChatAdapter();
        chatListener = adapter.getListener();
        rv_chat.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv_chat.setAdapter(adapter);


        try {
            socket = IO.socket("http://chat.hamroapi.com/");
            socket.emit("join", new JSONObject().put("name", "yubaraj"));
//            socket.emit("message", "yubaraj send message");
            socket.on("message", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    JSONObject mJson = ((JSONObject) args[0]);
                    final String data = mJson.optString("name") + " : " + mJson.optString("message") + " at " + new SimpleDateFormat("HH:mm:ss").format(new Date(mJson.optLong("timestamp")));
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatListener.onAdded(data);
                            rv_chat.scrollToPosition(adapter.getItemCount() - 1);
                        }
                    });
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Exception err = (Exception) args[0];
                    Log.d("Main", "error = " + err);
                }
            });

            socket.on("typing", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    JSONObject mJson = (JSONObject) args[0];
                    final String data = mJson.optString("name") + " is typing ";
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatListener.onTyping(data);
                            rv_chat.scrollToPosition(adapter.getItemCount() - 1);
                        }
                    });
                }
            });
            socket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        edt_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                socket.emit("typing", "Yubaraj is typing");
                typingHandler.removeCallbacks(checkStatusRunnable);
                typingHandler.postDelayed(checkStatusRunnable, 3000);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        iv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(edt_message.getText().toString())) {
                    Log.d("Main", "emitting message");
                    socket.emit("message", edt_message.getText().toString());
                    edt_message.setText("");
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (socket != null) {
            socket.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (socket != null) {
            socket.connect();
        }
    }

    class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnChatEventChanges {
        List<String> dataList;

        public ChatAdapter() {
            this.dataList = new ArrayList<>();
        }

        public OnChatEventChanges getListener() {
            return this;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.row_chat, parent, false);
            return new SimpleChatViewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                GradientDrawable mDarwable = (GradientDrawable) holder.itemView.getBackground();
                if (isTyping && position == getItemCount() - 1) {
                    mDarwable.setColor(Color.RED);
                } else {
                    mDarwable.setColor(Color.parseColor("#3F51B5"));
                }
                SimpleChatViewViewHolder mHolder = (SimpleChatViewViewHolder) holder;
                mHolder.tv_chat.setText(dataList.get(position));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        @Override
        public void onAdded(String data) {
            Log.d("Main", "onAdded Called");
            if (isTyping) {
                Log.d("Main", "remove Called");
                if (dataList.size() > 0) {
                    dataList.remove(getItemCount() - 1);
                    notifyItemRemoved(getItemCount());
                    isTyping = false;
                }
            }
            if (!TextUtils.isEmpty(data)) {
                dataList.add(data);
                notifyItemInserted(dataList.size() > 0 ? dataList.size() - 1 : 0);
            }
        }

        @Override
        public void onTyping(String data) {
            Log.d("Main", "onTyping called");
            if (!isTyping) {
                dataList.add(data);
                notifyItemInserted(dataList.size() == 0 ? 0 : dataList.size() - 1);
            }
            isTyping = true;
            Log.d("MainActivity", "onTyping data = " + data);
        }
    }

    public class SimpleChatViewViewHolder extends RecyclerView.ViewHolder {
        TextView tv_chat;
        LinearLayout ll_chat;

        public SimpleChatViewViewHolder(View itemView) {
            super(itemView);
            tv_chat = itemView.findViewById(R.id.tv_chat);
            ll_chat = itemView.findViewById(R.id.ll_chat);
        }
    }

    interface OnChatEventChanges {
        void onAdded(String data);

        void onTyping(String data);
    }

    Runnable checkStatusRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isTyping) return;
            chatListener.onAdded("");
        }
    };

}
