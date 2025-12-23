package org.jandle;

import org.jandle.api.JandleApplication;
import org.jandle.testutil.MockHttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class JandleApplicationTest {

    @Test
    void unknownRoute_returns404() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        JandleApplication app = new JandleApplication(server);

        MockHttpExchange exchange = new MockHttpExchange("GET", "/unknown");
        app.handle(exchange);

        assertEquals(404, exchange.getStatus());
    }
}
