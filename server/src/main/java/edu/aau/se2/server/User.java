package edu.aau.se2.server;

public class User {
    private String name;
    private boolean ready;
    private boolean isHost;

    public User() {
    }

    public User(String name) {
        this.name = name;
        this.ready = false;
        this.isHost = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }
}
