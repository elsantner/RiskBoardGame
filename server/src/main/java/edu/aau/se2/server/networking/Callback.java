package edu.aau.se2.server.networking;

/**
 * Used for callbacks.
 */
public interface Callback<T> {

    void callback(T argument);

}
