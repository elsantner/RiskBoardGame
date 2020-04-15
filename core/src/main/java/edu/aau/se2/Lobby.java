package edu.aau.se2;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import edu.aau.se2.server.User;
import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.CreateLobby;
import edu.aau.se2.server.networking.dto.TextMessage;
import edu.aau.se2.server.networking.dto.UserList;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;
import edu.aau.se2.server.networking.kryonet.NetworkConstants;
import edu.aau.se2.server.networking.kryonet.RegisterClasses;

public class Lobby {

    private NetworkClientKryo networkClientKryo;
    private ArrayList<User> users;
    private boolean usersChanged = false;
    private static final String TAG = "Lobby";
    private static final String HOST = "10.0.0.14";    // -> localhost | in cmd ipconfig eingeben -> IPv4 address
    private static final String ADDRESS = HOST + ":" + NetworkConstants.TCP_PORT;

    public Lobby() {
        this.networkClientKryo = new NetworkClientKryo();
        this.users = new ArrayList<>();
    }

    public void createLobby() {

        Gdx.app.log(TAG, "Trying to connect to Server at " + ADDRESS);

        try {
            networkClientKryo.connect(HOST);
            Gdx.app.log(TAG, "Connected to " + ADDRESS);
        } catch (IOException e) {
            Gdx.app.error(TAG, e.getMessage());
        }


        // Register classes used by server and client
        RegisterClasses.registerClasses(networkClientKryo);

        Gdx.app.log(TAG, "Send Message to create new Lobby");
        networkClientKryo.sendMessage(new CreateLobby("host"));

        networkClientKryo.registerCallback(new Callback<BaseMessage>() {
            @Override
            public void callback(BaseMessage arg) {
                if (arg instanceof TextMessage) {
                    Gdx.app.log(TAG, "Received message from Server: " + ((TextMessage) arg).getText());
                }

                if (arg instanceof UserList) {
                    users = (ArrayList<User>) ((UserList) arg).getUsers();
                    usersChanged = true;
                    Gdx.app.log(TAG, "Received userlist from Server");
                }
            }
        });
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = (ArrayList<User>) users;
    }

    public boolean isUsersChanged() {
        return usersChanged;
    }

    public void setUsersChanged(boolean usersChanged) {
        this.usersChanged = usersChanged;
    }

    public NetworkClientKryo getNetworkClientKryo() {
        return networkClientKryo;
    }

    public void setNetworkClientKryo(NetworkClientKryo networkClientKryo) {
        this.networkClientKryo = networkClientKryo;
    }

    public static String getTAG() {
        return TAG;
    }

    public static String getHOST() {
        return HOST;
    }

    public static String getADDRESS() {
        return ADDRESS;
    }
}
