package edu.aau.se2.server.networking.dto;

import java.util.ArrayList;
import java.util.List;

import edu.aau.se2.server.User;

public class UserList implements BaseMessage {
    private ArrayList<User> users;

    public UserList(){
    }

    public UserList(List<User> users) {
        this.users = (ArrayList<User>) users;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = (ArrayList<User>) users;
    }
}
