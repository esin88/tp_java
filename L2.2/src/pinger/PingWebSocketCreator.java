package pinger;

import base.AuthService;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.jetbrains.annotations.NotNull;
import websocket.MessageHandlerContainer;

import javax.servlet.http.HttpSession;

/**
 * Created by Solovyev on 05/04/16.
 */
public class PingWebSocketCreator implements WebSocketCreator {
    @NotNull
    private final AuthService authService;
    @NotNull
    private final PingService pingService;
    @NotNull
    private final MessageHandlerContainer messageHandlerContainer;


    public PingWebSocketCreator(@NotNull AuthService authService,
                                @NotNull PingService pingService,
                                @NotNull MessageHandlerContainer messageHandlerContainer) {
        this.authService = authService;
        this.pingService = pingService;
        this.messageHandlerContainer = messageHandlerContainer;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public PingWebSocket createWebSocket(@NotNull ServletUpgradeRequest servletUpgradeRequest,
                                         @NotNull ServletUpgradeResponse servletUpgradeResponse) {
        @SuppressWarnings("ConstantConditions")
        final HttpSession session = servletUpgradeRequest.getSession();
        if (session == null) {
            return null;
        }
        final String sessionId = session.getId();

        //noinspection ConstantConditions
        return new PingWebSocket(sessionId, authService, pingService, messageHandlerContainer);
    }
}
