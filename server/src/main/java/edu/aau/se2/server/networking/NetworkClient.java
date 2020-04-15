package edu.aau.se2.server.networking;

import java.io.IOException;

import edu.aau.se2.server.networking.dto.BaseMessage;

public interface NetworkClient {

    /**
     * Connects to a host.
     *
     * @param host Host to connect to.
     * @throws IOException If connection error occurs.
     */
    void connect(String host) throws IOException;

    /**
     * Disconnects from current host.
     */
    void disconnect();

    /**
     * Registers a callback which gets called if a message is received.
     *
     * @param callback Called if message is received.
     */
    void registerCallback(Callback<BaseMessage> callback);

    /**
     * Sends a message to the server.
     *
     * @param message Message for server.
     */
    void sendMessage(BaseMessage message);

}
