package edu.aau.se2.server.networking.dto;

import java.util.ArrayList;

import edu.aau.se2.server.User;

public class UserList extends BaseMessage {
    private ArrayList<User> users;

    public UserList(){
    }

    public UserList(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }
}
