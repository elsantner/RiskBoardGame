package edu.aau.se2.server.networking.dto;

public class CreateLobby implements BaseMessage {
    private String userName;

    public CreateLobby() {
        this.userName = "default";
    }

    public CreateLobby(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
