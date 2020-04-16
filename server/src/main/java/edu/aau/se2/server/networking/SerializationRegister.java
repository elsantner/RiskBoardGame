package edu.aau.se2.server.networking;

import java.util.ArrayList;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.CreateLobbyMessage;
import edu.aau.se2.server.networking.dto.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.JoinedLobbyMessage;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.dto.ConnectedMessage;
import edu.aau.se2.server.networking.kryonet.KryoNetComponent;


/**
 * This class helps to ensure that the class registration for all KryoNetComponents is identical.
 * This is important because the sequence of registration is vital for (de-)serialization to work.
 */
public interface SerializationRegister {

    static void registerClassesForComponent(KryoNetComponent component) {
        component.registerClass(Player.class);
        component.registerClass(ArrayList.class);
        component.registerClass(ReadyMessage.class);
        component.registerClass(StartGameMessage.class);
        component.registerClass(InitialArmyPlacingMessage.class);
        component.registerClass(ArmyPlacedMessage.class);
        component.registerClass(ConnectedMessage.class);
        component.registerClass(CreateLobbyMessage.class);
        component.registerClass(JoinedLobbyMessage.class);
    }
}
