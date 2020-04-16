package edu.aau.se2.server.networking;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.aau.se2.server.networking.dto.TextMessage;
import edu.aau.se2.server.networking.kryonet.KryoNetComponent;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;

public class NetworkCommunicationIntegrationTest {
    private static final String REQUEST_TEST = "request test";
    private static final String RESPONSE_TEST = "response test";

    private AtomicBoolean request1Handled;
    private AtomicBoolean request2Handled;
    private AtomicBoolean responseHandled;
    private NetworkServer server;

    @Before
    public void setup() {
        request1Handled = new AtomicBoolean(false);
        request2Handled = new AtomicBoolean(false);
        responseHandled = new AtomicBoolean(false);
    }

    @Test
    public void NetworkConnection_OneClient_SendAndReceiveText() throws IOException, InterruptedException {
        startServer();
        startClient();

        // wait for server and client to handle messages
        Thread.sleep(1500);

        Assert.assertTrue(request1Handled.get());
        Assert.assertTrue(request2Handled.get());
        Assert.assertTrue(responseHandled.get());
    }

    private void startServer() throws IOException {
        AtomicBoolean first = new AtomicBoolean(true);

        server = new NetworkServerKryo();
        registerClassesForComponent((NetworkServerKryo)server);

        server.start();
        server.registerCallback(argument -> {
                    if (first.get()) {
                        first.set(false);
                        // check correct polymorphism
                        Assert.assertNotSame(argument.getClass(), TextMessage.class);
                        Assert.assertTrue(argument instanceof TextMessageSubClass);
                        request1Handled.set(true);
                    } else {
                        // check correct polymorphism
                        Assert.assertFalse(argument instanceof TextMessageSubClass);
                        Assert.assertTrue(argument instanceof TextMessage);

                        Assert.assertEquals(REQUEST_TEST, ((TextMessage) argument).getText());
                        request2Handled.set(true);

                        server.broadcastMessage(new TextMessage(RESPONSE_TEST));
                    }
                }
        );
    }

    private void startClient() throws IOException {
        NetworkClientKryo client = new NetworkClientKryo();
        registerClassesForComponent(client);

        client.connect("localhost");
        client.registerCallback(argument ->
                {
                    Assert.assertTrue(argument instanceof TextMessage);
                    Assert.assertEquals(RESPONSE_TEST, ((TextMessage) argument).getText());
                    responseHandled.set(true);
                }
        );

        client.sendMessage(new TextMessageSubClass());
        client.sendMessage(new TextMessage(REQUEST_TEST));
    }

    private void registerClassesForComponent(KryoNetComponent component){
        component.registerClass(TextMessageSubClass.class);
        component.registerClass(TextMessage.class);
    }

    @After
    public void teardown() {
        server.stop();
    }
}
