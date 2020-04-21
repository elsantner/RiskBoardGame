package edu.aau.se2.server.networking;

import java.io.IOException;
import edu.aau.se2.server.networking.dto.BaseMessage;

public interface NetworkServer {

    /**
     * Starts the Server.
     *
     * @throws IOException If connection error occurs.
     */
    void start() throws IOException;

    /**
     * Stops the Server.
     */
    void stop();

    /**
     * Registers a callback which gets called if a message is received.
     *
     * @param callback Called when message is received.
     */
    void registerCallback(Callback<BaseMessage> callback);

    /**
     * Sends a message to all clients.
     *
     * @param message Message for clients
     */
    void broadcastMessage(BaseMessage message);

}
