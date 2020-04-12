package edu.aau.se2.server;

import java.util.ArrayList;

public class Lobby {
    private ArrayList<User> users;

    public Lobby(String hostName) {
        users = new ArrayList<>();
        users.add(new User(hostName));
        users.get(0).setHost(true);
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

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

}