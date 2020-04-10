package edu.aau.se2;

import java.io.IOException;
import java.util.ArrayList;


import edu.aau.se2.server.User;
import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.TextMessage;
import edu.aau.se2.server.networking.dto.UserList;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

public class Lobby {

    private NetworkClientKryo networkClientKryo;
    private ArrayList<User> users;

    public Lobby() {
        this.networkClientKryo = new NetworkClientKryo();
    }

    public void startLobby(){

        System.out.println("try to connect");

        networkClientKryo = new NetworkClientKryo();
        try {
            // 10.0.2.2 -> localhost
            networkClientKryo.connect("10.0.2.2");
            System.out.println("connected");
        }catch(IOException e){
            e.printStackTrace();
            System.err.println("error");
        }


        // Register classes used by server and client
        networkClientKryo.registerClass(TextMessage.class);
        networkClientKryo.registerClass(ArrayList.class);
        networkClientKryo.registerClass(User.class);
        networkClientKryo.registerClass(UserList.class);

        System.out.println("sendmessage");
        networkClientKryo.sendMessage(new TextMessage("host"));
        networkClientKryo.registerCallback(new Callback<BaseMessage>() {

            // Wird aufgerufen wenn der Server eine Nachricht zurücksendet
            @Override
            public void callback(BaseMessage arg) {
                if (arg instanceof TextMessage) {
                    System.out.println(((TextMessage) arg).text);
                }

                if (arg instanceof UserList){
                    users = ((UserList) arg).getUsers();
                    System.out.println(users.toString());
                }
            }
        });
    }

    public ArrayList<String> getUserNames(){
       ArrayList<String> arr = new ArrayList<>();
        for (User us: users
             ) {
           arr.add(us.getName());
        }
        return arr;
    }
}
