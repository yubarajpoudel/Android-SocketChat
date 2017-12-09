package yuvi.socketchat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by yubaraj on 12/9/17.
 */

public class LoginActivity extends AppCompatActivity {
    Socket socket;
    Button button;
    EditText edt_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        button = findViewById(R.id.btn_login);
        edt_name = findViewById(R.id.edt_name);


        socket = ((MainApplication) getApplication()).getSocket();
        socket.on("join", login);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edt_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    edt_name.setError("Name cannot be empty");
                } else {
                    socket.emit("join", name);
                }
            }
        });

    }


    Emitter.Listener login = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    };
}
