package edu.aau.se2.server.networking.kryonet;

import java.util.ArrayList;

import edu.aau.se2.server.User;
import edu.aau.se2.server.networking.dto.TextMessage;
import edu.aau.se2.server.networking.dto.UserList;

public class RegisterClasses {

    private RegisterClasses() {
    }

    private static Class[] classes = {
            TextMessage.class,
            ArrayList.class,
            User.class,
            UserList.class
    };

    public static void registerClasses(KryoNetComponent kryoNetComponent) {
        for (Class c : classes
        ) {
            kryoNetComponent.registerClass(c);
        }
    }
}
