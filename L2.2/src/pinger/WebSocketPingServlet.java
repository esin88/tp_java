package pinger;

import base.AuthService;
import base.GameMechanics;
import base.WebSocketService;
import frontend.GameWebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.jetbrains.annotations.NotNull;
import websocket.MessageHandlerContainer;

import javax.servlet.annotation.WebServlet;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a servlet starting a webSocket application
 */
@WebServlet(name = "WebSocketPingServlet", urlPatterns = {"/ping"})
public class WebSocketPingServlet extends WebSocketServlet {
    private static final long IDLE_TIME = TimeUnit.SECONDS.toMillis(60);
    @NotNull
    private AuthService authService;
    @NotNull
    private PingService pingService;
    @NotNull
    private final MessageHandlerContainer messageHandlerContainer;

    public WebSocketPingServlet(@NotNull MessageHandlerContainer messageHandlerContainer, @NotNull PingService pingService, @NotNull AuthService authService) {
        this.messageHandlerContainer = messageHandlerContainer;
        this.pingService = pingService;
        this.authService = authService;
    }


    @SuppressWarnings("NullableProblems")
    @Override
    public void configure(@NotNull WebSocketServletFactory factory) {
        //noinspection ConstantConditions
        factory.getPolicy().setIdleTimeout(IDLE_TIME);
        factory.setCreator(new PingWebSocketCreator(authService, pingService, messageHandlerContainer));
    }
}
