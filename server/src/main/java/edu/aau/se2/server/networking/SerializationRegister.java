package edu.aau.se2.server.networking;

import java.util.ArrayList;

import edu.aau.se2.server.data.Card;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.game.AccuseCheaterMessage;
import edu.aau.se2.server.networking.dto.game.ArmyMovedMessage;
import edu.aau.se2.server.networking.dto.game.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.game.AttackResultMessage;
import edu.aau.se2.server.networking.dto.game.AttackStartedMessage;
import edu.aau.se2.server.networking.dto.game.AttackingPhaseFinishedMessage;
import edu.aau.se2.server.networking.dto.game.CardExchangeMessage;
import edu.aau.se2.server.networking.dto.game.DefenderDiceCountMessage;
import edu.aau.se2.server.networking.dto.game.DiceResultMessage;
import edu.aau.se2.server.networking.dto.game.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.game.LeftGameMessage;
import edu.aau.se2.server.networking.dto.game.NewArmiesMessage;
import edu.aau.se2.server.networking.dto.game.NewCardMessage;
import edu.aau.se2.server.networking.dto.game.NextTurnMessage;
import edu.aau.se2.server.networking.dto.game.OccupyTerritoryMessage;
import edu.aau.se2.server.networking.dto.game.PlayerLostMessage;
import edu.aau.se2.server.networking.dto.game.RefreshCardsMessage;
import edu.aau.se2.server.networking.dto.game.StartGameMessage;
import edu.aau.se2.server.networking.dto.game.VictoryMessage;
import edu.aau.se2.server.networking.dto.lobby.CreateLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.ErrorMessage;
import edu.aau.se2.server.networking.dto.lobby.JoinedLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.LeftLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.PlayersChangedMessage;
import edu.aau.se2.server.networking.dto.lobby.ReadyMessage;
import edu.aau.se2.server.networking.dto.lobby.RequestJoinLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.RequestLeaveLobby;
import edu.aau.se2.server.networking.dto.prelobby.ChangeNicknameMessage;
import edu.aau.se2.server.networking.dto.prelobby.ConnectedMessage;
import edu.aau.se2.server.networking.dto.prelobby.LobbyListMessage;
import edu.aau.se2.server.networking.dto.prelobby.RequestLobbyListMessage;
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
        component.registerClass(NextTurnMessage.class);
        component.registerClass(CardExchangeMessage.class);
        component.registerClass(NewArmiesMessage.class);
        component.registerClass(RequestLobbyListMessage.class);
        component.registerClass(LobbyListMessage.class);
        component.registerClass(LobbyListMessage.LobbyData.class);
        component.registerClass(RequestJoinLobbyMessage.class);
        component.registerClass(ErrorMessage.class);
        component.registerClass(RequestLeaveLobby.class);
        component.registerClass(LeftLobbyMessage.class);
        component.registerClass(PlayersChangedMessage.class);
        component.registerClass(ArmyMovedMessage.class);
        component.registerClass(AttackingPhaseFinishedMessage.class);
        component.registerClass(NewCardMessage.class);
        component.registerClass(String[].class);
        component.registerClass(RefreshCardsMessage.class);
        component.registerClass(AttackStartedMessage.class);
        component.registerClass(DiceResultMessage.class);
        component.registerClass(AttackResultMessage.class);
        component.registerClass(OccupyTerritoryMessage.class);
        component.registerClass(Card[].class);
        component.registerClass(DefenderDiceCountMessage.class);
        component.registerClass(PlayerLostMessage.class);
        component.registerClass(LeftGameMessage.class);
        component.registerClass(VictoryMessage.class);
        component.registerClass(AccuseCheaterMessage.class);
        component.registerClass(ChangeNicknameMessage.class);
    }
}
