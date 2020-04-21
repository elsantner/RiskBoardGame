package edu.aau.se2.server.networking.kryonet;

public interface KryoNetComponent {

    /**
     * Register a class for serialization.
     *
     * @param c
     */
    void registerClass(Class<?> c);

}
