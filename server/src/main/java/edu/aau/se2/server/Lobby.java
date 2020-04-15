package edu.aau.se2.server;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private ArrayList<User> users;
    private int lobbyID;

    public Lobby(String hostName, int id) {
        this.lobbyID = id;
        this.users = new ArrayList<>();
        this.users.add(new User(hostName));
        this.users.get(0).setHost(true);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public User getUser(int id) {
        return users.get(id);
    }

    public void setUser(int id, User us) {
        this.users.set(id, us);
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = (ArrayList<User>) users;
    }

    public int getLobbyID() {
        return lobbyID;
    }
}
