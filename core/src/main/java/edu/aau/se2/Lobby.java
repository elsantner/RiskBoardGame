package edu.aau.se2;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.util.ArrayList;


import edu.aau.se2.server.User;
import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.TextMessage;
import edu.aau.se2.server.networking.dto.UserList;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;
import edu.aau.se2.server.networking.kryonet.NetworkConstants;

public class Lobby {

    private NetworkClientKryo networkClientKryo;
    private ArrayList<User> users;
    private final String LOG = "Lobby";
    private final String HOST = "10.0.2.2";    // -> localhost
    private String adress = HOST + ":" + NetworkConstants.TCP_PORT;

    public Lobby() {
        this.networkClientKryo = new NetworkClientKryo();
        this.users = new ArrayList<>();
    }

    public void startLobby() {

        Gdx.app.log(LOG, "Trying to connect to Server at " + adress);

        networkClientKryo = new NetworkClientKryo();
        try {

            networkClientKryo.connect(HOST);
            Gdx.app.log(LOG, "Connected to " + adress);
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.error(LOG, e.getMessage());
        }


        // Register classes used by server and client
        networkClientKryo.registerClass(TextMessage.class);
        networkClientKryo.registerClass(ArrayList.class);
        networkClientKryo.registerClass(User.class);
        networkClientKryo.registerClass(UserList.class);

        Gdx.app.log(LOG, "Sending \"host\" to Server");
        networkClientKryo.sendMessage(new TextMessage("host"));

        networkClientKryo.registerCallback(new Callback<BaseMessage>() {
            @Override
            public void callback(BaseMessage arg) {
                if (arg instanceof TextMessage) {
                    Gdx.app.log(LOG, "Received message from Server: " + ((TextMessage) arg).text);
                }

                if (arg instanceof UserList) {
                    users = ((UserList) arg).getUsers();
                    Gdx.app.log(LOG, "Received userlist from Server");
                }
            }
        });
    }

    public ArrayList<String> getUserNames() {
        ArrayList<String> arr = new ArrayList<>();
        for (User us : users
        ) {
            arr.add(us.getName());
        }
        return arr;
    }
}
