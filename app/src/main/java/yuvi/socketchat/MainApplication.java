package yuvi.socketchat;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;


/**
 * Created by yubaraj on 12/9/17.
 */

public class MainApplication extends Application {
    Socket socket;
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            socket = IO.socket("http://chat.hamroapi.com/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket(){
        return socket;
    }
}
